package mx.com.lab.spei.client.dto;

/**
 * Internal DTO carrying the account information extracted from a Fineract
 * savings account lookup. Passed from {@code FineractClient} to the service
 * layer — decoupled from the raw Fineract response structure.
 */
public class FineractAccountInfo {

    /** The Fineract savings account ID, used in all deposit calls. */
    private Long savingsId;

    /** The Fineract client ID associated with this account. */
    private Long clientId;

    /** The display name of the Fineract client (e.g. "Juan Pérez"). */
    private String clientName;

    public FineractAccountInfo() {}

    public FineractAccountInfo(Long savingsId, Long clientId, String clientName) {
        this.savingsId = savingsId;
        this.clientId = clientId;
        this.clientName = clientName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long savingsId;
        private Long clientId;
        private String clientName;

        private Builder() {}

        public Builder savingsId(Long savingsId) {
            this.savingsId = savingsId;
            return this;
        }

        public Builder clientId(Long clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientName(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public FineractAccountInfo build() {
            return new FineractAccountInfo(savingsId, clientId, clientName);
        }
    }

    public Long getSavingsId() { return savingsId; }
    public void setSavingsId(Long savingsId) { this.savingsId = savingsId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
}
