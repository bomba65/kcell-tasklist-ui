package kz.kcell.flow;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Log
public class TaskHistoryListener implements TaskListener {

    @Autowired
    IdentityService identityService;

    @Override
    public void notify(DelegateTask delegateTask) {

        // $scope.jobModel.resolutions.push({processInstanceId: $scope.processInstanceId, assignee: $rootScope.authentication.name, assigneeName: $rootScope.authentication.assigneeName, resolution: $scope.regionGroupHeadApprovalTaskResult, comment: $scope.regionGroupHeadApprovalComment, taskId: camForm.taskId, visibility: visibility});

        SpinJsonNode resolutions = delegateTask.<JsonValue>getVariableTyped("resolutions").getValue();

        System.out.println("processInstanceId = " + delegateTask.getProcessInstanceId());
        System.out.println("assignee = " + delegateTask.getAssignee());

        List<User> users = identityService.createUserQuery().userId(delegateTask.getAssignee()).list();
        if(users.size()>0){
            System.out.println("assigneeName = " + users.get(0).getFirstName() + " " + users.get(0).getLastName());
        }
        System.out.println("taskId = " + delegateTask.getId());
        System.out.println("taskName = " + delegateTask.getName());
        System.out.println("taskEndDate = " + (new Date()));
    }
}
