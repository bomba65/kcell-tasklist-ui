import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
//def ccMails = execution.getVariable("ccMails")
def companyLatName = execution.getVariable("clientCompanyLatNameMail")
def companyIp = execution.getVariable("ipNumberMail")

def binding = [
        "processName": processName,
        "companyLatName": companyLatName,
        "companyIp" :companyIp,
]
def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('<b>Здравствуйте</b>')
        p('<b>' + processName + '</b>.')
        newLine()
        p('Создан новый сетевой объект <Заявка на подключение ' + companyLatName +'_' + companyIp + '>.')
        newLine()
        p('Данное сообщение было сгенерировано автоматически')
        newLine()
        p('Greetings')
        newLine()
        p('Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
