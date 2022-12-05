import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getB2BEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup("hq_leasing").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def main(DelegateExecution execution) {
//    def starterEmails = getStarterEmails(execution)//["asdf@gasdf.coasdf"]
    def btbEmails = getB2BEmails(execution)

    def result = btbEmails.stream().collect(Collectors.joining(","))
    println 'get users:'
    println result
    result
}

main(execution)
//getStarterB2BAddresses(null)
