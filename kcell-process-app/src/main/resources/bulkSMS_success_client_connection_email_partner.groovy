import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getPartnerAndDeliveryEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    def groupName = "delivery_sms_ivr_b2b_delivery"

    def userList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def delivery = userList.stream().collect(Collectors.joining(","))

    def partner = execution.getVariable("partnerEmail").toString()
    println (partner)
    def techSpec = execution.getVariable("techSpecEmail").toString()
    println (partner)
    def starterEmails = getStarterEmails(execution)
    def result = starterEmails.stream().collect(Collectors.joining(",")) + ', ' + partner + ', ' + techSpec + ', ' + delivery

    println 'get users:'
    println result
    result
}

getPartnerAndDeliveryEmail(execution)
