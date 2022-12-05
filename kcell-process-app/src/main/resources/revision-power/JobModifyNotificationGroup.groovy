package revision

import org.camunda.bpm.engine.delegate.DelegateExecution

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    def groupName =["power_budget","power_hq"];
    def userLists=new ArrayList();
    for (i in 0..<groupName.size()) {
        def userList = identityService.createUserQuery().memberOfGroup(groupName.get(i)).list().stream()
                .map{it.getEmail()}
                .filter{it != null && !it.isEmpty()}
                .collect(Collectors.toSet())
        userLists.addAll(userList);
    }
    def result = userLists.stream().collect(Collectors.joining(","))
    result
}

getUserEmail(execution)

