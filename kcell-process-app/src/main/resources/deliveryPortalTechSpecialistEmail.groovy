import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getTechSpecialistEmail(DelegateExecution execution) {
    def responsibleB2BSales = execution.getVariable("techSpecEmail").toString()
    println (responsibleB2BSales)
    def starterEmails = getStarterEmails(execution)
    def result = starterEmails.stream().collect(Collectors.joining(",")) + ', ' + responsibleB2BSales
    println 'get users:'
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
getTechSpecialistEmail(execution)






