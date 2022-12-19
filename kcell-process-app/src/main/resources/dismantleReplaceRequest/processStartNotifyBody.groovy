package dismantleReplaceRequest

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def binding = ["job_number" : (requestType == "dismantle" ? sdrNumber : srrNumber),
               "site_name" : site_name,
               'start_date' : (requestType == "dismantle" ? sdrCreationDate : srrCreationDate),
               'processType' : (requestType == "dismantle" ? "демонтажа" : "переноса")]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Заявка ' + job_number + ' на одобрение ' + processType + ' сайта создана.')
        newLine()
        p {
            span('Site: ' + site_name)
            newLine()
            span('Дата создания заявки: ' + start_date)
        }
        newLine()
        p {
            span('Открыть Kcell Workflow вы можете пройдя по следующей ссылке: ')
            a(href : 'https://flow.kcell.kz', 'https://flow.kcell.kz')        
        }
        newLine()
        p('С Уважением,')
        newLine()
        p('Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result

