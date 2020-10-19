import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
def ccMails = execution.getVariable("ccMails")
def timeBounds = ""
if (connectionType == "rest") {
    connectionType = "REST"
    timeBounds = "ad99"
} else if (connectionType == "smpp") {
    connectionType = "SMPP"
    timeBounds = "full"
}
def binding = [
        "processName"   : processName,
        "finalIDs"      : finalIDs,
        "ccMails"       : ccMails,
        "connectionType": connectionType,
        "clientLogin"   : clientLogin,
        "clientPassword": clientPassword,
        "timeBounds"    : timeBounds,
        "testNumber" : testNumber
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
        p('Ваш тестовый доступ готов.')
        hr()
        p('<b>Параметры доступа:</b>')
        table(style: 'text-align:left;') {
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Тип подключения:</b> ')
                td(connectionType)
            }
    '''
def restAdd = ''
switch (connectionType) {
    case "REST":
        template += '''\
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>API:</b>')
                td('<a href="https://msg.kcell.kz/api/v3">https://msg.kcell.kz/api/v3</a>')
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Заголовок:</b> ')
                td(finalIDs)
            }
            '''
        restAdd = '''\
'''
        break
    case "SMPP":
        template += '''\
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Внешний IP: </b> ')
                td('2.78.58.183')
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Port:</b> ')
                td('16000')
            }            
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Заголовок рассылки:</b> ')
                td(finalIDs)
            }
            '''
        break
}
def common = '''\
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Login:</b> ')
                td(clientLogin)
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Password:</b> ')
                td(clientPassword)
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Тестовые номера:</b> ')
                td(testNumber)
            }
            }     
            '''
template += common + restAdd + '''\
        hr()
        p('Данное сообщение было сгенерировано автоматически. По всем вопросам обращаться к ' + ccMails )
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
