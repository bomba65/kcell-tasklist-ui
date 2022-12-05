import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getB2BEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().memberOfGroup("delivery_pbx_sbc_technical_dept").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    emails.stream().collect(Collectors.joining(","))
}

getB2BEmails(execution)
