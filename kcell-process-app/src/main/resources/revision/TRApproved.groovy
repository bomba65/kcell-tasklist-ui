package revision

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration


def binding = ["jrNumber" : execution.processBusinessKey]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Добрый день!')
        newLine()
        p('Примите во внимание, что в рамках процесса одобрения материалов по заявке JR # ' + jrNumber + ' материалы и TR были одобрены и переданы на склад для дальнейшей обработки. Просим связаться со складом и согласовать дату и время выдачи материалов.')
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

result
