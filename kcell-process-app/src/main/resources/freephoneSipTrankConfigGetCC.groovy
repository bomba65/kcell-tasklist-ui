import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getCcEmails(DelegateExecution execution) {
    def responsibleB2BDelivery = execution.getVariable("responsibleB2BDelivery").toString()
    def responsibleB2BDeliveryParse = new JsonSlurper().parseText(responsibleB2BDelivery)
    def engineerEmail = getStarterEmails(execution).stream().collect(Collectors.joining(","))
    def result = engineerEmail + ', ' + responsibleB2BDeliveryParse.email
    execution.setVariable("mailOne", (engineerEmail.toString()))
    execution.setVariable("mailTwo", (responsibleB2BDeliveryParse.email.toString()))
    def test = execution.getVariable('mailOne')


    println 'get cc users:'
    println result
    result
}

def getStarterEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}
getCcEmails(execution)
