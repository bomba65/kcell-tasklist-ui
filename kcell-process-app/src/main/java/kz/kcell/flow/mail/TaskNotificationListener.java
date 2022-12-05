package kz.kcell.flow.mail;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.ModelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Log
public class TaskNotificationListener implements TaskListener {
    private static final String[] LeasingRolloutBCC = {"Stanislav.Li@kcell.kz"};
    private String sender;
    private String baseUrl;
    private JavaMailSender mailSender;
    private final CompiledScript template;
    private ScriptEngine groovyEngine;

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
        this.groovyEngine = groovy;

        InputStreamReader reader = new InputStreamReader(TaskListener.class.getResourceAsStream("/TaskAssigneeNotificationTemplate.groovy"));
        this.template = ((Compilable)groovy).compile(reader);
    }

    @Override
    public void notify(DelegateTask delegateTask) {

        boolean isRevisionProcess =
            delegateTask
                .getProcessEngineServices()
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(delegateTask.getProcessDefinitionId())
                .list()
                .stream()
                .filter(e-> "Revision".equals(e.getKey()))
                .findAny()
                .isPresent();

        boolean isLeasingProcess =
            delegateTask
                .getProcessEngineServices()
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(delegateTask.getProcessDefinitionId())
                .list()
                .stream()
                .filter(e-> "Leasing".equals(e.getKey()))
                .findAny()
                .isPresent();

        boolean isTsdTnuProcess =
            delegateTask
                .getProcessEngineServices()
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(delegateTask.getProcessDefinitionId())
                .list()
                .stream()
                .filter(e-> "create-new-tsd".equals(e.getKey()))
                .findAny()
                .isPresent();

        final Set<String> recipientEmails = new HashSet<>();
        if (TaskListener.EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            if (delegateTask.getAssignee() != null) recipientEmails.addAll(getAssigneeAddresses(delegateTask, true));
            else recipientEmails.addAll(getCandidateAddresses(delegateTask));
        } else if (delegateTask.getAssignee() != null && TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) {
            recipientEmails.addAll(getAssigneeAddresses(delegateTask, false));
            checkTaskAssignPermission(delegateTask);
        }

        if (recipientEmails.size() > 0) {
            try {
                Collection<Process> processes = delegateTask
                    .getExecution()
                    .getProcessEngineServices()
                    .getRepositoryService()
                    .getBpmnModelInstance(delegateTask.getProcessDefinitionId())
                    .getModelElementsByType(Process.class);

                String templateName = processes
                    .stream()
                    .map(Process::getExtensionElements)
                    .filter(Objects::nonNull)
                    .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
                    .flatMap(e -> e.getCamundaProperties().stream())
                    .filter(e -> e.getCamundaName().equals("taskNotificationTemplate"))
                    .map(CamundaProperty::getCamundaValue)
                    .findAny()
                    .orElse("/TaskAssigneeNotificationTemplate.tpl");

                String subjectScript = processes
                    .stream()
                    .map(Process::getExtensionElements)
                    .filter(Objects::nonNull)
                    .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
                    .flatMap(e -> e.getCamundaProperties().stream())
                    .filter(e -> e.getCamundaName().equals("taskNotificationSubjectScript"))
                    .map(CamundaProperty::getCamundaValue)
                    .findAny()
                    .orElse("default");


                Bindings bindings = groovyEngine.createBindings();
                bindings.put("delegateTask", delegateTask);
                String subject;
                if (subjectScript.equals("default")) {
                    String businessKey = delegateTask.getExecution().getProcessBusinessKey();
                    String processName = delegateTask
                        .getProcessEngineServices()
                        .getRepositoryService()
                        .getProcessDefinition(delegateTask.getProcessDefinitionId())
                        .getName();

                    if(isRevisionProcess){
                        subject = businessKey!=null?String.format("%s - %s - %s", processName, businessKey, delegateTask.getName()):processName;
                    } else if(isTsdTnuProcess){
                        String region_name = delegateTask.getVariable("region_name").toString();
                        Map<String, String> regionsTitle =
                            ((Supplier<Map<String, String>>) (() -> {
                                Map<String, String> map = new HashMap<>();
                                map.put("alm", "Almaty");
                                map.put("astana", "Astana");
                                map.put("nc", "N&C");
                                map.put("east", "East");
                                map.put("south", "South");
                                map.put("west", "West");
                                return Collections.unmodifiableMap(map);
                            })).get();

                        subject = "New TSD from " + regionsTitle.get(region_name) + " region " + String.format("- %s", businessKey);
                    } else {
                        subject = businessKey!=null?String.format("%s - %s", processName, businessKey):processName;
                    }
                } else {
                    InputStreamReader reader = new InputStreamReader(TaskListener.class.getResourceAsStream(subjectScript));
                    subject = String.valueOf(((Compilable)groovyEngine).compile(reader).eval(bindings));
                }

                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                HistoricProcessInstance procInst = delegateTask.getProcessEngineServices()
                    .getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(delegateTask.getProcessInstanceId()).singleResult();

                //TODO: Fix time in Java
                Calendar startTime = Calendar.getInstance();
                if(procInst!=null){
                    startTime.setTime(procInst.getStartTime());
                } else {
                    startTime.setTime(new Date());
                }
                startTime.add(Calendar.HOUR, 6);
                bindings.put("startTime", format.format(startTime.getTime()));

                bindings.put("baseUrl", baseUrl);
                bindings.put("templateName", templateName);
                bindings.put("subject", subject);
                String htmlMessage = String.valueOf(template.eval(bindings));

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

                    if (isLeasingProcess){
                        helper.setBcc(LeasingRolloutBCC);
                    }
                });
            } catch (ScriptException e) {
                throw new RuntimeException("Could not render mail message", e);
            }

        }
    }

    private static Collection<String> getAssigneeAddresses(DelegateTask delegateTask, boolean onlyCurrentUser) {
        Authentication currentAuthentication = delegateTask.getProcessEngineServices().getIdentityService().getCurrentAuthentication();

        IdentityService identityService = delegateTask.getProcessEngineServices().getIdentityService();

        return Stream.of(delegateTask.getAssignee())
            .filter(userId -> currentAuthentication != null && userId.equals(currentAuthentication.getUserId()) == onlyCurrentUser)
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

    // Added to give specific user permission to unclaim
    private void checkTaskAssignPermission(DelegateTask delegateTask){

        Boolean authorizationGranted = false;
        Authorization taskWorkAuthorization = null;

        List<Authorization> taskAuthorizations = delegateTask.getProcessEngineServices().getAuthorizationService().createAuthorizationQuery().resourceId(delegateTask.getId()).userIdIn(delegateTask.getAssignee()).resourceType(7).list();
        if(taskAuthorizations.size()>0){
            for(Authorization authorization: taskAuthorizations){
                if(authorization.isPermissionGranted(Permissions.TASK_ASSIGN)){
                    authorizationGranted = true;
                }
                if(authorization.isPermissionGranted(Permissions.TASK_WORK)){
                    taskWorkAuthorization = authorization;
                }
            }
        }
        if(!authorizationGranted){
            if(taskWorkAuthorization != null){
                taskWorkAuthorization.addPermission(Permissions.TASK_ASSIGN);
                delegateTask.getProcessEngineServices().getAuthorizationService().saveAuthorization(taskWorkAuthorization);
            } else {
                Authorization taskAssignAuth = delegateTask.getProcessEngineServices().getAuthorizationService().createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                taskAssignAuth.setResourceType(7); //Task
                taskAssignAuth.setResourceId(delegateTask.getId());
                taskAssignAuth.addPermission(Permissions.TASK_ASSIGN);
                taskAssignAuth.setUserId(delegateTask.getAssignee());
                delegateTask.getProcessEngineServices().getAuthorizationService().saveAuthorization(taskAssignAuth);
            }
        }
    }

    private static boolean validEmail(String email) {
        return email != null && !email.isEmpty();
    }
}
