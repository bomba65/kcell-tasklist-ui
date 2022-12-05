package revision

import org.camunda.bpm.engine.delegate.DelegateExecution

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    String regionName = execution.getVariable("regionName").toString();
    Map<String, String> map = new HashMap<>();
    map.put("Almaty", "alm");
    map.put("Astana", "astana");
    map.put("N&C", "nc");
    map.put("North & Central", "nc");
    map.put("East", "east");
    map.put("South", "south");
    map.put("West", "west");

    def groupName = map.get(regionName) + "_tn_engineer"
    println(groupName)
    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def result = userList.stream().collect(Collectors.joining(","))
    result
}

getUserEmail(execution)
