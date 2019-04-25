package kz.kcell.flow.mail;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Log
public class ProcessNotificationListener implements ExecutionListener {

    private String sender;
    private String baseUrl;
    private JavaMailSender mailSender;
    private final CompiledScript template;
    private ScriptEngine groovyEngine;
    private final List<String> enabledProcesses = Arrays.asList("Revision", "Invoice");

    @Autowired
    public ProcessNotificationListener(
        @Value("${mail.sender:flow@kcell.kz}") String sender,
        @Value("${mail.message.baseurl:http://localhost}") String baseUrl,
        ScriptEngineManager manager,
        JavaMailSender mailSender
    ) throws ScriptException {
        this.sender = sender;
        this.baseUrl = baseUrl;
        this.mailSender = mailSender;

        ScriptEngine groovy = manager.getEngineByName("groovy");
        this.groovyEngine = groovy;

        InputStreamReader reader = new InputStreamReader(ExecutionListener.class.getResourceAsStream("/ProcessCreateNotificationTemplate.groovy"));
        this.template = ((Compilable)groovy).compile(reader);
    }


    @Override
    public void notify(DelegateExecution delegateExecution) {

        final Set<String> recipientEmails = new HashSet<>();

        IdentityService identityService = delegateExecution.getProcessEngineServices().getIdentityService();
        String starter = identityService.getCurrentAuthentication().getUserId();

        if(starter!=null) {
            recipientEmails.addAll(getInitiatorAddress(identityService, starter));

            boolean isEnabledProcess = delegateExecution
                .getProcessEngineServices()
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(delegateExecution.getProcessDefinitionId())
                .list()
                .stream()
                .filter(e -> enabledProcesses.contains(e.getKey()))
                .findAny()
                .isPresent();

            if (recipientEmails.size() > 0 && isEnabledProcess) {
                try {
                    Collection<Process> processes = delegateExecution
                        .getProcessEngineServices()
                        .getRepositoryService()
                        .getBpmnModelInstance(delegateExecution.getProcessDefinitionId())
                        .getModelElementsByType(Process.class);

                    String templateName = processes
                        .stream()
                        .map(Process::getExtensionElements)
                        .filter(Objects::nonNull)
                        .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
                        .flatMap(e -> e.getCamundaProperties().stream())
                        .filter(e -> e.getCamundaName().equals("processCreateNotificationTemplate"))
                        .map(CamundaProperty::getCamundaValue)
                        .findAny()
                        .orElse("/ProcessCreateNotificationTemplate.tpl");

                    Bindings bindings = groovyEngine.createBindings();
                    bindings.put("delegateExecution", delegateExecution);

                    String businessKey = delegateExecution.getProcessBusinessKey();
                    String processName = delegateExecution
                        .getProcessEngineServices()
                        .getRepositoryService()
                        .getProcessDefinition(delegateExecution.getProcessDefinitionId())
                        .getName();
                    String subject = businessKey != null ? String.format("%s - %s", processName, businessKey) : processName;

                    List<User> user = identityService.createUserQuery().userId(starter).list();
                    if (!user.isEmpty()) {
                        starter = user.get(0).getFirstName() + " " + user.get(0).getLastName();
                    }

                    bindings.put("baseUrl", baseUrl);
                    bindings.put("templateName", templateName);
                    bindings.put("subject", subject);
                    bindings.put("starter", starter);
                    String htmlMessage = String.valueOf(template.eval(bindings));

                    mailSender.send(mimeMessage -> {
                        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                        helper.setTo(recipientEmails.toArray(new String[]{}));
                        helper.setFrom(sender);
                        helper.setSubject(subject);
                        helper.setText(htmlMessage, true);
                    });
                } catch (ScriptException e) {
                    throw new RuntimeException("Could not render mail message", e);
                }
            }
        }
    }

    private static Collection<String> getInitiatorAddress(IdentityService identityService, String user) {
        return Stream.of(user)
            .flatMap(userId -> identityService.createUserQuery().userId(user).list().stream())
            .map(User::getEmail)
            .filter(ProcessNotificationListener::validEmail)
            .collect(toList());
    }

    private static boolean validEmail(String email) {
        return email != null && !email.isEmpty();
    }
}
