import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def request_number = execution.processBusinessKey
def priority = execution.getVariable("priority").toString()
def approverComments = execution.getVariable("approverComments").toString()

def binding = ["request_number"  : request_number,
               "priority"           : priority,
               "approverComments"  : approverComments]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "Автоматический запрос # " + request_number + " на расширение VPN отправлен на корректировку по следующей причине: " + approverComments
        }
        p { 
            yield "Для просмотра заявки Вам необходимо пройти по следующей ссылкe: "
            b {
                a(href: 'https://flow.kcell.kz', 'https://flow.kcell.kz')
            }
        }
        br()
        p {
            span("Статус: " + priority)
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
