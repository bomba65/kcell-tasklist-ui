import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors



def getEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    def groupName = "delivery_enterprise_data_network"

    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def delivery = userList.stream().collect(Collectors.joining(","))

    delivery
}

getEmails(execution)