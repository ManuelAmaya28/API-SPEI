-- ============================================================
-- SPEI Inbound API — DDL schema
-- Compatible with H2 and PostgreSQL
-- Requirements: 7.2
-- ============================================================

CREATE TABLE IF NOT EXISTS spei_orders (
    id                      UUID            NOT NULL,
    clave_rastreo           VARCHAR(40)     NOT NULL,
    monto                   DECIMAL(15, 2)  NOT NULL,
    cuenta_beneficiaria     VARCHAR(18)     NOT NULL,
    concepto                VARCHAR(200),
    principal               DECIMAL(15, 2)  NOT NULL,
    spread                  DECIMAL(15, 2)  NOT NULL,
    commission              DECIMAL(15, 2)  NOT NULL,
    fineract_client_id      BIGINT          NOT NULL,
    fineract_client_name    VARCHAR(200)    NOT NULL,
    fineract_savings_id     BIGINT          NOT NULL,
    fineract_deposit_tx_id  BIGINT          NOT NULL,
    fineract_spread_tx_id   BIGINT          NOT NULL,
    fineract_commission_tx_id BIGINT        NOT NULL,
    processed_at            TIMESTAMP       NOT NULL,
    status                  VARCHAR(20)     NOT NULL,
    CONSTRAINT pk_spei_orders PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    key_value       UUID            NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    spei_order_id   UUID,
    created_at      TIMESTAMP       NOT NULL,
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (id),
    CONSTRAINT uq_idempotency_keys_key_value UNIQUE (key_value)
);
