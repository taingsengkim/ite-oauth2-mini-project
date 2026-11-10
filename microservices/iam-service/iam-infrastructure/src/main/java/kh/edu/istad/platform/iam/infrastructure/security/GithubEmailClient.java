package kh.edu.istad.platform.iam.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class GithubEmailClient {
    private final RestClient client;

    public GithubEmailClient(
            @Value("${iam.oauth2.github.api-base-url:https://api.github.com}") String baseUrl) {
        client = RestClient.builder().baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json").build();
    }

    public String findVerifiedEmail(String accessToken) {
        List<Map<String, Object>> emails;
        try {
            emails = client.get().uri("/user/emails")
                    .headers(headers -> headers.setBearerAuth(accessToken)).retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (HttpClientErrorException.Forbidden exception) {
            return null;
        } catch (RestClientException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_lookup_failed"), "Unable to retrieve GitHub email addresses", exception);
        }
        if (emails == null) return null;
        return emails.stream()
                .filter(value -> Boolean.TRUE.equals(value.get("verified")))
                .filter(value -> value.get("email") instanceof String email && !email.isBlank())
                .sorted((first, second) -> Boolean.compare(
                        Boolean.TRUE.equals(second.get("primary")), Boolean.TRUE.equals(first.get("primary"))))
                .map(value -> (String) value.get("email")).findFirst().orElse(null);
    }
}
