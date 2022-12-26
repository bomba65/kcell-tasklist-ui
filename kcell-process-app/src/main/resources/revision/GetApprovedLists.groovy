package revision

import org.camunda.bpm.engine.delegate.DelegateExecution

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def historyService = execution.processEngineServices.historyService
    def identityService = execution.processEngineServices.identityService
    String siteRegion = execution.getVariable("siteRegion").toString();
    String contractor = execution.getVariable("contractor").toString();
    def contractorsTitle = ["1":"avrora", "2":"aicom", "3":"spectr", "4": "lse", "5":"kcell", "6": "alta", "7":"logycom", "8": "arlan", "9": "inter", "10":"foresterhg", "11":"transtlc"]
    def groupName = siteRegion + "_contractor_" + contractorsTitle.get(contractor)
    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())

    def assignees = historyService.createHistoricTaskInstanceQuery().processInstanceId(execution.processInstanceId).list().stream()
                    .map{it.assignee}
                    .filter {it!=null && !it.isEmpty()}
                    .collect(Collectors.toSet())
    assignees.add("Begaly.Kokin@kcell.kz");
    assignees.add("Zhanar.Zhubantayeva@kcell.kz");
    assignees.add("Aidana.Abdrakhman@kcell.kz");
    assignees.add("Lyudmila.Vilkova@kcell.kz");
    def result = assignees.stream()
                    .flatMap{identityService.createUserQuery().userId(it).list().stream()}
                    .map{it.getEmail()}
                    .filter{it != null && !it.isEmpty()}
                    .collect(Collectors.joining(",")) + "," + userList.stream().collect(Collectors.joining(","))
    result
}

getUserEmail(execution)
