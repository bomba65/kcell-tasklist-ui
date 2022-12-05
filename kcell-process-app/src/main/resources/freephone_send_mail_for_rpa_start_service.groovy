import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration




def binding = [
        "finalIDs": finalIDs,
        "officialClientCompanyName": officialClientCompanyName,
        "clientBIN": clientBIN,
        "ipNumber": ipNumber,
        "ipNumberAlt": (ipNumberAlt == null ? "" : "p('Дополнительный IP: '" + ipNumberAlt + "newLine()"),
        "port": port,
        "provider": provider

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
        p('Добрый день!')
        newLine()
        p('Требуется добавить в конфигурации SBC короткий номер ' + finalIDs)
        newLine()
        p('Клиент: ' + officialClientCompanyName)
        newLine()
        p('БИН: ' + clientBIN)
        newLine()
        p('IP адрес: ' + ipNumber)
        newLine()
        ipNumberAlt
        p('Порт: ' + port)
        newLine()
        p('Оффнет: ' + provider)
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
