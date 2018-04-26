import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def jrNumberObj = (jrNumber != null ? jrNumber : '########')

def binding = ["jrNumber" : jrNumberObj, "contractorAttachesMaterialListComment" : contractorAttachesMaterialListComment]

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
        p('Примите во внимание, что в рамках процесса одобрения материалов по вашей заявке JR # ' + jrNumber + ' на этапе Attach Material List подрядчик выбрал действие - материал не требуется. Комментарии следующие: ' + contractorAttachesMaterialListComment)
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
