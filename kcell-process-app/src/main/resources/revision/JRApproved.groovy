package revision

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def jrNumberObj = (jrNumber != null ? jrNumber : '########')

def binding = ["jrNumber" : jrNumberObj, "site_name" : site_name]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('В рамках процесса приемки выполненных работ, ваш JR был принят в системе Kcell Workflow, просьба предоставить Acceptance Form для подписания.')
        newLine()
        p {
            span('Номер заявки: ' + jrNumber)
            newLine()
            span('Сайт: ' + site_name)
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
