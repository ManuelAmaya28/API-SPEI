package mx.com.lab.spei.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Spring configuration for the Fineract {@link WebClient} bean.
 *
 * <p>Satisfies requirements 4.4 (Basic Auth + Tenant header on every request)
 * and 6.4 (connection and response timeouts for Fineract calls).
 *
 * <ul>
 *   <li>Connection timeout: 5 s (Netty {@code ChannelOption.CONNECT_TIMEOUT_MILLIS})</li>
 *   <li>Response timeout:  30 s ({@code HttpClient.responseTimeout})</li>
 *   <li>Default headers:  {@code Authorization: Basic …}, {@code Fineract-Platform-TenantId},
 *       {@code Content-Type: application/json}, {@code Accept: application/json}</li>
 * </ul>
 */
@Configuration
public class WebClientConfig {

    /**
     * Builds the pre-configured {@link WebClient} used by {@code FineractClientImpl}.
     *
     * @param props externalized SPEI configuration (Fineract credentials, base URL, tenant)
     * @return a fully configured {@link WebClient} instance
     */
    @Bean
    public WebClient fineractWebClient(SpeiProperties props) {
        // 1. Encode Basic Auth credentials (username:password → Base64)
        String credentials = Base64.getEncoder().encodeToString(
                (props.getFineract().getUsername() + ":" + props.getFineract().getPassword())
                        .getBytes(StandardCharsets.UTF_8));

        // 2. Configure Netty HttpClient with connection + response timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(30));

        // 3. Build WebClient with base URL, timeouts, and default headers
        return WebClient.builder()
                .baseUrl(props.getFineract().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .defaultHeader("Fineract-Platform-TenantId", props.getFineract().getTenantId())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
