package revision

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    String reason = execution.getVariable("reason").toString()
    def groupName = ""
    def result = ""
    if(reason.equals("5")) {
        result = "Begaly.Kokin@kcell.kz";
    } else {
        if(reason.equals("1")){
            groupName = "hq_sap_specialist_optimization"
        } else if(reason.equals("2")){
            groupName = "hq_sap_specialist_transmission"
        } else if(reason.equals("3")){
            groupName = "hq_sap_specialist_infrastructure"
        } else if(reason.equals("4")){
            groupName = "hq_sap_specialist_operation"
        }

        def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
                .map{it.getEmail()}
                .filter{it != null && !it.isEmpty()}
                .collect(Collectors.toSet())
        result = userList.stream().collect(Collectors.joining(","))
    }

    println 'get users:'
    println result
    result
}

getUserEmail(execution)
