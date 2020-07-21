import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration


def connectionPoint = technicalSpecifications.unwrap().get('connectionPoint').asText()
def pbxNumbers = technicalSpecifications.unwrap().get('pbxNumbers').asText()
def authorizationType = sipProtocol.unwrap().get('authorizationType').asText()
def ipSignaling = sipProtocol.unwrap().get('ipSignaling').asText()
def ipVoiceTraffic = sipProtocol.unwrap().get('ipVoiceTraffic').asText()
def preferredCoding = sipProtocol.unwrap().get('preferredCoding').asText()
def transportLayerProtocol = sipProtocol.unwrap().get('transportLayerProtocol').asText()
String signalingPort = sipProtocol.unwrap().get('signalingPort').asText()
def virtualNumbersCount = technicalSpecifications.unwrap().get('virtualNumbersCount').asInt()

String portStr = ""

for (String port : signalingPort.split(",")) {
    portStr += "ipSignaling:" + port + ";"
}


def destinationIp;

if (connectionPoint == 'SIP Proxy') {
    if (authorizationType == 'SIP-авторизация(доступ по стат. IP c лог/пар)') {
        destinationIp = "2.78.58.167"
    } else {
        destinationIp = "2.78.58.154"
    }
} else if (connectionPoint == 'SBC') {
    if (virtualNumbersCount < 100) {
        destinationIp = "195.47.255.119"
    } else if (virtualNumbersCount >= 100 && virtualNumbersCount < 1000) {
        destinationIp = "195.47.255.84"
    } else {
        destinationIp = "195.47.255.97"
    }

} else {
    destinationIp = "195.47.255.212"
}

def binding = [
        "connectionPoint"  : connectionPoint,
        "authorizationType": authorizationType,
        "destinationIp"    : destinationIp,
        "preferredCoding"        : preferredCoding,
        "pbxNumbers"        : pbxNumbers,
        "portStr"          : portStr,
        "ipSignaling"          : ipSignaling,
        "ipVoiceTraffic"          : ipVoiceTraffic,
        "transportLayerProtocol"          : transportLayerProtocol,
        "legalName"     : customerInformation.unwrap().get('legalName').asText(),
        "signalingPort" : signalingPort
]

def template;

if (connectionPoint == 'SIP Proxy') {
    template = """
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
        p('Вас приветствует технический департамент АО "Kcell"!')
        newLine()
        p('К нам пришла заявка на организацию SIP-транка для вашего PBX.')
        newLine()
        p('Пожалуйста, не отвечайте на данное письмо, так как оно отправлено автоматически.')
        newLine()
        p('Мы сделали настройки со своей стороны, прописали ваши IP-адреса(' + portStr + '), а также организовали маршрутизацию на выделенную вам нумерационную ёмкость.' )
        newLine()
        p('Теперь мы ждём, когда вы выполните соответствующие настройки на своём оборудовании. Наш внешний IP-адрес ' + destinationIp +  ' для SIP-трафика (на этот адрес настраивается соединение, адреса из диапазона 185.2.224.16/255.255.255.240 (UDP 10000-49999) - для RTP (обычно эти адреса прописываются только на Firewall, они пригодятся, если транк не заработает сразу). ')
        newLine()        
        p('Для вас на время тестировния прописаны наборы только на номера с префиксом +7 701 211 (служебные номера Kcell).')
        newLine()
        p('Просим вас сообщить о факте готовности к тестированию на почтовый адрес <a href="mailto:Anatoliy.Khan@kcell.kz">Anatoliy.Khan@kcell.kz</a> или по номеру +7 701 2118277.')
        newLine()
        p('Просьба, если наборы не будут получаться, выслать дамп (лог, трейс) вызова с вашего оборудования, чтобы выявить причину.')
        newLine()
        p('Согласно нашей процедуре, тестирование будет заключаться в следующем:')
        newLine()
        p('- отбой со стороны абонента А')
        newLine()
        p('- отбой со стороны абонента Б')
        newLine()
        p('- проверка качества передачи голоса')
        newLine()
        p(' - проверка корректности тарификации')
        newLine()
        p('Заранее спасибо.')
        newLine()
    }
}
"""
} else {
    template = """
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Добрый день,')
        newLine()
        p('Для настройки услуги бизнес телефония вам необходимо прописать на вашей стороне следующие параметры:')
        newLine()
        ul{
            li('1. IP со стороны Kcell – ' + destinationIp + ':5060 (с нашей стороны используется NAT).')
            li('2. IP со стороны ' + legalName + ' – ' + ipSignaling + '.')
            li('3. SIP порт – ' + signalingPort + ', ' + transportLayerProtocol + '.')
            li('4. Выделенная для вас нумерация – ' + pbxNumbers + '.')
            li('5. Кодеки  – ' + preferredCoding + '.')
            li('6. DTMF- RFC 2833.')
            li('7. Регистрация с нашей стороны не поддерживается, вам необходимо организовать соединение peer-to-peer (trunk).')
        }
        newLine()
        p('Для вас на время тестирования прописаны исходящие наборы только на служебные номера Kcell +7 701 211 хххх')
        newLine()
        p('Время тестирования  с 9 до 18 в рабочие дни.')
        newLine()
        p('Входящие наборы доступны с любых мобильных номеров.')
        newLine()
        p('Просьба если наборы не будут получаться выслать дамп (лог, трейс) вызова с вашего оборудования, ответив на данное письмо, чтобы выявить причину.')
        newLine()
        p('В случае, если в процессе настройки с Вашей стороны изменились технические параметры (IP адрес, порт, нумерация) просим Вас обращаться к Вашему курирующему менеджеру.')
        newLine()
    }
}
"""
}


def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()
result
