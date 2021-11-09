package ssu

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    String reason = execution.getVariable("reason").toString()
    def groupName = "rpa_admin"
    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    userList.add("rpa@kcell.kz");
    def result = userList.stream().collect(Collectors.joining(","))
    println 'get users:'
    println result
    result

}

getUserEmail(execution)
