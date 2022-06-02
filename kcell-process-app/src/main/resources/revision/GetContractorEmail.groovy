package revision

import org.camunda.bpm.engine.delegate.DelegateExecution

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    String siteRegion = execution.getVariable("siteRegion").toString()
    String contractor = execution.getVariable("contractor").toString()

    def contractorsTitle = ["4": "lse", "6": "alta", "7":"logycom", "8": "arlan", "9": "inter"]

//    if("4".equals(contractor) && "nc".equals(siteRegion)){
//        siteRegion = "astana";
//    }

    def groupName = siteRegion + "_contractor_" + contractorsTitle.get(contractor)
    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def result = userList.stream().collect(Collectors.joining(","))
    result
}

getUserEmail(execution)
