package kh.edu.istad.platform.iam.web.controller;

import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

@Controller
@RequiredArgsConstructor
public class ConsentPageController {
    private final ClientConsentRepository clients;

    @GetMapping("/oauth2/consent")
    public String consent(@RequestParam("client_id") String clientId,
                          @RequestParam("scope") String scope,
                          @RequestParam("state") String state,
                          Principal principal, Model model) {
        if (state.isBlank() || scope.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var client = clients.findClient(clientId, principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Set<String> requested = new TreeSet<>(Arrays.asList(scope.strip().split("\\s+")));
        if (!client.scopes().containsAll(requested)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        requested.remove("openid");
        model.addAttribute("client", client);
        model.addAttribute("scopes", requested.stream().filter(value -> !client.grantedScopes().contains(value)).toList());
        model.addAttribute("grantedScopes", requested.stream().filter(client.grantedScopes()::contains).toList());
        model.addAttribute("state", state);
        return "consent";
    }
}
