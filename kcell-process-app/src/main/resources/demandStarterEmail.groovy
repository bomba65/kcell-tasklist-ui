import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getStarterEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    starterEmails = identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    def result = starterEmails.stream().collect(Collectors.joining(","))
    println("startEmails =====> ")
    println(result)
    result
}

getStarterEmails(execution)

