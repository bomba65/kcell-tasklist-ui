import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getITEmils(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().memberOfGroup("delivery_it_delivery").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    def result = emails.stream().collect(Collectors.joining(","))
    result
}

getITEmils(execution)

