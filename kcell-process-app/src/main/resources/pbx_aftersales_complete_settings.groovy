import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def ts = techSpecs.unwrap()

def binding = [
        "legalName"       : legalInfo.unwrap().get('legalName').asText(),
        "bin"             : BIN,
        "curPublicVoiceIP": ts.get('sip').get('curPublicVoiceIP').asText(),
        "curSignalingIP"  : ts.get('sip').get('curSignalingIP').asText(),
        "transProtocol"   : ts.get('sip').get('transProtocol').asText(),
        "signalingPort"   : ts.get('sip').get('signalingPort').asText(),
        "voicePortStart"  : ts.get('sip').get('voicePortStart').asText(),
        "voicePortEnd"    : ts.get('sip').get('voicePortEnd').asText(),
        "sessionsCount"   : ts.get('sip').get('sessionsCount').asText(),
        "coding"          : ts.get('sip').get('coding').asText(),
        "connectionPoint" : ts.get('connectionPoint').asText(),
        "description"     : ts.get('sip').get('description').asText()
]

if (ts.get('sip').get('newPublicVoiceIP') != null) {
    binding.put("newPublicVoiceIP", ts.get('sip').get('newPublicVoiceIP').asText())
    binding.put("newSignalingIP", ts.get('sip').get('newSignalingIP').asText())
} else {
    binding.put("newPublicVoiceIP", 'не указан')
    binding.put("newSignalingIP", 'не указан')
}

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
        p('Ваш запрос на изменение настроек транспортного уровня на ' + connectionPoint + ' выполнен.')
        newLine()
        p('<b>Прежний IP:</b> ' + curPublicVoiceIP + ', ' + curSignalingIP)
        newLine()
        p('<b>Новый IP:</b> ' + newPublicVoiceIP + (newSignalingIP != 'не указан'?(', ' + newSignalingIP):''))
        newLine()
        p('<b>Протокол:</b> ' + transProtocol)
        newLine()
        p('<b>Новые порты:</b> ' + signalingPort + ', ' + voicePortStart + '-' + voicePortEnd)
        newLine()
        p('<b>Количество сесси:</b> ' + sessionsCount)
        newLine()
        p('<b>Кодек:</b> ' + coding)
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
