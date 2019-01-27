import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def ts = techSpecs.unwrap()

def binding = [
        "legalName"             : legalInfo.unwrap().get('legalName').asText(),
        "bin"                   : BIN,
        "connectionPointNew"    : ts.get('connectionPointNew').asText(),
        "pbxNumbers"            : ts.get('pbxNumbers').asText()
]

def template = """
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p(legalName)
        newLine()
        p(bin)
        newLine()
        p('Ваш запрос на открытие коммерческого доступа на ' + connectionPointNew + ' выполнен.')
        newLine()
        p('<b>Нумерация:</b> ' + pbxNumbers)
        newLine()
    }
}
"""

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
