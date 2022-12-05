import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def binding = [
        "legalName": legalInfo.unwrap().get('legalName').asText(),
        "bin": clientBIN,
        "currVoiceIp":techSpecs.unwrap().get("sip").get("curPublicVoiceIP").asText(),
        "currSignalIp":techSpecs.unwrap().get("sip").get("curSignalingIP").asText(),
        "newVoiceIp":techSpecs.unwrap().get("sip").get("newPublicVoiceIP") ? techSpecs.unwrap().get("sip").get("newPublicVoiceIP").asText() : " ",
        "newSignalIp":techSpecs.unwrap().get("sip").get("newSignalingIP") ? techSpecs.unwrap().get("sip").get("newSignalingIP").asText() : " ",
        "signalingPort":techSpecs.unwrap().get("sip").get("signalingPort").asText(),
        "voicePortStart":techSpecs.unwrap().get("sip").get("voicePortStart").asText(),
        "voicePortEnd":techSpecs.unwrap().get("sip").get("voicePortEnd").asText(),
        "transProtocol":techSpecs.unwrap().get("sip").get("transProtocol").asText(),
        "sessionsCount":techSpecs.unwrap().get("sip").get("sessionsCount").asText(),
        "coding":techSpecs.unwrap().get("sip").get("coding").asText(),
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
        p('Процесс по изменению настроек транспортного уровня на '+ connectionPoint +' был принудительно завершен.')
        newLine()
        p('<b>Прежний IP: </b>'+currVoiceIp+', '+ currSignalIp)
        newLine()
        p('<b>Новый IP: </b>'+ newVoiceIp + ', ' + newSignalIp)        
        newLine()
        p('<b>Протокол: </b>'+ transProtocol)        
        newLine()
        p('<b>Новые порты: </b>'+ signalingPort + ', ' + voicePortStart +' - '+ voicePortEnd)        
        newLine()
        p('<b>Количество сессий: </b>'+ sessionsCount)        
        newLine()
        p('<b>Кодек: </b>'+ coding)        
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
