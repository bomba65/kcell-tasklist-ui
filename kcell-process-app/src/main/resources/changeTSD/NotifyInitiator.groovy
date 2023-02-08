package changeTSD

import org.camunda.bpm.engine.delegate.DelegateExecution

def getUserEmail(DelegateExecution execution) {
    String initiatorEmail = (String) execution.getVariable("initiatorEmail");
    def result = initiatorEmail;
    result
}

getUserEmail(execution)
