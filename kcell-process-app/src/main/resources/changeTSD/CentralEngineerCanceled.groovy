package changeTSD
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def binding = [
               "businessKey": execution.processBusinessKey,
               "assignee":execution.getVariable('centerEngineerassigneeName'),
                "comment":execution.getVariable('checkByCenterEngineerTaskComment')
]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
       p('Добрый день!')
        newLine()
        p('ваша заявка Change TSD ' +businessKey+ ' отклонена ' +assignee+ ' по причине '+comment)
        newLine()
        p ('C уважением,<br>Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
