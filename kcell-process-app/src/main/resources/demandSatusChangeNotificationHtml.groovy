import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
def procInst = execution.getProcessEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult()
def startTime = new Date()
if (procInst != null) startTime = procInst.getStartTime()
def lastResolution = resolutions.unwrap()[0]
def assignee = lastResolution.get('assignee').asText()
def taskName = lastResolution.get('taskName').asText()
def general = generalData.unwrap().get('general')


//def subject = "Demand - " + businessKey + " - Order status changed to " + status + " on " + taskName
//if (status == "New order") subject = "Demand - " + businessKey + " - Order created"
subject = processName + " - " + businessKey

def binding = [
        "processName": processName,
        "businessKey": businessKey,
        "oldStatus": oldStatus,
        "status": status,
        "assignee" : assignee,
        "taskName" : taskName,
        "demandOwner": general.get('demandOwner').asText(),
        "createDate": startTime.format('dd.MM.yyyy HH:mm'),
        "demandName" : demandName,
        "description": general.get('description').asText(),
        "subject": subject
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
'''

if (status == "New order") template += '''p('В рамках процесса рассмотрения заявок по <b>' + processName + '</b>, в системе <b>Kcell Workflow</b> пользователь <b>' + demandOwner + '</b> создал(а) заявку # <b>' + businessKey + '</b>.')'''
else template += '''p('В рамках процесса рассмотрения заявок по <b>' + processName + '</b>, в системе <b>Kcell Workflow</b> статус заявки # <b>' + businessKey + '</b> изменен с <b>' + oldStatus + '</b> на <b>' + status + '</b> пользователем <b>' + assignee + '</b> в активности <b>' + taskName + '</b>.')'''

template += '''\
        newLine()
        table {
            tr {
                td(scope: 'row', style:'white-space: nowrap;')('<b>Процесс:</b> ')
                td(processName)
            }
            tr {
                td(scope: 'row', style:'white-space: nowrap;')('<b>Номер заявки:</b> ')
                td(businessKey)
            }
            tr {
                td(scope: 'row', style:'white-space: nowrap;')('<b>Статус:</b> ')
                td(status)
            }
            tr {
                td(scope: 'row', style:'white-space: nowrap;')('<b>Инициатор:</b> ')
                td(demandOwner)
            }
            tr {
                td(scope: 'row', style:'white-space: nowrap;')('<b>Дата создания:</b> ')
                td(createDate)
            }
            tr {
                td(scope: 'row', style: 'vertical-align:top; white-space: nowrap;')('<b>Имя заявки:</b> ')
                td(demandName)
            }
            tr {
                td(scope: 'row', style: 'vertical-align:top;white-space: nowrap;')('<b>Описание:</b> ')
                td(description)
            }
        }
        newLine()
        
        hr()
        
        p {
            yield 'Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: '
            b {
                a(href: taskUrl, 'https://preprod.test-flow.kcell.kz')
            }
        }
        p {
            yield 'При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в '
            b {
                a(href: 'mailto:support_flow@kcell.kz?subject='+subject, 'support_flow@kcell.kz')
            }
            yield ' с описанием.'
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
