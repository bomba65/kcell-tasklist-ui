import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getSalesRepresentativeEmails(DelegateExecution execution) {
    def legalInfo = execution.getVariable("legalInfo").unwrap()
    def repr = ''
    if (legalInfo.get('salesReprId') != null) repr = legalInfo.get('salesReprId').asText()
    def identityService = execution.processEngineServices.identityService

    identityService.createUserQuery().userId(repr).list().stream()
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

def getReprB2BAddresses(DelegateExecution execution) {
    def reprEmails = getSalesRepresentativeEmails(execution)
    def btbEmails = getB2BEmails(execution)

    btbEmails.addAll(reprEmails)
    btbEmails.stream().collect(Collectors.joining(","))
}

getReprB2BAddresses(execution)

