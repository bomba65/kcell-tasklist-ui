import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def binding = ["finalIDs" : finalIDs, "smsbulkLogin" : smsbulkLogin, "smsbulkPass" : smsbulkPass]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Уважаемый коллега!')
        newLine()
        p('Учётные данные кабинета для кабинета smsbulk.kcell.kz')
        p('Идентификаторы: ' + finalIDs)
        newLine()
        p('Username: ' + smsbulkLogin)
        newLine()
        p('Password: ' + smsbulkPass)
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
