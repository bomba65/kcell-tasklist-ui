import org.camunda.bpm.engine.delegate.DelegateExecution
import groovy.json.JsonSlurper

def getUserEmail(DelegateExecution execution, String userId) {
    def responsibleB2BSales = execution.getVariable("responsibleB2BSales").toString()
    def responsibleB2BSalesParse = new JsonSlurper().parseText(responsibleB2BSales)
    println (responsibleB2BSalesParse.email)
    def identityService = execution.processEngineServices.identityService
    def userList = identityService.createUserQuery().userId(userId).list()
    (userList[0]?.email ?: 'test_flow@kcell.kz') + ', ' + responsibleB2BSalesParse.email
}



getUserEmail(execution, starter)
