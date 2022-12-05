import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def binding = [
        "legalName": legalInfo.unwrap().get('legalName').asText(), "bin": clientBIN ]

def template = """
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('<b>Клиент:</b> ' + legalName)
        newLine()
        p('<b>БИН: </b>' + bin)
        newLine()
        p('Просьба инициировать тестовый звонок для проверки биллинга.')
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
