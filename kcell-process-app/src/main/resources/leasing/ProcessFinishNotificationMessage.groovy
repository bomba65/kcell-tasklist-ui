import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import groovy.json.JsonSlurper

def messageCounter = execution.getVariable('messageCounter')

if(messageCounter == null){
    messageCounter = 0
}

def notCreatedContractArtefactsObject = new JsonSlurper().parseText(notCreatedContractArtefacts.toString())
def jsonBody = new JsonSlurper().parseText(jsonBody.toString())

def sitename = notCreatedContractArtefactsObject[messageCounter].ct_sitename
def bkey = execution.getProcessBusinessKey()
def start_date=jsonBody.contract_start_date

def binding = ["sitename": sitename,  "bkey": bkey, "start_date":start_date ]
def template= '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Заявка '+bkey+' на строительство сайта завершена.')
        newLine()
        p('Site: ' + sitename)
        newLine()
        p('Дата создания заявки: ' + start_date)
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
