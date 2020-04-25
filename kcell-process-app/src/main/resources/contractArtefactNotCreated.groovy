import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import groovy.json.JsonSlurper

def messageCounter = execution.getVariable('messageCounter')

if(messageCounter == null){
    messageCounter = 0
}

def notCreatedContractArtefactsObject = new JsonSlurper().parseText(notCreatedContractArtefacts.toString())

def contractid = notCreatedContractArtefactsObject[messageCounter].contractid
def sitename = notCreatedContractArtefactsObject[messageCounter].ct_sitename
def type = notCreatedContractArtefactsObject[messageCounter].ct_acquisitionType
def bkey = execution.getProcessBusinessKey()

def binding = ["contractid" : contractid, "sitename": sitename, "type": type, "bkey": bkey ]

def template = '''\
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
        p('В рамках выполнения процесса Roll Out ' + type == 'newContract' ? 'создать новый' : 'изменить' + ' договор ' + contractid + ' в UDB не удалось')
        newLine()
        p('Причина: сайт фаренда ' + sitename + ' отсутствует в базе данных UDB.')
        newLine()
        p('Process Number: ' + bkey)
        newLine()
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

execution.setVariable('messageCounter', messageCounter+1)

result
