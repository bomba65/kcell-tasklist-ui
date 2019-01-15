import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getITEmils(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup("delivery_it_delivery").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getB2BEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup("delivery_pbx_b2b_delivery").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getITB2BAddresses(DelegateExecution execution) {
    def itEmails = getITEmils(execution)
    def btbEmails = getB2BEmails(execution)

    btbEmails.addAll(itEmails)
    def result = btbEmails.stream().collect(Collectors.joining(","))
    println 'get users:'
    println result
    result
}

getITB2BAddresses(execution)

