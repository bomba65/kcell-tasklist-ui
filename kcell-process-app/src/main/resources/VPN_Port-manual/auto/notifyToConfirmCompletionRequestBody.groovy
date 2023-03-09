import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import kz.kcell.flow.GroovyDebugger

def request_number = execution.processBusinessKey
def priority = execution.getVariable("priority").toString()
def automodifyServices = execution.getVariable("automodifyServices").toString()
def automodifyServicesList = new JsonSlurper().parseText(automodifyServices)
def vpnNumbers = automodifyServicesList.findAll { it.provider_confirmed == false }.collect { it.vpn_number }

def binding = ["request_number": request_number,
               "vpn_numbers"   : vpnNumbers,
               "priority"      : priority]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "Автоматический запрос # " + request_number + " на расширение VPN одобрен Провайдером и отправлен в работу."
        }
        p { 
            yield "Для завершения заявки необходимо пройти по следующей ссылкe: "
            b {
                a(href: 'https://flow.kcell.kz', 'https://flow.kcell.kz')
            }
        }
        br()
        p {
            span("Статус: " + priority)
        }
        br()
        if (vpn_numbers.size() > 0) {
            p {
                yield "При этом в расширении следующих VPN отказано, ввиду отсутствия технической возможности:"
            }
            ul {
                vpn_numbers.each {
                    li {
                        yield it
                    }
                }
            }
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
