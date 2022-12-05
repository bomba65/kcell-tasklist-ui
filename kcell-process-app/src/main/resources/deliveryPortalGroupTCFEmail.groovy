import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.function.Supplier
import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    def groupName = "delivery_TCF_notification"

    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def result = userList.stream().collect(Collectors.joining(","))

    println 'get users:'
    println result
    result
}

getUserEmail(execution)
