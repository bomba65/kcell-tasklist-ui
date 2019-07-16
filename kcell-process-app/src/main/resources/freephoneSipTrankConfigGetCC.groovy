import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getCcEmails(DelegateExecution execution) {
    def responsibleB2BDelivery = execution.getVariable("responsibleB2BDelivery").toString()
    def responsibleB2BDeliveryParse = new JsonSlurper().parseText(responsibleB2BDelivery)
    def isBulkSms =  execution
            .getProcessEngineServices()
            .getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionId(execution.getProcessDefinitionId())
            .list()
            .stream()
            .filter({ e -> e.getKey().equals("bulksmsConnectionKAE") })
            .findAny()
            .isPresent();
    def group = ""
    if (isBulkSms) {
        println 'bulk'
        group = "delivery_SMSGW_admin"
    } else  {
        println 'freephone'
        group = "delivery_transmission_network_operations"
    }
    emails = getEmails(execution, group)
    result = ""
    if (!emails.equals("")) {
        result = emails + ", "
    }
    result += responsibleB2BDeliveryParse.email
    execution.setVariable("ccMails", result)
    result
}

def getEmails(DelegateExecution execution, String targetGroup) {
    def identityService = execution.processEngineServices.identityService
    def emails = identityService.createUserQuery().memberOfGroup(targetGroup).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
    emails.stream().collect(Collectors.joining(", "))
}
getCcEmails(execution)
