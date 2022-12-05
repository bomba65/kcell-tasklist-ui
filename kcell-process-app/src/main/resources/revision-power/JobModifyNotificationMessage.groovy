package revision

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import java.text.SimpleDateFormat

def jrNumberObj = (jrNumber != null ? jrNumber : '########')

def initiatorObj = new JsonSlurper().parseText(initiatorFull.toString())
def jrReasonObj = new JsonSlurper().parseText(jrReason.toString())
def binding = [
        "jrNumber" : jrNumberObj,
        "initiatorFull" : initiatorObj.firstName + " " + initiatorObj.lastName,
        "site_name": Site_Name,
        "jrReason":jrReasonObj.name,
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
        p("Заявка на электромонтажные работы " + jrNumber + " возвращена инициатору, требуются корректировки.")
        newLine()
        p("Site: "+site_name)
        newLine() 
        p ("Инициатор возврата: "+initiatorFull)
        newLine()
        p ("Причина возврата: "+jrReason)
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result



