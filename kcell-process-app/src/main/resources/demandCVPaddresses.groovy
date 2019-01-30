import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getCVPEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().memberOfGroup(cvpReviewGroup).list().stream()
            .filter{it != null && !it.id.equals(starter)}
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    emails.stream().collect(Collectors.joining(","))
}

getCVPEmails(execution)

