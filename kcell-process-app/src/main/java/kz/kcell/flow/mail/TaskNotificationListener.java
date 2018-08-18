package kz.kcell.flow.mail;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Log
public class TaskNotificationListener implements TaskListener {
    private static final String[] BCC = {"Yernaz.Kalingarayev@kcell.kz"};
    private String sender;
    private String baseUrl;
    private JavaMailSender mailSender;
    private final CompiledScript template;

    @Autowired
    public TaskNotificationListener(
        @Value("${mail.sender:flow@kcell.kz}") String sender,
        @Value("${mail.message.baseurl:http://localhost}") String baseUrl,
        ScriptEngineManager manager,
        JavaMailSender mailSender
    ) throws ScriptException {
        this.sender = sender;
        this.baseUrl = baseUrl;
        this.mailSender = mailSender;

        ScriptEngine groovy = manager.getEngineByName("groovy");
        InputStreamReader reader = new InputStreamReader(TaskListener.class.getResourceAsStream("/TaskAssigneeNotificationTemplate.groovy"));
        this.template = ((Compilable)groovy).compile(reader);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        final Set<String> recipientEmails = new HashSet<>();
        if (delegateTask.getAssignee()!=null && TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) {
            recipientEmails.addAll(getAssigneeAddresses(delegateTask));
        } else if(delegateTask.getAssignee()==null && TaskListener.EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            recipientEmails.addAll(getCandidateAddresses(delegateTask));
        }

        if (recipientEmails.size() > 0) {

            try {
                String templateName = delegateTask
                    .getExecution()
                    .getProcessEngineServices()
                    .getRepositoryService()
                    .getBpmnModelInstance(delegateTask.getProcessDefinitionId())
                    .getModelElementsByType(Process.class)
                    .stream()
                    .map(Process::getExtensionElements)
                    .filter(Objects::nonNull)
                    .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
                    .flatMap(e -> e.getCamundaProperties().stream())
                    .filter(e -> e.getCamundaName().equals("taskNotificationTemplate"))
                    .map(CamundaProperty::getCamundaValue)
                    .findAny()
                    .orElse("/TaskAssigneeNotificationTemplate.tpl");

                Bindings bindings = template.getEngine().createBindings();
                bindings.put("delegateTask", delegateTask);
                bindings.put("baseUrl", baseUrl);
                bindings.put("templateName", templateName);
                String htmlMessage = String.valueOf(template.eval(bindings));
                String businessKey = delegateTask.getExecution().getProcessBusinessKey();
                String subject = String.format("%s, %s", businessKey, delegateTask.getName());

                mailSender.send(mimeMessage -> {
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                    helper.setTo(recipientEmails.toArray(new String[]{}));
                    helper.setFrom(sender);
                    helper.setSubject(subject);
                    helper.setText(htmlMessage, true);

                    long isRevisionMonthlyActCount =
                        delegateTask
                            .getProcessEngineServices()
                            .getRepositoryService()
                            .createProcessDefinitionQuery()
                            .processDefinitionId(delegateTask.getProcessDefinitionId())
                            .list()
                            .stream()
                            .filter(e-> e.getKey().equals("Revision") || e.getKey().equals("Invoice"))
                            .count();

                    System.out.println("isRevisionMonthlyActCount: " + isRevisionMonthlyActCount);

                    if(isRevisionMonthlyActCount > 0){
                        helper.setBcc(BCC);
                    }
                });
            } catch (ScriptException e) {
                throw new RuntimeException("Could not render mail message", e);
            }

        }
    }

    private static Collection<String> getAssigneeAddresses(DelegateTask delegateTask) {
        Authentication currentAuthentication = delegateTask.getProcessEngineServices().getIdentityService().getCurrentAuthentication();

        IdentityService identityService = delegateTask.getProcessEngineServices().getIdentityService();

        return Stream.of(delegateTask.getAssignee())
            .filter(userId -> currentAuthentication != null && !userId.equals(currentAuthentication.getUserId()))
            .flatMap(userId -> identityService.createUserQuery().userId(userId).list().stream())
            .map(User::getEmail)
            .filter(TaskNotificationListener::validEmail)
            .collect(toList());
    }


    private static Collection<String> getCandidateAddresses(DelegateTask delegateTask) {
        Set<IdentityLink> candidates = delegateTask.getCandidates();

        // Get User Profile from User Management
        IdentityService identityService = delegateTask.getProcessEngineServices().getIdentityService();
        Set<String> recipientsSet = candidates.stream()
            .map(IdentityLink::getGroupId)
            .filter(Objects::nonNull)
            .flatMap(groupId -> identityService.createUserQuery().memberOfGroup(groupId).list().stream())
            .map(User::getEmail)
            .filter(TaskNotificationListener::validEmail)
            .collect(Collectors.toSet());

        recipientsSet.addAll(candidates.stream()
            .map(IdentityLink::getUserId)
            .filter(Objects::nonNull)
            .flatMap(userId -> identityService.createUserQuery().userId(userId).list().stream())
            .map(User::getEmail)
            .filter(TaskNotificationListener::validEmail)
            .collect(Collectors.toSet()));

        return recipientsSet;
    }

    private static boolean validEmail(String email) {
        return email != null && !email.isEmpty();
    }
}
