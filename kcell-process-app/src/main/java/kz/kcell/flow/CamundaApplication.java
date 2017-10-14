package kz.kcell.flow;

import kz.kcell.flow.mail.CamundaMailerDelegate;
import kz.kcell.camunda.authentication.plugin.KcellIdentityProviderPlugin;
import kz.kcell.flow.mail.TaskNotificationListener;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.event.ProcessApplicationEventListenerPlugin;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.script.ScriptEngineManager;

import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_ASSIGNMENT;
import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_CREATE;

@SpringBootApplication
@ProcessApplication
public class CamundaApplication extends SpringBootProcessApplication {

    @Autowired
    TaskNotificationListener taskNotificationListener;

    public static void main(String[] args) {
        SpringApplication.run(CamundaApplication.class, args);
    }

    @Override
    public TaskListener getTaskListener() {
        return delegateTask -> {

            /**
             * Listen globally for task assignment events and send an email to assignee
             */
            String eventName = delegateTask.getEventName();

            if (EVENTNAME_ASSIGNMENT.equals(eventName) ||  EVENTNAME_CREATE.equals(eventName)) {
                taskNotificationListener.notify(delegateTask);
            }
        };
    }

    @Bean
    public ProcessEnginePlugin processApplicationEventListenerPlugin() {
        return new ProcessApplicationEventListenerPlugin();
    }

    @Bean
    public CamundaMailerDelegate camundaMailer() {
        return new CamundaMailerDelegate();
    }

    @Bean
    @ConfigurationProperties(prefix="kcell.ldap")
    @ConditionalOnProperty(prefix = "kcell.ldap", name = "enabled")
    public KcellIdentityProviderPlugin kcellIdentityProviderPlugin() {
        return new KcellIdentityProviderPlugin();
    }

    @Bean
    public ScriptEngineManager scriptEngineManager() {
        return new ScriptEngineManager();
    }
}
