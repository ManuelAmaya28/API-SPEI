package mx.com.lab.spei.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Request body for POST /savingsaccounts/{savingsId}/transactions?command=deposit
 */
public class FineractDepositRequest {

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("dateFormat")
    private String dateFormat;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("transactionAmount")
    private BigDecimal transactionAmount;

    @JsonProperty("paymentTypeId")
    private Integer paymentTypeId;

    @JsonProperty("note")
    private String note;

    // --- Default constructor ---

    public FineractDepositRequest() {}

    // --- All-args constructor ---

    public FineractDepositRequest(String locale,
                                  String dateFormat,
                                  String transactionDate,
                                  BigDecimal transactionAmount,
                                  Integer paymentTypeId,
                                  String note) {
        this.locale = locale;
        this.dateFormat = dateFormat;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentTypeId = paymentTypeId;
        this.note = note;
    }

    // --- Builder factory ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String locale = "en";
        private String dateFormat = "dd MMMM yyyy";
        private String transactionDate;
        private BigDecimal transactionAmount;
        private Integer paymentTypeId;
        private String note;

        private Builder() {}

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public Builder transactionDate(String transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }

        public Builder transactionAmount(BigDecimal transactionAmount) {
            this.transactionAmount = transactionAmount;
            return this;
        }

        public Builder paymentTypeId(Integer paymentTypeId) {
            this.paymentTypeId = paymentTypeId;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public FineractDepositRequest build() {
            return new FineractDepositRequest(locale, dateFormat, transactionDate,
                    transactionAmount, paymentTypeId, note);
        }
    }

    // --- Getters & Setters ---

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    public Integer getPaymentTypeId() { return paymentTypeId; }
    public void setPaymentTypeId(Integer paymentTypeId) { this.paymentTypeId = paymentTypeId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
