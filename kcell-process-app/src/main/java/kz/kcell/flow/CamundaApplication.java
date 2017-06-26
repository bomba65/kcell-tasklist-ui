package kz.kcell.flow;

import kz.kcell.bpm.MailTaskAssigneeListener;
import kz.kcell.bpm.MailTaskCandidatesListener;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.event.ProcessApplicationEventListenerPlugin;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@SpringBootApplication
@ProcessApplication
public class CamundaApplication extends SpringBootProcessApplication {
    public static void main(String[] args) {
        SpringApplication.run(CamundaApplication.class, args);
    }

    @Override
    public TaskListener getTaskListener() {
        return delegateTask -> {

            /**
             * Listen globally for task assignment events and send an email to assignee
             */
            if (TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) {
                new MailTaskAssigneeListener().notify(delegateTask);
            } else if (TaskListener.EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
                new MailTaskCandidatesListener().notify(delegateTask);
            }
        };
    }

    @Bean
    public static ProcessEnginePlugin processApplicationEventListenerPlugin() {
        return new ProcessApplicationEventListenerPlugin();
    }

    @Bean
    @ConfigurationProperties(prefix="kcell.ldap")
    @ConditionalOnProperty(prefix = "kcell.ldap", name = "enabled")
    public LdapIdentityProviderPlugin ldapIdentityProviderPlugin() {
        return new LdapIdentityProviderPlugin();
    }
}
