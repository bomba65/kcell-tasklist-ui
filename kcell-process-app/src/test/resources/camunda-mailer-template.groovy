import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def binding = ["surname": surname, "name": name, "patronymic": patronymic]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('Mail Page')
    }
    newLine()
    style ('td { padding: 10px; }')
    newLine()
    body {
        p('Hi,')
        newLine()
        p('Dear ' + surname + ' ' + name + ' ' + patronymic)
        newLine()
        newLine()
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