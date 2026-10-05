package mx.com.lab.spei.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Externalized configuration properties for the SPEI Inbound API.
 *
 * <p>Bound from the {@code spei.*} prefix in {@code application.yml} (or the
 * equivalent environment variables).  Spring Boot's {@code @Validated} support
 * causes the application to <em>fail at startup</em> if any constraint is
 * violated, satisfying requirements 5.4, 5.5, 6.8, 10.1, and 10.4.
 */
@ConfigurationProperties(prefix = "spei")
@Validated
public class SpeiProperties {

    /**
     * Fineract connection settings — all fields are mandatory.
     */
    @Valid
    @NotNull
    private Fineract fineract;

    /**
     * Fractional spread rate applied to each transfer amount.
     * Must be in the range [0.000001, 0.999999].
     */
    @NotNull
    @DecimalMin(value = "0.000001", message = "spread-rate must be at least 0.000001")
    @DecimalMax(value = "0.999999", message = "spread-rate must not exceed 0.999999")
    private BigDecimal spreadRate;

    /**
     * Fixed commission charge in MXN applied to each transfer.
     * Must be in the range [0.01, 999999.99].
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "commission must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "commission must not exceed 999999.99")
    private BigDecimal commission;

    /**
     * Fineract payment-type identifier used for all deposit transactions.
     * Must be a positive integer (≥ 1).
     */
    @NotNull
    @Min(value = 1, message = "payment-type-id must be a positive integer")
    private Integer paymentTypeId;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Fineract getFineract() {
        return fineract;
    }

    public void setFineract(Fineract fineract) {
        this.fineract = fineract;
    }

    public BigDecimal getSpreadRate() {
        return spreadRate;
    }

    public void setSpreadRate(BigDecimal spreadRate) {
        this.spreadRate = spreadRate;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public Integer getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(Integer paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    // ── Inner class ──────────────────────────────────────────────────────────

    /**
     * Fineract connection settings.
     */
    public static class Fineract {

        /** Base URL of the Fineract REST API (no trailing slash). */
        @NotBlank(message = "fineract.base-url must not be blank")
        private String baseUrl;

        /** Fineract Basic Auth username. */
        @NotBlank(message = "fineract.username must not be blank")
        private String username;

        /** Fineract Basic Auth password. */
        @NotBlank(message = "fineract.password must not be blank")
        private String password;

        /** Fineract multi-tenant identifier (e.g. {@code default}). */
        @NotBlank(message = "fineract.tenant-id must not be blank")
        private String tenantId;

        // ── Getters & Setters ────────────────────────────────────────────────

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }
}
