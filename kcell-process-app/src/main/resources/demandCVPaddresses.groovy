import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getCVPEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    emails = identityService.createUserQuery().memberOfGroup(cvpReviewGroup).list().stream()
            .filter{it != null && !it.id.equals(starter)}
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    def result = cvpEmails.stream().collect(Collectors.joining(","))
    result
}

getCVPEmails(execution)

