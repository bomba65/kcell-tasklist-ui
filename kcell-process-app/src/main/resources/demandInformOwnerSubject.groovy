import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.spin.impl.json.jackson.JacksonJsonNode

def getSubject(DelegateExecution execution) {
    def businessKey = (String) execution.getVariable("businessKey")
    def resolutions = ((JacksonJsonNode) execution.getVariable("resolutions")).unwrap()
    def lastResolution = resolutions[0]
    def subject = businessKey + ", "
    def resolution = lastResolution.get("resolution").asText()
    if (resolution == 'Approve') resolution = 'Approved'
    else if (resolution == 'Cancel') resolution = 'Canceled'
    else if (resolution == 'Finish') resolution = 'Finished'
    else resolution = 'Approved conditionally'
    subject += resolution + " on " + lastResolution.get("taskName").asText()
    subject
}

getSubject(execution)
