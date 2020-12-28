package revision

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

String regionName = execution.getVariable("regionName").toString();
def binding = [
    "regionName" : regionName
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
        p('Hi,')
        newLine()
        p('Уведомление')
        newLine()
        newLine()
        br()
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

