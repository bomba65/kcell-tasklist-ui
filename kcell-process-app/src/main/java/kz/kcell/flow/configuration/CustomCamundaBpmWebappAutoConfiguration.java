package kz.kcell.flow.configuration;

import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.camunda.bpm.spring.boot.starter.webapp.CamundaBpmWebappAutoConfiguration;
import org.camunda.bpm.spring.boot.starter.webapp.filter.LazyInitRegistration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(CamundaBpmAutoConfiguration.class)
public class CustomCamundaBpmWebappAutoConfiguration extends CamundaBpmWebappAutoConfiguration {
    @Bean
    public LazyInitRegistration lazyInitRegistration() {
        return new LazyInitRegistration(){
            @EventListener
            protected void onContextClosed(ContextClosedEvent ev) {
                APPLICATION_CONTEXT = null;
            }
        };
    }
}
