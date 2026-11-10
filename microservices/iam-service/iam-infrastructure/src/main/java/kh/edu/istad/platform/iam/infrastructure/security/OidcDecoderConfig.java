package kh.edu.istad.platform.iam.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

@Configuration
public class OidcDecoderConfig {
    @Bean
    JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
        var defaults = new OidcIdTokenDecoderFactory();
        var decoders = new ConcurrentHashMap<ClientRegistration, JwtDecoder>();
        var requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(10_000);
        requests.setReadTimeout(10_000);
        var rest = new RestTemplate(requests);
        rest.setInterceptors(List.of((request, body, execution) ->
                decompress(execution.execute(request, body))));

        return registration -> {
            if (!"telegram".equals(registration.getRegistrationId())) {
                return defaults.createDecoder(registration);
            }
            return decoders.computeIfAbsent(registration, client -> {
                var decoder = NimbusJwtDecoder.withJwkSetUri(client.getProviderDetails().getJwkSetUri())
                        .restOperations(rest).build();
                decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(new OidcIdTokenValidator(client)));
                decoder.setClaimSetConverter(OidcIdTokenDecoderFactory.createDefaultClaimTypeConverter());
                return decoder;
            });
        };
    }

    static ClientHttpResponse decompress(ClientHttpResponse response) throws IOException {
        if (!"gzip".equalsIgnoreCase(response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING))) {
            return response;
        }
        final InputStream body;
        try {
            body = new GZIPInputStream(response.getBody());
        } catch (IOException exception) {
            response.close();
            throw exception;
        }
        var headers = new HttpHeaders();
        headers.putAll(response.getHeaders());
        headers.remove(HttpHeaders.CONTENT_ENCODING);
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        return new ClientHttpResponse() {
            public HttpStatusCode getStatusCode() throws IOException { return response.getStatusCode(); }
            public String getStatusText() throws IOException { return response.getStatusText(); }
            public HttpHeaders getHeaders() { return headers; }
            public InputStream getBody() { return body; }
            public void close() {
                try { body.close(); } catch (IOException ignored) { }
                finally { response.close(); }
            }
        };
    }
}
