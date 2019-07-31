import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getSalesRepresentativeEmails(DelegateExecution execution) {
    def legalInfo = execution.getVariable("legalInfo").unwrap()
    def result = null
    if (legalInfo.get('salesReprId') != null) {
        repr = legalInfo.get('salesReprId').asText()
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

