package kh.edu.istad.platform.iam.rest.overview.controller;

import kh.edu.istad.platform.iam.usecase.overview.GetOverviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardOverviewController {
    private final GetOverviewUseCase getOverview;
    @GetMapping("/dashboard-api/overview")
    public ResponseEntity<GetOverviewUseCase.Result> get() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(getOverview.execute());
    }
}
