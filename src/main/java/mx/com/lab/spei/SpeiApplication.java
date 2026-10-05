package mx.com.lab.spei;

import mx.com.lab.spei.config.SpeiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the SPEI Inbound API.
 *
 * <p>This service processes simulated inbound SPEI transfers by:
 * <ol>
 *   <li>Authenticating callers via Keycloak JWT.</li>
 *   <li>Validating the request body (including CLABE check-digit).</li>
 *   <li>Enforcing idempotency with an {@code Idempotency-Key} header.</li>
 *   <li>Looking up the beneficiary savings account in Apache Fineract.</li>
 *   <li>Recording three deposit transactions (principal, spread, commission).</li>
 *   <li>Persisting the resulting SPEI order and returning HTTP 201.</li>
 * </ol>
 */
@SpringBootApplication
@EnableConfigurationProperties(SpeiProperties.class)
public class SpeiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpeiApplication.class, args);
    }
}
