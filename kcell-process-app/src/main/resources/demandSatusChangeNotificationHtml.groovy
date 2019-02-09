import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import java.text.SimpleDateFormat

def lastResolution = resolutions.unwrap()[0]
def formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
def assignee = lastResolution.get('assignee').asText()
def taskName = lastResolution.get('taskName').asText()
def general = generalData.unwrap().get('general')

def binding = [
        "businessKey": businessKey,
        "oldStatus": oldStatus,
        "status": status,
        "assignee" : assignee,
        "taskName" : taskName,
        "demandOwner": general.get('demandOwner').asText(),
        "createDate": processCreateDate,
        "demandName" : demandName,
        "description": general.get('description').asText(),
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

if (status == "New order") template += '''p('В рамках процесса рассмотрения заявок по Demand, в системе Kcell Workflow пользователь ' + demandOwner + ' создал(а) заявку # ' + businessKey + '.')'''
else template += '''p('В рамках процесса рассмотрения заявок по Demand, в системе Kcell Workflow статус заявки # ' + businessKey + ' изменен с ' + oldStatus + ' на ' + status + ' пользователем ' + assignee + ' в активности ' + taskName + '.')'''

template += '''\
        newLine()
        p('<b>Процесс:</b> Demand')
        newLine()
        p('<b>Номер заявки:</b> ' + businessKey)
        newLine()
        p('<b>Статус:</b> ' + status)
        newLine()
        p('<b>Инициатор:</b> ' + demandOwner)
        newLine()
        p('<b>Дата создания:</b> ' + createDate)
        newLine()
        p('<b>Имя заявки:</b> ' + demandName)
        newLine()
        p('<b>Описание:</b> ' + description)
        
        hr()
        
        p {
            yield 'Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: '
            b {
                a(href: taskUrl, 'https://preprod.test-flow.kcell.kz')
            }
        }
        p {
            yield 'Для входа в систему используйте свой корпоративный логин (Name.Surname@kcell.kz)* и пароль.'
            br()
            yield '* имя и фамилия в логине начинается с заглавной буквы. Например: '
            b {
                a(href: 'mailto:Petr.Petrov@kcell.kz', 'Petr.Petrov@kcell.kz')
            }
        }
        p {
            yield 'При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в '
            b {
                a(href: 'mailto:support_flow@kcell.kz', 'support_flow@kcell.kz')
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
