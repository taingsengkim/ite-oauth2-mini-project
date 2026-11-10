package kh.edu.istad.platform.iam.infrastructure.security;

import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class OidcDecoderConfigTest {
    @Test
    void decompressesSigningKeysBeforeJsonParsing() throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(bytes)) {
            gzip.write("{\"keys\":[]}".getBytes(StandardCharsets.UTF_8));
        }
        var compressed = new MockClientHttpResponse(bytes.toByteArray(), 200);
        compressed.getHeaders().set(HttpHeaders.CONTENT_ENCODING, "gzip");
        compressed.getHeaders().setContentLength(bytes.size());
        try (var response = OidcDecoderConfig.decompress(compressed)) {
            assertTrue(JWKSet.parse(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8))
                    .getKeys().isEmpty());
            assertFalse(response.getHeaders().containsHeader(HttpHeaders.CONTENT_ENCODING));
            assertFalse(response.getHeaders().containsHeader(HttpHeaders.CONTENT_LENGTH));
        }
    }

    @Test
    void leavesUncompressedResponsesUnchanged() throws Exception {
        var response = new MockClientHttpResponse("{\"keys\":[]}".getBytes(StandardCharsets.UTF_8), 200);
        assertSame(response, OidcDecoderConfig.decompress(response));
    }
}
