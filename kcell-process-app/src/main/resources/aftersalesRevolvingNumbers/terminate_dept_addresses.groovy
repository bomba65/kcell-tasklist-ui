import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getB2BEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup("delivery_pbx_b2b_delivery").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getITEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup("delivery_it_delivery").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}
def getITB2BAddresses(DelegateExecution execution) {
    def itEmails = getITEmails(execution)
    def btbEmails = getB2BEmails(execution)

    btbEmails.addAll(itEmails)
    btbEmails.stream().collect(Collectors.joining(","))
}

getITB2BAddresses(execution)
