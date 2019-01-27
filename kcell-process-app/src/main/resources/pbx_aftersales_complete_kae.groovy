import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def ts = techSpecs.unwrap()
def vpn = vpnAccessRules.unwrap()

def binding = [
        "legalName"       : legalInfo.unwrap().get('legalName').asText(),
        "bin"             : BIN,
        "connectionPoint" : ts.get('connectionPoint').asText(),
        "ip1" : vpn[0].get('source').asText(),
        "ip2" : vpn[1].get('source').asText()
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
        p('Ваш запрос на изменение/добавление настроек VPN выполнен.')
        newLine()
        p('<b>IP:</b> ' + ip1 + ', ' + ip2)
        newLine()
        p('<b>PBX:</b> ' + connectionPoint)
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
