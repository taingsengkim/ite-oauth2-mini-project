package kh.edu.istad.platform.iam.usecase.overview;

import kh.edu.istad.platform.iam.port.out.overview.OverviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GetOverviewUseCase {
    private final OverviewRepository repository;
    private final Clock clock;
    public record Result(OverviewRepository.Statistics statistics, Instant generatedAt) {}
    @Transactional(readOnly = true)
    public Result execute() { return new Result(repository.statistics(), clock.instant()); }
}
