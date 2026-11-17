package kh.edu.istad.platform.iam.rest.client.controller;

import kh.edu.istad.platform.iam.dto.client.*;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.usecase.client.ManageClientsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/dashboard-api/clients")
@RequiredArgsConstructor
public class DashboardClientController {
    private final ManageClientsUseCase clients;

    @GetMapping public ResponseEntity<PageResult<ManagedClientResult>> list(
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "priorityId", required = false) String priorityId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(clients.list(search, page, size, priorityId));
    }

    @GetMapping("/{id}") public ResponseEntity<ManagedClientResult> get(@PathVariable("id") String id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(clients.get(id));
    }

    @PostMapping public ResponseEntity<IssuedClientResult> create(@RequestBody ClientDefinition definition) {
        var issued = clients.create(definition);
        return ResponseEntity.created(URI.create("/dashboard-api/clients/" + issued.client().id()))
                .cacheControl(CacheControl.noStore()).header("Pragma", "no-cache").body(issued);
    }

    @PutMapping("/{id}") public ResponseEntity<ManagedClientResult> update(@PathVariable("id") String id, @RequestBody ClientDefinition definition) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(clients.update(id, definition));
    }

    @PostMapping("/{id}/secret") public ResponseEntity<IssuedClientResult> rotate(@PathVariable("id") String id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("Pragma", "no-cache").body(clients.rotateSecret(id));
    }

    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        clients.delete(id);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
