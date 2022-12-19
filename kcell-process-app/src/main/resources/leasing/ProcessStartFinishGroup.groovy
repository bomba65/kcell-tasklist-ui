import org.camunda.bpm.engine.delegate.DelegateExecution

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def groupName = "notify_rollout";

    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def result = userList.stream().collect(Collectors.joining(","))
    result
}

getUserEmail(execution)
