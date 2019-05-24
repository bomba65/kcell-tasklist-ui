import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.spin.impl.json.jackson.JacksonJsonNode

def getSubject(DelegateExecution execution) {
    def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
    //def businessKey = (String) execution.processBusinessKey
    def bin = (String) execution.getVariable("clientBIN")
    def shortNumber = (String) execution.getVariable('finalIDs')

    def companyName = (String) execution.getVariable("officialClientCompanyName")
    def process = ""
    if (processName.contains('SMS')) {
        process="_SMS_"
    }
    else {
        process="_IVR_"
    }
    def subject = companyName + " " + bin + process + shortNumber
    subject

}

getSubject(execution)
