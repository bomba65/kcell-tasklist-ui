import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getTechSpecialistEmail(DelegateExecution execution) {
    def techSpecEmail = execution.getVariable("techSpecEmail").toString()
    techSpecEmail
}

def getStarterEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}
getTechSpecialistEmail(execution)
