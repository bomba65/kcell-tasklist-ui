import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getStarterEmails(DelegateExecution execution) {
    def legalInfo = execution.getVariable("legalInfo").unwrap()
    def result = null
    if (starter != null) {
        def identityService = execution.processEngineServices.identityService

        def emails = identityService.createUserQuery().userId(starter).list().stream()
                .map { it.email }
                .filter { it != null && !it.empty }
                .collect(Collectors.toSet())

        result = emails.stream().collect(Collectors.joining(","))
    }
    result
}

getStarterEmails(execution)

