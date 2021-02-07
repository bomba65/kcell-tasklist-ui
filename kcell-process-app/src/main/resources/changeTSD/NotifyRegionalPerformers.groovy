package changeTSD

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.function.Supplier
import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    String regionName = execution.getVariable("regionName").toString()
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().memberOfGroup(regionName + "_tn_engineer").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    emails.stream().collect(Collectors.joining(","))
}

getUserEmail(execution)
