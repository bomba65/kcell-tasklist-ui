import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.spin.impl.json.jackson.JacksonJsonNode

def getSubject(DelegateExecution execution) {
    def businessKey = (String) execution.getVariable("businessKey")
    def status = (String) execution.getVariable("status")
    def resolutions = ((JacksonJsonNode) execution.getVariable("resolutions")).unwrap()
    def lastResolution = resolutions[0]
    def subject = "Demand - " + businessKey + " - Order status changed to " + status + " on " + lastResolution.get("taskName").asText()
    if (status == "New order") subject = "Demand - " + businessKey + " - Order created"
    subject
}

getSubject(execution)
