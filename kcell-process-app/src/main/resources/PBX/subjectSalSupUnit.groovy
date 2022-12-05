
import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.camunda.bpm.engine.delegate.DelegateExecution


def generateSubject(DelegateExecution delegateExecution){
    def processName = delegateExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateExecution.getProcessDefinitionId()).getName()
    def businessKey = delegateExecution.processBusinessKey
    def result = processName + " " + businessKey

    result
}

generateSubject(execution)

