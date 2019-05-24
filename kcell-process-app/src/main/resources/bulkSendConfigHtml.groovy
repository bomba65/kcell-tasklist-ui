import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
def mailOne = execution.getVariable("mailOne")
def mailTwo = execution.getVariable("mailTwo")
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
        "mailOne"       : mailOne,
        "mailTwo"       : mailTwo,
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
        p('Здравствуйте,')
        newLine()
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
                td('<a href="https://api.kcell.kz/app/smsgw/rest/v2">https://api.kcell.kz/app/smsgw/rest/v2</a>')
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Заголовок:</b> ')
                td(finalIDs)
            }
            '''
        restAdd = '''\

hr()
p('Время рассылки ad99 (все дни с 9:00 до 21:00)')
'''
        break
    case "SMPP":
        template += '''\
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Внешний IP: </b> ')
                td('2.78.58.137')
            }
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Port:</b> ')
                td('1600')
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
            tr {
                th(scope: 'row', style:'white-space: nowrap;')('<b>Timebounds:</b> ')
                td(timeBounds)
            }
            }     
            '''
template += common + restAdd + '''\
        hr()
        p('Данное сообщение было сгенерировано автоматически. По всем вопросам обращаться к ' + mailOne + ', '  + mailTwo )
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
