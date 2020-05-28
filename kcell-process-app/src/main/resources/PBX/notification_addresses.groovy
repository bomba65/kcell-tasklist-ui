import org.camunda.bpm.engine.delegate.DelegateExecution

def getSalesRepresentativeEmails(DelegateExecution execution) {
    def ts = execution.getVariable("technicalSpecifications").unwrap()
    def result = ts.get('technicalEmail').asText()
    result
}

getSalesRepresentativeEmails(execution)
