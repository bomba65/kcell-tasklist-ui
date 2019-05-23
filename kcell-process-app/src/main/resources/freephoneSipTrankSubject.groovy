import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.spin.impl.json.jackson.JacksonJsonNode

def getSubject(DelegateExecution execution) {
    //def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
    //def businessKey = (String) execution.processBusinessKey
    def bin = (String) execution.getVariable("clientBIN")
    def shortNumber = (String) execution.getVariable('finalIDs')
    def companyName = (String) execution.getVariable("officialClientCompanyName")
    def subject = companyName + " " + bin + "_IVR_" + shortNumber
    subject

}

getSubject(execution)
