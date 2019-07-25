import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getSalesRepresentativeEmails(DelegateExecution execution) {
    def ci = execution.getVariable("customerInformation").unwrap()
    def result = null
    if (ci.get('salesRepresentativeId') != null) {
        repr = ci.get('salesRepresentativeId').asText()
        def identityService = execution.processEngineServices.identityService

        def emails = identityService.createUserQuery().userId(repr).list().stream()
                .map { it.email }
                .filter { it != null && !it.empty }
                .collect(Collectors.toSet())

        result = emails.stream().collect(Collectors.joining(","))
    }
    result
}

getSalesRepresentativeEmails(execution)
