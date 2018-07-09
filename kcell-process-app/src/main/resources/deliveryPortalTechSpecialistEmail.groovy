import org.camunda.bpm.engine.delegate.DelegateExecution

def getTechSpecialistEmail(DelegateExecution execution) {
    def responsibleB2BSales = execution.getVariable("techSpecEmail").toString()
    println (responsibleB2BSales)
    responsibleB2BSales
}
getTechSpecialistEmail(execution)
