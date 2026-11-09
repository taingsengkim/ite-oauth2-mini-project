package kh.edu.istad.platform.iam.persistence.config;


import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
    @Bean
    AuditorAware<String> auditorAware(CurrentActorProvider actorProvider) {
        return ()-> Optional.of(actorProvider.currentActor());
    }
}
