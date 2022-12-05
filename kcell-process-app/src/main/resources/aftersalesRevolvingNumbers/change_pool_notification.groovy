import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def binding = [
        "legalName": legalInfo.unwrap().get('legalName').asText(),
        "bin": clientBIN,
        "currVoiceIp":techSpecs.unwrap().get("sip").get("curPublicVoiceIP").asText(),
        "currSignalIp":techSpecs.unwrap().get("sip").get("curSignalingIP").asText(),
        "connectionPoint":techSpecs.unwrap().get("connectionPoint").asText(),
        "description":techSpecs.unwrap().get('sip').get('description') ? techSpecs.unwrap().get('sip').get('description').asText() : " ",
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
        p('Транк на '+ connectionPoint +' расформирован.')
        newLine()
        p('<b>IP: </b>'+currVoiceIp+', '+ currSignalIp)
        newLine()
        p('<b>Описание: </b>'+ description)        
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
