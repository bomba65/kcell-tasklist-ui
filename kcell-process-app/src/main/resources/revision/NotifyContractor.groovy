package revision

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.function.Supplier
import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    String siteRegion = execution.getVariable("siteRegion").toString()
    String contractor = execution.getVariable("contractor").toString()

    def contractorsTitle = ["1": "avrora","2": "aicom", "3": "spectr", "4": "lse", "5": "kcell", "6": "alta", "7":"logycom", "8": "arlan"]

    if("4".equals(contractor) && "nc".equals(siteRegion)){
        siteRegion = "astana";
    }

    def groupName = "5".equals(contractor) ? siteRegion + "_engineer" : siteRegion + "_contractor_" + contractorsTitle.get(contractor)

    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    userList.add('Begaly.Kokin@kcell.kz');
    userList.add('Zhanar.Zhubantayeva@kcell.kz');
    userList.add('Aidana.Abdrakhman@kcell.kz');
    userList.add('Lyudmila.Vilkova@kcell.kz');
    def result = userList.stream().collect(Collectors.joining(","))
    result
}

getUserEmail(execution)
