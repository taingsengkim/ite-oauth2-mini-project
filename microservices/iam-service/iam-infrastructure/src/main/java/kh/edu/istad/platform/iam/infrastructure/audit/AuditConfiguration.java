package kh.edu.istad.platform.iam.infrastructure.audit;

import kh.edu.istad.platform.iam.audit.Audited;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.audit.AuditLogRepository;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.time.Clock;

@Configuration
@EnableTransactionManagement(order = 0)
public class AuditConfiguration {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    Advisor auditAdvisor(AuditLogRepository repository, CurrentActorProvider actors, Clock clock) {
        var advisor = new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(null, Audited.class), new AuditInterceptor(repository, actors, clock));
        advisor.setOrder(100);
        return advisor;
    }
}
