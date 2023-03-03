import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def request_number = execution.processBusinessKey
def priority = execution.getVariable("priority").toString()

def binding = ["request_number"  : request_number,
               "priority"           : priority]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "Автоматический запрос # " + request_number + " на расширение VPN успешно отправлен Провайдеру с целью присвоения NOR."
        }
        p { 
            yield "После получения ответа от Провайдера Специалисту по работе с Провайдерами необходимо пройти по следующей ссылкe: "
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
