# SPEI Inbound API

API REST en Java 21 + Spring Boot 3 que recibe transferencias SPEI entrantes simuladas, valida el token con Keycloak, calcula los importes (principal, spread, comision) y registra 3 depositos en Apache Fineract.

## Requisitos

- Java 21
- Maven 3.9+
- Acceso de red a los servidores del laboratorio

## Configuracion

Todas las variables de entorno tienen valores por defecto en `application.yml`:

| Variable | Descripcion | Default |
|----------|-------------|---------|
| `KEYCLOAK_ISSUER_URI` | URI del issuer Keycloak | `https://auth.lab.dnu.mx/realms/fineract_keycloak` |
| `FINERACT_BASE_URL` | Base URL de Fineract | `https://fineract.lab.dnu.mx/fineract-provider/api/v1` |
| `FINERACT_USERNAME` | Usuario Basic Auth Fineract | `mifos` |
| `FINERACT_PASSWORD` | Password Basic Auth Fineract | `password` |
| `FINERACT_TENANT_ID` | Tenant Fineract | `default` |
| `SPEI_SPREAD_RATE` | Tasa de spread (ej: 0.005 = 0.5%) | `0.005` |
| `SPEI_COMMISSION` | Comision fija en MXN | `8.00` |
| `SPEI_PAYMENT_TYPE_ID` | ID del tipo de pago SPEI en Fineract | `3` |
| `DB_URL` | URL JDBC de la base de datos | `jdbc:h2:file:./data/spei;AUTO_SERVER=TRUE` |

## Levantar el servicio

```bash
# Con Maven
mvn spring-boot:run

# O con el JAR compilado
mvn package -DskipTests
java -jar target/spei-api-0.0.1-SNAPSHOT.jar
```

La API escucha en `http://localhost:8080`.

## Endpoints

### POST /api/v1/spei/inbound

Procesa una transferencia SPEI entrante.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
Idempotency-Key: <UUID>  (opcional pero recomendado)
```

**Body:**
```json
{
  "claveRastreo": "SPEI20261001120000",
  "monto": 1000.00,
  "cuentaBeneficiaria": "646180909697341557",
  "concepto": "Abono cliente demo"
}
```

**Respuesta exitosa (201):**
```json
{
  "id": "b7a1c0e2-5f0e-4b0b-9a4c-1f2d3e4a5b6c",
  "claveRastreo": "SPEI20261001120000",
  "cuentaBeneficiaria": "646180909697341557",
  "fineractClientId": 7,
  "fineractClientName": "Cliente SPEI Lab",
  "fineractSavingsId": 12,
  "principal": 1000.00,
  "spread": 5.00,
  "commission": 8.00,
  "fineractDepositTxId": 145,
  "fineractSpreadTxId": 146,
  "fineractCommissionTxId": 147,
  "processedAt": "2026-10-01T18:20:00Z",
  "status": "APPLIED"
}
```

### GET /api/v1/spei/{id}

Recupera una orden SPEI procesada por su UUID.

**Respuesta exitosa (200):** misma estructura que el POST.

## Flujo de prueba (Postman)

1. **Obtener token Keycloak:**
```
POST https://auth.lab.dnu.mx/realms/fineract_keycloak/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=candidate-api
client_secret=candidate-secret
username=candidato01
password=Candidato123!
```

2. **Llamar al endpoint** con el `access_token` del paso anterior como Bearer token.

3. **Repetir** con el mismo `Idempotency-Key` -> devuelve 200 con el mismo resultado (sin depositos nuevos).

4. **Sin token** -> 401.

5. **Verificar movimientos en Fineract:**
```
GET /savingsaccounts/{savingsId}?associations=transactions
GET /journalentries?savingsId={savingsId}&transactionDetails=true
```

## Codigos de error

| Caso | HTTP |
|------|------|
| Sin token / token invalido | 401 |
| Body invalido | 400 |
| CLABE sin cuenta en Fineract | 422 |
| Cuenta inactiva | 422 |
| Error en Fineract | 502 |
| Idempotency-Key en progreso | 409 |
| Idempotency-Key repetida | 200 (resultado original) |

## Calculo de importes

Con `monto = 1000.00`:

| Concepto | Calculo | Resultado |
|----------|---------|-----------|
| Principal | monto | 1000.00 |
| Spread | monto x 0.005 (HALF_UP) | 5.00 |
| Comision | fija configurable | 8.00 |

Los 3 se registran como depositos separados en Fineract con notas:
- `SPEI-IN principal <claveRastreo>`
- `SPEI-IN spread <claveRastreo>`
- `SPEI-IN commission <claveRastreo>`