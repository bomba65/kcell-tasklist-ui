import org.camunda.bpm.engine.delegate.DelegateExecution

def mockExecution(DelegateExecution execution) {
    println 'Mock'
}
mockExecution(execution)
