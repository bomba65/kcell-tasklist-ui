package leasing

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def regionCode = execution.getVariable('regionCode').toString();
    def regionGroup = "";

    if (regionCode.equals("1")) {
        regionGroup = "alm_transmission";
    } else if (regionCode.equals("2")) {
        regionGroup = "nc_transmission";
    } else if (regionCode.equals("3")) {
        regionGroup = "east_transmission";
    } else if (regionCode.equals("4")) {
        regionGroup = "south_transmission";
    } else if (regionCode.equals("5")) {
        regionGroup = "west_transmission";
    } else if (regionCode.equals("0")) {
        regionGroup = "astana_transmission";
    }
    def userList = identityService.createUserQuery().memberOfGroup(regionGroup).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def result = userList.stream().collect(Collectors.joining(","))
    println 'get users:'
    println result
    result
}

getUserEmail(execution)
