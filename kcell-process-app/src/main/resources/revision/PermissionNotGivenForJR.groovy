package revision

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    String reason = execution.getVariable("reason").toString()
    String siteRegion = execution.getVariable("siteRegion").toString()
    def groupName = ""

    if(reason.equals("1")){
        groupName = "hq_optimization"
    } else if(reason.equals("2")){
        groupName = "hq_transmission_engineer"
    } else if(reason.equals("3")){
        groupName = "hq_infrastructure"
    } else if(reason.equals("4")){
        groupName = "hq_operation"
    }

    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())

    if (reason.equals("1")) {
        groupName = siteRegion + "_optimization_head"
    } else if (reason.equals("2")) {
        groupName = siteRegion + "_transmission_head"
    } else if (reason.equals("3")) {
        groupName = siteRegion + "_infrastructure_head"
    } else if (reason.equals("4")) {
        groupName = siteRegion + "_operation_head"
    }

    userList.addAll(identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet()))
    def result = userList.stream().collect(Collectors.joining(","))
    println 'get users:'
    println result
    result
}

getUserEmail(execution)
