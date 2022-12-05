package revision

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    def historyService = execution.processEngineServices.historyService
    def identityService = execution.processEngineServices.identityService
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
                    .collect(Collectors.joining(","))
    result
}

getUserEmail(execution)
