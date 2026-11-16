package kh.edu.istad.platform.iam.infrastructure.audit;

import kh.edu.istad.platform.iam.audit.Audited;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.audit.AuditLogRepository;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

public final class AuditInterceptor implements MethodInterceptor {
    private final AuditLogRepository repository;
    private final CurrentActorProvider actors;
    private final Clock clock;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    public AuditInterceptor(AuditLogRepository repository, CurrentActorProvider actors, Clock clock) {
        this.repository = repository; this.actors = actors; this.clock = clock;
    }

    @Override public Object invoke(MethodInvocation invocation) throws Throwable {
        var rule = AnnotatedElementUtils.findMergedAnnotation(invocation.getMethod(), Audited.class);
        if (rule == null) return invocation.proceed();
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("Audited operations must run inside the business transaction");
        // Capture the actor before operations such as terminating the current session.
        String actorId = actors.currentActor();
        UUID actorUuid = null;
        try { actorUuid = UUID.fromString(actorId); } catch (IllegalArgumentException ignored) { }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)
                ? authentication.getName() : actorId;
        Object result = invocation.proceed();
        var context = SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build();
        Object[] arguments = invocation.getArguments();
        for (int index = 0; index < arguments.length; index++) context.setVariable("p" + index, arguments[index]);
        context.setVariable("result", result);
        Object target = value(rule.target(), context);
        if (!(target instanceof UUID) && !(target instanceof String)) throw new IllegalStateException("Audit target must be an identifier");
        List<String> related = new ArrayList<>();
        Object ids = value(rule.related(), context);
        if (ids instanceof UUID uuid) related.add(uuid.toString());
        else if (ids instanceof Collection<?> collection) {
            for (Object id : collection) {
                if (!(id instanceof UUID)) throw new IllegalStateException("Audit related identifiers must be UUIDs");
                related.add(id.toString());
            }
        } else if (ids != null) throw new IllegalStateException("Audit related identifiers must be UUIDs");
        List<String> fields = new ArrayList<>();
        if (rule.fields().length > 0) {
            Object input = rule.fieldSource().isEmpty() ? arguments[rule.fieldArgument()] : value(rule.fieldSource(), context);
            for (String field : rule.fields()) if (input.getClass().getMethod(field).invoke(input) != null) fields.add(field);
        }
        Object effect = value(rule.effect(), context);
        if (effect != null && (!(effect instanceof Enum<?>) || !Set.of("GRANT", "DENY").contains(((Enum<?>) effect).name())))
            throw new IllegalStateException("Unsupported audit assignment effect");
        Object expiry = value(rule.expiry(), context);
        if (expiry != null && !(expiry instanceof Instant)) throw new IllegalStateException("Audit expiry must be an instant");
        var attributes = RequestContextHolder.getRequestAttributes();
        var request = attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
        repository.append(new AuditLogRepository.Event(UUID.randomUUID(), clock.instant(), bounded(actor, 255), actorUuid,
                rule.action().code(), rule.action().resource(), bounded(target.toString(), 100), AuditLogRepository.Outcome.SUCCESS,
                request == null ? null : bounded(request.getRemoteAddr(), 64), request == null ? null : bounded(request.getHeader("User-Agent"), 512),
                related, fields, (Instant) expiry, effect == null ? null : ((Enum<?>) effect).name(), null, null));
        return result;
    }
    private Object value(String expression, SimpleEvaluationContext context) { return expression.isEmpty() ? null : parser.parseExpression(expression).getValue(context); }
    private String bounded(String value, int max) {
        if (value == null) return null;
        String text = value.replaceAll("[\\p{Cntrl}]", " ");
        return text.length() > max ? text.substring(0, max) : text;
    }
}
