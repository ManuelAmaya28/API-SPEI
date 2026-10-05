package mx.com.lab.spei.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from POST /savingsaccounts/{savingsId}/transactions?command=deposit
 * Fineract returns the created transaction ID as "resourceId".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FineractTransactionResponse {

    private Long resourceId;

    public FineractTransactionResponse() {}

    public FineractTransactionResponse(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
}
