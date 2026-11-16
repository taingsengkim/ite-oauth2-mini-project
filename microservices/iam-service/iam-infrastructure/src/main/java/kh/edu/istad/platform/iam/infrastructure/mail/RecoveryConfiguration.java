package kh.edu.istad.platform.iam.infrastructure.mail;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
public class RecoveryConfiguration {
    @Bean(name = "passwordRecoveryExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor executor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); executor.setMaxPoolSize(2); executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("password-recovery-");
        return executor;
    }
}
