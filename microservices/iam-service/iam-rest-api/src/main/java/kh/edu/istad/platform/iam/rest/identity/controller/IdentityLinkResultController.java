package kh.edu.istad.platform.iam.rest.identity.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** Contains no identity data; the opener confirms the result through its authenticated API. */
@RestController
public class IdentityLinkResultController {
    @GetMapping(value = "/account/identities/result", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> result() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("Content-Security-Policy", "default-src 'none'; script-src 'self'; base-uri 'none'; frame-ancestors 'none'")
                .body("""
                    <!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Linked accounts</title><script src="/assets/identity-link-result.js" defer></script></head>
                    <body><p>You can close this window and return to Linked accounts to check the result.</p><p lang="km">សូមបិទផ្ទាំងនេះ ហើយត្រឡប់ទៅគណនីដែលបានភ្ជាប់ ដើម្បីពិនិត្យលទ្ធផល។</p><a href="/dashboard/index.html?view=accounts">Linked accounts</a></body></html>
                    """);
    }
}
