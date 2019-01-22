package revision

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.function.Supplier
import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    String siteRegion = execution.getVariable("siteRegion").toString()
    String contractor = execution.getVariable("contractor").toString()

    def contractorsTitle = ["1": "avrora","2": "aicom", "3": "spectr", "4": "lse", "5": "kcell"]

    def groupName = ("nc".equals(siteRegion)?"astana":siteRegion) + "_contractor_" + contractorsTitle.get(contractor)

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
