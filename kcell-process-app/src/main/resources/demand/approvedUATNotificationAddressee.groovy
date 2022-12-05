import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getRequirementManagerEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    emails.stream().collect(Collectors.joining(","))
}

getRequirementManagerEmail(execution)

