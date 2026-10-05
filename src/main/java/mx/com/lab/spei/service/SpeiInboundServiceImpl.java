package mx.com.lab.spei.service;

import mx.com.lab.spei.client.FineractClient;
import mx.com.lab.spei.client.dto.FineractAccountInfo;
import mx.com.lab.spei.client.dto.FineractDepositRequest;
import mx.com.lab.spei.config.SpeiProperties;
import mx.com.lab.spei.domain.SpeiOrder;
import mx.com.lab.spei.domain.SpeiOrderStatus;
import mx.com.lab.spei.dto.SpeiInboundRequest;
import mx.com.lab.spei.dto.SpeiInboundResult;
import mx.com.lab.spei.dto.SpeiOrderResponse;
import mx.com.lab.spei.repository.SpeiOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates the full SPEI inbound transfer pipeline.
 *
 * <p>Processing steps:
 * <ol>
 *   <li>Idempotency check — return cached 200 if the key was already completed.</li>
 *   <li>Fineract account lookup by CLABE.</li>
 *   <li>Amount calculation (principal, spread, commission).</li>
 *   <li>Three sequential deposit transactions in Fineract.</li>
 *   <li>Persistence of the {@link SpeiOrder} with status {@code APPLIED}.</li>
 *   <li>Mark idempotency key as {@code COMPLETED}.</li>
 * </ol>
 *
 * <p>On any failure after the idempotency lock is acquired the lock is released
 * so the caller can retry with the same key. Persistence failures after all
 * Fineract deposits succeed are logged at ERROR level with enough detail for
 * manual recovery.</p>
 *
 * <p>Requirements: 3.1–3.5, 4.1–4.5, 5.1–5.3, 6.1–6.7, 7.1–7.3, 8.1–8.3, 9.1–9.2</p>
 */
@Service
public class SpeiInboundServiceImpl implements SpeiInboundService {

    private static final Logger log = LoggerFactory.getLogger(SpeiInboundServiceImpl.class);

    /** Fineract deposit date format: "dd MMMM yyyy" in English locale (e.g. "15 January 2025"). */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final FineractClient fineractClient;
    private final AmountCalculator amountCalculator;
    private final IdempotencyService idempotencyService;
    private final SpeiOrderRepository speiOrderRepository;
    private final SpeiProperties speiProperties;

    public SpeiInboundServiceImpl(FineractClient fineractClient,
                                   AmountCalculator amountCalculator,
                                   IdempotencyService idempotencyService,
                                   SpeiOrderRepository speiOrderRepository,
                                   SpeiProperties speiProperties) {
        this.fineractClient = fineractClient;
        this.amountCalculator = amountCalculator;
        this.idempotencyService = idempotencyService;
        this.speiOrderRepository = speiOrderRepository;
        this.speiProperties = speiProperties;
    }

    // ── process ──────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code isIdempotentReplay = true} (HTTP 200) when the supplied
     * {@code idempotencyKey} already has a completed order stored. Returns
     * {@code isIdempotentReplay = false} (HTTP 201) for a freshly processed order.
     */
    @Override
    public SpeiInboundResult process(SpeiInboundRequest request, UUID idempotencyKey) {

        // ── Step 1: idempotency check ─────────────────────────────────────────
        Optional<SpeiOrderResponse> cached = idempotencyService.checkAndLock(idempotencyKey);
        if (cached.isPresent()) {
            log.debug("Idempotent replay for key={}", idempotencyKey);
            return new SpeiInboundResult(cached.get(), true);
        }

        try {
            // ── Step 2: Fineract account lookup ───────────────────────────────
            FineractAccountInfo account =
                    fineractClient.findAccountByClabe(request.cuentaBeneficiaria());

            // ── Step 3: amount calculation ────────────────────────────────────
            BigDecimal principal = amountCalculator.calculatePrincipal(request.monto());
            BigDecimal spread    = amountCalculator.calculateSpread(
                    request.monto(), speiProperties.getSpreadRate());
            BigDecimal commission = amountCalculator.calculateCommission(
                    speiProperties.getCommission());

            String txDate        = LocalDate.now().format(DATE_FMT);
            Integer paymentTypeId = speiProperties.getPaymentTypeId();
            String  claveRastreo  = request.claveRastreo();

            // ── Step 4a: principal deposit ────────────────────────────────────
            long depositTxId = fineractClient.depositTransaction(
                    account.getSavingsId(),
                    FineractDepositRequest.builder()
                            .transactionDate(txDate)
                            .transactionAmount(principal)
                            .paymentTypeId(paymentTypeId)
                            .note("SPEI-IN principal " + claveRastreo)
                            .build());

            // ── Step 4b: spread deposit ───────────────────────────────────────
            long spreadTxId = fineractClient.depositTransaction(
                    account.getSavingsId(),
                    FineractDepositRequest.builder()
                            .transactionDate(txDate)
                            .transactionAmount(spread)
                            .paymentTypeId(paymentTypeId)
                            .note("SPEI-IN spread " + claveRastreo)
                            .build());

            // ── Step 4c: commission deposit ───────────────────────────────────
            long commissionTxId = fineractClient.depositTransaction(
                    account.getSavingsId(),
                    FineractDepositRequest.builder()
                            .transactionDate(txDate)
                            .transactionAmount(commission)
                            .paymentTypeId(paymentTypeId)
                            .note("SPEI-IN commission " + claveRastreo)
                            .build());

            // ── Step 5: build and persist SpeiOrder ───────────────────────────
            SpeiOrder order = new SpeiOrder();
            order.setId(UUID.randomUUID());
            order.setClaveRastreo(claveRastreo);
            order.setMonto(request.monto());
            order.setCuentaBeneficiaria(request.cuentaBeneficiaria());
            order.setConcepto(request.concepto());
            order.setPrincipal(principal);
            order.setSpread(spread);
            order.setCommission(commission);
            order.setFineractClientId(account.getClientId());
            order.setFineractClientName(account.getClientName());
            order.setFineractSavingsId(account.getSavingsId());
            order.setFineractDepositTxId(depositTxId);
            order.setFineractSpreadTxId(spreadTxId);
            order.setFineractCommissionTxId(commissionTxId);
            order.setProcessedAt(Instant.now());
            order.setStatus(SpeiOrderStatus.APPLIED);

            SpeiOrder saved;
            try {
                saved = speiOrderRepository.save(order);
            } catch (Exception persistenceEx) {
                // All three Fineract deposits succeeded but we could not persist the order.
                // Log enough detail for manual recovery — requirement 7.3.
                log.error("Persistence failure after successful Fineract deposits. " +
                                "claveRastreo={} depositTxId={} spreadTxId={} commissionTxId={}",
                        claveRastreo, depositTxId, spreadTxId, commissionTxId, persistenceEx);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Order could not be persisted after deposits were applied");
            }

            // ── Step 6: mark idempotency key completed ────────────────────────
            idempotencyService.complete(idempotencyKey, saved);

            // ── Step 7: return result ─────────────────────────────────────────
            return new SpeiInboundResult(SpeiOrderResponse.from(saved), false);

        } catch (ResponseStatusException e) {
            idempotencyService.release(idempotencyKey);
            throw e;
        } catch (Exception e) {
            idempotencyService.release(idempotencyKey);
            throw e;
        }
    }

    // ── findById ──────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Throws {@link ResponseStatusException} with HTTP 404 when no order
     * exists for the supplied {@code id}.</p>
     */
    @Override
    public SpeiOrderResponse findById(UUID id) {
        return speiOrderRepository.findById(id)
                .map(SpeiOrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "SPEI order " + id + " not found"));
    }
}
