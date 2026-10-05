package mx.com.lab.spei.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Root response from GET /savingsaccounts?externalId={CLABE}&associations=all
 * Fineract returns results in a paginated wrapper with a "pageItems" array.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FineractAccountResponse {

    private List<FineractSavingsAccount> pageItems;

    public FineractAccountResponse() {}

    public List<FineractSavingsAccount> getPageItems() { return pageItems; }
    public void setPageItems(List<FineractSavingsAccount> pageItems) { this.pageItems = pageItems; }

    // -------------------------------------------------------------------------

    /**
     * Individual savings account entry within pageItems.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FineractSavingsAccount {

        /** savingsId used in all subsequent deposit calls */
        private Long id;

        /** Fineract returns clientId as an object: { "id": N, "displayName": "..." } */
        private FineractClientInfo clientId;

        /** CLABE stored as externalId on the account */
        private String externalId;

        private FineractAccountStatus status;

        public FineractSavingsAccount() {}

        /**
         * Returns true when the account status is "Active" or "ACTIVE"
         * (Fineract may return either casing depending on version).
         */
        public boolean isActive() {
            return status != null &&
                    ("Active".equalsIgnoreCase(status.getValue()) ||
                     "ACTIVE".equalsIgnoreCase(status.getValue()));
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public FineractClientInfo getClientId() { return clientId; }
        public void setClientId(FineractClientInfo clientId) { this.clientId = clientId; }

        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }

        public FineractAccountStatus getStatus() { return status; }
        public void setStatus(FineractAccountStatus status) { this.status = status; }
    }

    // -------------------------------------------------------------------------

    /**
     * Fineract client reference object embedded inside a savings account response.
     * Example: { "id": 42, "displayName": "Juan Pérez" }
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FineractClientInfo {

        private Long id;
        private String displayName;

        public FineractClientInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }

    // -------------------------------------------------------------------------

    /**
     * Account status object returned by Fineract.
     * Example: { "id": 300, "code": "savingsAccountStatusType.active", "value": "Active" }
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FineractAccountStatus {

        private String value;

        public FineractAccountStatus() {}

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
