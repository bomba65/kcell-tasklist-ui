import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getB2BEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup("delivery_pbx_b2b_delivery").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}
getB2BEmails(execution)
