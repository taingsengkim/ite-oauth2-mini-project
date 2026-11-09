package kh.edu.istad.platform.iam.infrastructure.audit;


import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import org.springframework.stereotype.Component;

//@Component
public class BootstrapCurrentActorProvider implements CurrentActorProvider {

    @Override
    public String currentActor() {
        return "System";
    }
}
