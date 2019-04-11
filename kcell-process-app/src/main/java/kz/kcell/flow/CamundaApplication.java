package kz.kcell.flow;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import kz.kcell.camunda.authentication.plugin.ExternalIdentityProviderPlugin;
import kz.kcell.camunda.authentication.plugin.KcellIdentityProviderPlugin;
import kz.kcell.flow.mail.CamundaMailerDelegate;
import kz.kcell.flow.mail.ProcessNotificationListener;
import kz.kcell.flow.mail.TaskNotificationListener;
import kz.kcell.flow.repository.custom.ReportRepository;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.event.ProcessApplicationEventListenerPlugin;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

import javax.script.ScriptEngineManager;

import java.util.List;

import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_ASSIGNMENT;
import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_COMPLETE;
import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_CREATE;

import static org.camunda.bpm.engine.delegate.ExecutionListener.EVENTNAME_END;

@SpringBootApplication
@ProcessApplication
@IntegrationComponentScan
@EnableIntegration
@Primary
public class CamundaApplication extends SpringBootProcessApplication {

    @Autowired
    TaskNotificationListener taskNotificationListener;

    @Autowired
    ProcessNotificationListener processNotificationListener;

    @Autowired
    TaskHistoryListener taskHistoryListener;

    public static void main(String[] args) {
        JacksonConfigurator.setDateFormatString("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
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

            if(EVENTNAME_COMPLETE.equals(eventName)) {
                taskHistoryListener.notify(delegateTask);
            }
        };
    }

    @Override
    public ExecutionListener getExecutionListener (){
        return delegateExecution -> {

            String eventName = delegateExecution.getEventName();
            if(EVENTNAME_END.equals(eventName) && (delegateExecution.getBpmnModelElementInstance() instanceof StartEvent) && delegateExecution.getParentId() == null){
                processNotificationListener.notify(delegateExecution);
            }
        };
    }

    @Bean
    public ProcessEnginePlugin processApplicationEventListenerPlugin() {
        return new ProcessApplicationEventListenerPlugin();
    }

    @Bean
    public ProcessEnginePlugin defaultTaskPermissionNameSettingsPlugin() {
        return new SpringBootProcessEnginePlugin() {
            @Override
            public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
                processEngineConfiguration.setDefaultUserPermissionForTask(Permissions.TASK_WORK);
                processEngineConfiguration.setRestrictUserOperationLogToAuthenticatedUsers(false);
                processEngineConfiguration.setLoginMaxAttempts(100);
                processEngineConfiguration.setLoginDelayBase(1);
                processEngineConfiguration.setGeneralResourceWhitelistPattern(".+");
            }
        };
    }

    @Bean
    public CamundaMailerDelegate camundaMailer() {
        return new CamundaMailerDelegate();
    }

    @Bean
    public ReportRepository reportRepository() {
        return new ReportRepository();
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
    @ConfigurationProperties(prefix="external.ldap")
    @ConditionalOnProperty(prefix = "external.ldap", name = "enabled")
    public ExternalIdentityProviderPlugin externalIdentityProviderPlugin() {
        ExternalIdentityProviderPlugin plugin = new ExternalIdentityProviderPlugin();
        // Set some defaults
        plugin.setServerUrl("ldaps://vkc-extdc1.ext.kcell.kz");
        plugin.setAcceptUntrustedCertificates(true);
        plugin.setManagerDn("CN=camunda,OU=Service_Accounts,OU=EXTKCELL,DC=ext,DC=kcell,DC=kz");
        plugin.setBaseDn("DC=ext,DC=kcell,DC=kz");
        plugin.setUserSearchBase("OU=EXTKCELL");
        plugin.setUserSearchFilter("(&(objectCategory=organizationalPerson)(objectClass=User)(!(userAccountControl:1.2.840.113556.1.4.803:=2)))");
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

    @Bean
    public AmazonS3 amazonS3(@Value("${minio.url:http://localhost:9000}") String minioUrl,
                             @Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey,
                             @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey){

        AWSCredentials credentials = new BasicAWSCredentials(minioAccessKey, minioSecretKey);

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);

        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
            minioUrl, "us-east-1");

        return AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withClientConfiguration(clientConfig)
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(endpointConfiguration).build();
    }
}
