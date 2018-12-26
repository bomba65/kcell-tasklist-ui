import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getStarterEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getCVPEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup(cvpReviewGroup).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getStarterB2BAddresses(DelegateExecution execution) {
    def starterEmails = getStarterEmails(execution)
    def cvpEmails = getCVPEmails(execution)

    cvpEmails.addAll(starterEmails)
    def result = cvpEmails.stream().collect(Collectors.joining(","))
    result
}

getStarterB2BAddresses(execution)

