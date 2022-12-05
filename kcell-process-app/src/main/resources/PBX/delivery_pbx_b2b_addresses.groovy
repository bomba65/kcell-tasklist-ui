import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getDeliveryPBXb2bEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().memberOfGroup("delivery_pbx_b2b_delivery").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    emails.stream().collect(Collectors.joining(","))
}

getDeliveryPBXb2bEmails(execution)

