import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
def mailOne = execution.getVariable("mailOne")
def mailTwo = execution.getVariable("mailTwo")

def binding = [
        "processName": processName,
        "finalIDs": finalIDs,
        "mailOne" :mailOne,
        "mailTwo" :mailTwo,
]
def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Здравствуйте,')
        newLine()
        p('Вам пришло письмо от технического департамента АО “Kcell”. Заявка на подключение услуги Freephone IVR была обработана. Вам был предоставлен тестовый доступ, транк в Вашу сторону направлен.')
        hr()
        p('<b>Наши параметры:</b>')
        table(style: 'text-align:left;') {
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>IP:</b> ')
                td('195.47.255.119')
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Формат номера:</b> ')
                td(finalIDs)
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>SIP Port:</b> ')
                td('5060')
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Кодеки:</b> ')
                td('G711 A-law, G729')
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>DTMF:</b> ')
                td('RFC 2833')
            }
        }
        hr()
        p('На стороне АО “Kcell” используется NAT, просьба учесть это при Ваших настройках.')
        p('Регистрация на стороне АО “Kcell” не поддерживается, соединение будет транком.')
        hr()
        p('Данное сообщение было  сгенерировано автоматически. По всем вопросам обращаться к ' + mailOne + ', '  + mailTwo )
       
        newLine()
        newLine()
        p('С Уважением,')
        newLine()
        p('Delivery Support Team')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
