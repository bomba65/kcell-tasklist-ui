
import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.camunda.bpm.engine.delegate.DelegateExecution


def generateSubject(DelegateExecution execution){
    def ci = execution.getVariable("customerInformation").unwrap()
    def ts = execution.getVariable("technicalSpecifications").unwrap()
    def connectionPoint = ts.get("connectionPoint").asText()
    def result;

    if (connectionPoint == 'SIP Proxy'){
        result = 'Тестирование SIP-номеров для '
    } else {
        result = 'Бизнес-телефония '
    }

    result += ci.get('legalName').asText()

    result
}

generateSubject(execution)

