package kz.kcell.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Log
public class TaskHistoryListener implements TaskListener {

    @Autowired
    IdentityService identityService;

    private final List<String> enabledProcesses = Arrays.asList("leasing", "freephone", "bulksmsConnectionKAE", "AftersalesPBX", "revolvingNumbers", "after-sales-ivr-sms", "BulkSMS_disconnection", "changeConnectionType", "ivr_disconnection", "SiteSharingTopProcess", "BeelineHostBeelineSite", "KcellHostBeelineSite", "KcellHostKcellSite", "BeelineHostKcellSite", "Demand", "UAT");

    @Override
    public void notify(DelegateTask delegateTask) {

        List<ProcessDefinition> definitions =
            delegateTask
                .getProcessEngineServices()
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(delegateTask.getProcessDefinitionId())
                .list();

        boolean isEnabledBackendResolution =
                definitions.stream()
                .filter(e-> enabledProcesses.contains(e.getKey()))
                .findAny()
                .isPresent();

        if(isEnabledBackendResolution){
            SpinList<SpinJsonNode> resolutions = delegateTask.<JsonValue>getVariableTyped("resolutions").getValue().elements();

            ObjectMapper mapper = new ObjectMapper();

            ObjectNode resolution = mapper.createObjectNode();
            resolution.put("processInstanceId", delegateTask.getProcessInstanceId());
            resolution.put("assignee", delegateTask.getAssignee());
            List<User> users = identityService.createUserQuery().userId(delegateTask.getAssignee()).list();
            if(users.size()>0){
                resolution.put("assigneeName", users.get(0).getFirstName() + " " + users.get(0).getLastName());
            }
            resolution.put("resolution", checkVariable(delegateTask,delegateTask.getTaskDefinitionKey() + "TaskResult") ? String.valueOf(delegateTask.getVariable(delegateTask.getTaskDefinitionKey() + "TaskResult")) : "");
            resolution.put("comment", checkVariable(delegateTask,delegateTask.getTaskDefinitionKey() + "TaskComment") ? String.valueOf(delegateTask.getVariable(delegateTask.getTaskDefinitionKey() + "TaskComment")) : "");
            resolution.put("taskId", delegateTask.getId());
            resolution.put("taskName", delegateTask.getName());
            resolution.put("taskEndDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX").format(new Date()));
           // resolution.put("taskStartDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX").format(delegateTask.getCreateTime()));

            if(checkVariable(delegateTask,delegateTask.getTaskDefinitionKey() + "Files")) {
                JSONArray filesJSONArray = new JSONArray(String.valueOf(delegateTask.getVariable(delegateTask.getTaskDefinitionKey() + "Files")));
                resolution.putPOJO("files", filesJSONArray);
            }

            Date assignDate = new Date();
            Date claimDate = new Date();
            if (checkVariable(delegateTask,delegateTask.getTaskDefinitionKey() + "TaskAssignDate")) {
                assignDate = (Date) delegateTask.getVariable(delegateTask.getTaskDefinitionKey() + "TaskAssignDate");
                delegateTask.removeVariable(delegateTask.getTaskDefinitionKey() + "TaskAssignDate");
            }
            if (checkVariable(delegateTask,delegateTask.getTaskDefinitionKey() + "TaskClaimDate")) {
                claimDate = (Date) delegateTask.getVariable(delegateTask.getTaskDefinitionKey() + "TaskClaimDate");
                delegateTask.removeVariable(delegateTask.getTaskDefinitionKey() + "TaskClaimDate");
            }
            resolution.put("assignDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX").format(assignDate));
            resolution.put("claimDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX").format(claimDate));

            if (checkVariable(delegateTask,delegateTask.getTaskDefinitionKey() + "TaskAttachments")) {
                resolution.putPOJO("attachments", delegateTask.getVariable(delegateTask.getTaskDefinitionKey() + "TaskAttachments"));
                delegateTask.removeVariable(delegateTask.getTaskDefinitionKey() + "TaskAttachments");
            }

            JsonValue jsonValue = SpinValues.jsonValue(resolution.toString()).create();

            resolutions.add(jsonValue.getValue());

            delegateTask.setVariable("resolutions", SpinValues.jsonValue(resolutions.toString()));
        }
    }

    private boolean checkVariable(DelegateTask delegateTask, String variable){
        return delegateTask.hasVariable(variable) && delegateTask.getVariable(variable)!=null;
    }
}
