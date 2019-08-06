import org.camunda.bpm.engine.delegate.DelegateExecution

def getSubject(DelegateExecution execution) {
    def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
    def businessKey = (String) execution.getVariable("businessKey")
    processName + " - " + businessKey
}

getSubject(execution)
