package kz.kcell.flow;

import kz.kcell.camunda.authentication.plugin.KcellIdentityProviderPlugin;
import kz.kcell.flow.mail.CamundaMailerDelegate;
import kz.kcell.flow.mail.TaskNotificationListener;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.event.ProcessApplicationEventListenerPlugin;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.camunda.bpm.spring.boot.starter.webapp.CamundaBpmWebappAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.script.ScriptEngineManager;

import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_ASSIGNMENT;
import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_CREATE;

@SpringBootApplication(exclude = CamundaBpmWebappAutoConfiguration.class)
@ProcessApplication
public class CamundaApplication extends SpringBootProcessApplication {

    @Autowired
    TaskNotificationListener taskNotificationListener;

    public static void main(String[] args) {
        JacksonConfigurator.setDateFormatString("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        SpringApplication.run(CamundaApplication.class, args);
    }

    @Override
    public TaskListener getTaskListener() {
        return delegateTask -> {

            /**
             * Listen globally for task assignment events and send an email to assignee
             */
            String eventName = delegateTask.getEventName();

            if (EVENTNAME_CREATE.equals(eventName) || EVENTNAME_ASSIGNMENT.equals(eventName)) {
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
        KcellIdentityProviderPlugin plugin = new KcellIdentityProviderPlugin();
        // Set some defaults
        plugin.setServerUrl("ldaps://ldap.kcell.kz:636/");
        plugin.setAcceptUntrustedCertificates(true);
        plugin.setManagerDn("CN=flow,OU=Special,OU=KCELL,DC=kcell,DC=kz");
        plugin.setBaseDn("DC=kcell,DC=kz");
        plugin.setUserSearchBase("OU=KCELL");
        plugin.setUserSearchFilter("(&(objectClass=user)(objectClass=person)(mail=*)(!(userAccountControl:1.2.840.113556.1.4.803:=2)))");
        plugin.setUserIdAttribute("userPrincipalName");
        plugin.setUserFirstnameAttribute("givenName");
        plugin.setUserLastnameAttribute("sn");
        plugin.setUserEmailAttribute("mail");
        plugin.setUserPasswordAttribute("userpassword");
        plugin.setGroupSearchBase("OU=Workflow,OU=KCELL");
        plugin.setGroupSearchFilter("(objectclass=group)");
        plugin.setGroupIdAttribute("cn");
        plugin.setGroupNameAttribute("cn");
        plugin.setGroupMemberAttribute("member");
        plugin.setSortControlSupported(true);
        plugin.setAuthorizationCheckEnabled(false);
        plugin.setUseSsl(true);

        return plugin;
    }

    @Bean
    public ScriptEngineManager scriptEngineManager() {
        return new ScriptEngineManager();
    }

}
