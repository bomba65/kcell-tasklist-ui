import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getStarterEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    retEmails = null
    if (oldStatus != status) retEmails = emails.stream().collect(Collectors.joining(","))
    retEmails
}

getStarterEmails(execution)

