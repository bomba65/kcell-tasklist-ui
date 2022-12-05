import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getStarterEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getPartnerAndDeliveryEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    def groupName = "delivery_sms_ivr_b2b_delivery"

    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def delivery = userList.stream().collect(Collectors.joining(","))

    def techSpec = execution.getVariable("techSpecEmail").toString()
    println (techSpec)
    def starterEmails = getStarterEmails(execution)
    def result = starterEmails.stream().collect(Collectors.joining(",")) + ', ' + techSpec + ', ' + delivery

    println 'get users:'
    println result
    result
}

getPartnerAndDeliveryEmail(execution)
