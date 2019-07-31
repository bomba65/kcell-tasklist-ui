import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def workTypeJson = workType.unwrap()
def workType = ''
for (int i = 0; i < workTypeJson.size(); i++) {
    if (i > 0) workType += ', '
    workType += workTypeJson[i].asText()
}

def ts = techSpecs.unwrap()

def binding = [
        "legalName"     : legalInfo.unwrap().get('legalName').asText(),
        "bin"           : clientBIN,
        "workType"      : workType,
        "description"   : ts.get('sip').get('description').asText()
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
        p('<b>Клиент:</b> ' + legalName)
        newLine()
        p('<b>БИН:</b> ' + bin)
        newLine()
        p('Процесс по ' + workType + ' был принудительно завершен.')
        newLine()
        if (description != 'null') {
            p('<b>Описание: </b>' + description)
            newLine()
        }
    }
}
"""

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()
result
