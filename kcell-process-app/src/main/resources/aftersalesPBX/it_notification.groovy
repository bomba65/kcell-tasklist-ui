import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def ts = techSpecs.unwrap()

def binding = [
        "legalName"       : legalInfo.unwrap().get('legalName').asText(),
        "bin"             : clientBIN,
        "curPublicVoiceIP": ts.get('sip').get('curPublicVoiceIP').asText(),
        "curSignalingIP"  : ts.get('sip').get('curSignalingIP').asText(),
        "connectionPoint" : ts.get('connectionPoint').asText(),
        "pbxNumbers"      : ts.get('pbxNumbers').asText(),
        "description"     : ts.get('sip').get('description').asText()
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
        p('<b>БИН: </b>' + bin)
        newLine()
        p('Транк на ' + connectionPoint + ' расформирован.')
        newLine()
        p('<b>Нумерация:</b> ' + pbxNumbers)
        newLine()
        p('<b>IP:</b> ' + curPublicVoiceIP + ', ' + curSignalingIP)
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
