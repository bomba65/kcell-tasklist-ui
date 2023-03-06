import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.camunda.spin.json.SpinJsonNode

def request_number = execution.processBusinessKey
def priority = execution.getVariable("priority").toString()
def rejectedString = execution.getVariable("rejectedAutomodifyServices")
SpinJsonNode rejectedJsonNode = rejectedString.getValue(SpinJsonNode.class);
String[] rejected = rejectedJsonNode.mapTo(String[].class);


def binding = ["request_number"  : request_number,
               "rejected"  : rejected,
               "priority"  : priority]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "Поступил автоматический запрос # " + request_number + " на расширение VPN, ожидающий решения группы IP Core."
        }
        p { 
            yield "Для просмотра заявки Вам необходимо пройти по следующей ссылкe: "
            b {
                a(href: 'https://flow.kcell.kz', 'https://flow.kcell.kz')
            }
        }
        p {
            span("Статус: " + priority)
        }
        p("При этом в  расширении следующих VPN отказано, ввиду отсутствия технической возможности (требуется увеличение емкости порта):")
        each rejectedItem in rejected {
            p(rejectedItem)
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
