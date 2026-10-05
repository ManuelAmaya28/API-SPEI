package mx.com.lab.spei.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Root response from GET /savingsaccounts?externalId={CLABE}&associations=all
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FineractAccountResponse {

    private List<FineractSavingsAccount> pageItems;

    public FineractAccountResponse() {
    }

    public List<FineractSavingsAccount> getPageItems() {
        return pageItems;
    }

    public void setPageItems(List<FineractSavingsAccount> pageItems) {
        this.pageItems = pageItems;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FineractSavingsAccount {

        private Long id;

        /**
         * Fineract returns numeric clientId.
         */
        private Long clientId;

        private String externalId;

        private FineractAccountStatus status;

        public FineractSavingsAccount() {
        }

        public boolean isActive() {
            return status != null
                    && ("Active".equalsIgnoreCase(status.getValue())
                    || "ACTIVE".equalsIgnoreCase(status.getValue()));
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getClientId() {
            return clientId;
        }

        public void setClientId(Long clientId) {
            this.clientId = clientId;
        }

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public FineractAccountStatus getStatus() {
            return status;
        }

        public void setStatus(FineractAccountStatus status) {
            this.status = status;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FineractAccountStatus {

        private String value;

        public FineractAccountStatus() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}