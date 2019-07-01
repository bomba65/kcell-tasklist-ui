import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getCcEmails(DelegateExecution execution) {
    def responsibleB2BDelivery = execution.getVariable("responsibleB2BDelivery").toString()
    def responsibleB2BDeliveryParse = new JsonSlurper().parseText(responsibleB2BDelivery)
    def engineerEmail = getStarterEmails(execution)
    def result = engineerEmail + ', ' + responsibleB2BDeliveryParse.email
    execution.setVariable("mailOne", (engineerEmail.toString()))
    execution.setVariable("mailTwo", (responsibleB2BDeliveryParse.email.toString()))
    result
}

def getStarterEmails(DelegateExecution execution) {
    def assignee = execution.processEngineServices
            .getTaskService().createTaskQuery()
            .processInstanceId(execution.getProcessInstanceId()).singleResult().getAssignee()
    def identityService = execution.processEngineServices.identityService
    def user = identityService.createUserQuery().userId(assignee).singleResult()
    user.getEmail()
}
getCcEmails(execution)
