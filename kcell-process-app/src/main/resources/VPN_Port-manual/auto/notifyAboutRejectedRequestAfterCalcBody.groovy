import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.camunda.spin.json.SpinJsonNode

def request_number = execution.processBusinessKey
def rejectedString = execution.getVariable("rejectedAutomodifyServices").toString()
SpinJsonNode rejectedJsonNode = rejectedString.getValue(SpinJsonNode.class);
String[] rejected = new JsonSlurper().parseText(rejectedJsonNode);


def binding = ["request_number"  : request_number,
               "rejected"  : rejected]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "Автоматический запрос # " + request_number + " на расширение следующих VPN отклонен:"
        }
        each rejectedItem in rejected {
            p(rejectedItem)
        }
        p {
            yield "В расширение VPN отказано, ввиду отсутствия технической возможности (требуется увеличение емкости порта)."
        }
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
