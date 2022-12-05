import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def ts = techSpecs.unwrap()

def binding = [
        "kcellIp": ts.get('connectionPoint').asText() == "SBC" ? "2.78.58.151" : "2.78.58.167",
        "ipVoice": ts.get("sip").get('curPublicVoiceIP').asText(),
        "sipPort": ts.get("sip").get('signalingPort').asText(),
        "callerId": ts.get("sip").get('callerID').asText(),
        "coding": ts.get("sip").get('coding').asText(),
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
        p('Для настройки услуги Револьверный пакет Вам необходимо прописать на Вашей стороне следующие параметры:')
        p('1. IP со стороны Kcell - ' + kcellIp)
        p('2. IP со стороны Клиента: ' + ipVoice)
        p('3. SIP порт - ' + sipPort)
        p('4. Caller ID - ' + callerId)
        p('5. Кодеки - ' + coding)
        p('6. DTMF- RFC 2833')
        p('7. Формат номера международный, требуется «+».')
        p('8. Регистрация с нашей стороны не поддерживается, вам необходимо организовать соединение peer-to-peer (trunk).')
        newLine()
        p('<b><u>Для вас на время тестирования прописаны исходящие наборы только на служебные номера Kcell +7 701 211 хххх</u></b>')
        p('<b><u>Время тестирования с 9 до 18 в рабочие дни.</u></b>')
        p('Входящие наборы доступны с любых мобильных номеров.')
        p('Просьба если наборы не будут получаться выслать дамп (лог, трейс) вызова с вашего оборудования, чтобы выявить причину.')
        p('Тех отдел:')
        p('Pavel.Rachenkov@kcell.kz')
        p('Evgeniy.Grebnev@kcell.kz')
        p('Vladimir.Taldayev@kcell.kz')
        p('Roman.Shakhmatov@kcell.kz')
        p('Konstantin.Bayev@kcell.kz')
    }
}
"""

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()
result
