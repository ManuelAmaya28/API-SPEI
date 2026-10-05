package mx.com.lab.spei.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a processed SPEI inbound transfer order.
 * Mapped to the {@code spei_orders} table.
 */
@Entity
@Table(name = "spei_orders")
public class SpeiOrder {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 40)
    private String claveRastreo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 18)
    private String cuentaBeneficiaria;

    @Column(length = 200)
    private String concepto;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal spread;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal commission;

    @Column(nullable = false)
    private Long fineractClientId;

    @Column(nullable = false, length = 200)
    private String fineractClientName;

    @Column(nullable = false)
    private Long fineractSavingsId;

    @Column(nullable = false)
    private Long fineractDepositTxId;

    @Column(nullable = false)
    private Long fineractSpreadTxId;

    @Column(nullable = false)
    private Long fineractCommissionTxId;

    @Column(nullable = false)
    private Instant processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SpeiOrderStatus status;

    /** No-arg constructor required by JPA. */
    public SpeiOrder() {
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClaveRastreo() {
        return claveRastreo;
    }

    public void setClaveRastreo(String claveRastreo) {
        this.claveRastreo = claveRastreo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getCuentaBeneficiaria() {
        return cuentaBeneficiaria;
    }

    public void setCuentaBeneficiaria(String cuentaBeneficiaria) {
        this.cuentaBeneficiaria = cuentaBeneficiaria;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getSpread() {
        return spread;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public Long getFineractClientId() {
        return fineractClientId;
    }

    public void setFineractClientId(Long fineractClientId) {
        this.fineractClientId = fineractClientId;
    }

    public String getFineractClientName() {
        return fineractClientName;
    }

    public void setFineractClientName(String fineractClientName) {
        this.fineractClientName = fineractClientName;
    }

    public Long getFineractSavingsId() {
        return fineractSavingsId;
    }

    public void setFineractSavingsId(Long fineractSavingsId) {
        this.fineractSavingsId = fineractSavingsId;
    }

    public Long getFineractDepositTxId() {
        return fineractDepositTxId;
    }

    public void setFineractDepositTxId(Long fineractDepositTxId) {
        this.fineractDepositTxId = fineractDepositTxId;
    }

    public Long getFineractSpreadTxId() {
        return fineractSpreadTxId;
    }

    public void setFineractSpreadTxId(Long fineractSpreadTxId) {
        this.fineractSpreadTxId = fineractSpreadTxId;
    }

    public Long getFineractCommissionTxId() {
        return fineractCommissionTxId;
    }

    public void setFineractCommissionTxId(Long fineractCommissionTxId) {
        this.fineractCommissionTxId = fineractCommissionTxId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public SpeiOrderStatus getStatus() {
        return status;
    }

    public void setStatus(SpeiOrderStatus status) {
        this.status = status;
    }
}
