package mx.com.lab.spei.client;

import mx.com.lab.spei.client.dto.FineractAccountInfo;
import mx.com.lab.spei.client.dto.FineractAccountResponse;
import mx.com.lab.spei.client.dto.FineractDepositRequest;
import mx.com.lab.spei.client.dto.FineractTransactionResponse;
import mx.com.lab.spei.exception.AccountInactiveException;
import mx.com.lab.spei.exception.AccountNotFoundException;
import mx.com.lab.spei.exception.FineractClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * WebClient-based implementation of {@link FineractClient}.
 *
 * <p>All blocking calls use a fixed timeout:
 * <ul>
 *   <li>{@code findAccountByClabe} — 10 s</li>
 *   <li>{@code depositTransaction}  — 30 s</li>
 * </ul>
 *
 * HTTP error responses are translated to {@link FineractClientException} via
 * {@code onStatus}; network / connectivity errors are caught and re-wrapped.
 *
 * <p>Requirements covered: 4.1–4.5, 6.1–6.6
 */
@Component
public class FineractClientImpl implements FineractClient {

    private static final Logger log = LoggerFactory.getLogger(FineractClientImpl.class);

    private static final Duration ACCOUNT_LOOKUP_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEPOSIT_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;

    public FineractClientImpl(WebClient fineractWebClient) {
        this.webClient = fineractWebClient;
    }

    // ── Account Lookup ────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Calls {@code GET /savingsaccounts?externalId={clabe}&associations=all}.
     * Filters for status {@code ACTIVE}; throws {@link AccountNotFoundException} when no
     * match exists, {@link AccountInactiveException} when a match exists but is not active.
     */
    @Override
    public FineractAccountInfo findAccountByClabe(String clabe) {
        log.debug("Looking up Fineract savings account for CLABE: {}", clabe);

        FineractAccountResponse response;
        try {
            response = webClient.get()
                    .uri("/savingsaccounts?externalId={clabe}&associations=all", clabe)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, httpResponse ->
                            httpResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Unknown Fineract error")
                                    .map(body -> new FineractClientException("Fineract error: " + body)))
                    .bodyToMono(FineractAccountResponse.class)
                    .block(ACCOUNT_LOOKUP_TIMEOUT);
        } catch (FineractClientException e) {
            throw e;
        } catch (WebClientRequestException e) {
            throw new FineractClientException("Unable to reach Fineract: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FineractClientException("Fineract account lookup failed: " + e.getMessage(), e);
        }

        List<FineractAccountResponse.FineractSavingsAccount> pageItems =
                (response != null) ? response.getPageItems() : null;

        if (pageItems == null || pageItems.isEmpty()) {
            throw new AccountNotFoundException(clabe);
        }

        // Prefer the first active account
        return pageItems.stream()
                .filter(FineractAccountResponse.FineractSavingsAccount::isActive)
                .findFirst()
                .map(account -> FineractAccountInfo.builder()
                        .savingsId(account.getId())
                        .clientId(account.getClientId())
                        .clientName("UNKNOWN")
                        .build())
                .orElseThrow(() -> {
                    // At least one account exists but none is active — report the first one's status
                    String statusValue = pageItems.get(0).getStatus() != null
                            ? pageItems.get(0).getStatus().getValue()
                            : "UNKNOWN";
                    return new AccountInactiveException(clabe, statusValue);
                });
    }

    // ── Deposit Transaction ───────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Calls {@code POST /savingsaccounts/{savingsId}/transactions?command=deposit}.
     * Returns the Fineract {@code resourceId} of the created transaction.
     */
    @Override
    public long depositTransaction(long savingsId, FineractDepositRequest request) {
        log.debug("Posting deposit to savingsId={}, amount={}", savingsId, request.getTransactionAmount());

        FineractTransactionResponse txResponse;
        try {
            txResponse = webClient.post()
                    .uri("/savingsaccounts/{savingsId}/transactions?command=deposit", savingsId)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, httpResponse ->
                            httpResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Unknown Fineract error")
                                    .map(body -> new FineractClientException("Fineract error: " + body)))
                    .bodyToMono(FineractTransactionResponse.class)
                    .block(DEPOSIT_TIMEOUT);
        } catch (FineractClientException e) {
            throw e;
        } catch (WebClientRequestException e) {
            throw new FineractClientException("Unable to reach Fineract: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FineractClientException("Fineract deposit failed: " + e.getMessage(), e);
        }

        if (txResponse == null || txResponse.getResourceId() == null) {
            throw new FineractClientException("Fineract returned no transaction ID for savingsId=" + savingsId);
        }

        return txResponse.getResourceId();
    }
}
