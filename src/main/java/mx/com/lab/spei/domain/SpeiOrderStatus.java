package mx.com.lab.spei.domain;

/**
 * Represents the lifecycle status of a processed SPEI transfer order.
 *
 * <p>{@code APPLIED} indicates that all three Fineract deposit transactions
 * (principal, spread, and commission) completed successfully and the order has
 * been persisted to durable storage.
 */
public enum SpeiOrderStatus {

    /**
     * The SPEI transfer was fully processed: principal, spread, and commission
     * deposits were recorded in Fineract and the order was persisted.
     */
    APPLIED
}
