package mx.com.lab.spei.client;

import mx.com.lab.spei.client.dto.FineractAccountInfo;
import mx.com.lab.spei.client.dto.FineractDepositRequest;

/**
 * Contract for all interactions with the Apache Fineract REST API.
 *
 * <p>Implementations are responsible for:
 * <ul>
 *   <li>Authenticating every call with the configured Basic Auth credentials and tenant header.</li>
 *   <li>Translating Fineract HTTP errors into {@link mx.com.lab.spei.exception.FineractClientException}.</li>
 *   <li>Translating missing / inactive account scenarios into the appropriate business exceptions.</li>
 * </ul>
 *
 * <p>Requirements covered: 4.1–4.5, 6.1–6.6
 */
public interface FineractClient {

    /**
     * Looks up a Fineract savings account by its CLABE (stored as {@code externalId}).
     *
     * @param clabe the 18-digit CLABE to search for
     * @return account information needed for deposit calls
     * @throws mx.com.lab.spei.exception.AccountNotFoundException  if no savings account exists for the given CLABE
     * @throws mx.com.lab.spei.exception.AccountInactiveException  if an account exists but is not in ACTIVE status
     * @throws mx.com.lab.spei.exception.FineractClientException   on any Fineract HTTP error or network failure
     */
    FineractAccountInfo findAccountByClabe(String clabe);

    /**
     * Posts a deposit transaction to a Fineract savings account.
     *
     * @param savingsId the Fineract savings account identifier
     * @param request   the deposit payload (amount, date, paymentTypeId, note, …)
     * @return the Fineract {@code resourceId} of the created transaction
     * @throws mx.com.lab.spei.exception.FineractClientException on any Fineract HTTP error or network failure
     */
    long depositTransaction(long savingsId, FineractDepositRequest request);
}
