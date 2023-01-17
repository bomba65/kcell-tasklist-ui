package revision

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import java.text.SimpleDateFormat
import groovy.json.JsonSlurper

def formatDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm")
def jrNumberObj = (jrNumber != null ? jrNumber : '########')

def calendar = Calendar.getInstance();
calendar.setTime(acceptanceDate);
def acceptanceDate = formatDateTime.format(calendar.getTime())

calendar.setTime(requestedDate);
def startTimeObj = formatDateTime.format(calendar.getTime())

def initiatorFullObj = new JsonSlurper().parseText(initiatorFull.toString())
def jobWorksObj = new JsonSlurper().parseText(jobWorks.toString())

def binding = ["jrNumber" : jrNumberObj, "site_name" : site_name, "explanation" : explanation, "jobWorks": jobWorksObj, "startTime" : startTimeObj, "initiatorFull": initiatorFullObj, "acceptanceDate" : acceptanceDate]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('В системе Workflow была открыта, завершена и принята работа, касающаяся демонтажа или перемещения РРЛ.')
        newLine()
        p('В связи с этим, вам необходимо произвести корректировки в базах данных и, при необходимости, инициировать отказ от РЧС.')
        p {
            span('Номер работы: ' + jrNumber)
        }
        newLine()
        p {
            span('Дата открытия работы: ' + startTime)
        }
        p {
            span('Инициатор: ' + initiatorFull.firstName + ' ' + initiatorFull.lastName)
        }
        p {
            span('Сайт: ' + site_name)
        }
        p {
            span('Тип работы: ')
            jobWorks.each { work ->
                p {
                    span(work.displayServiceName + '; ')
                }
            }
        }
        p {
            span('Дата завершения работы: ' + acceptanceDate)
        }
        p {
            span('Пояснение к работе: ' + explanation)
        }
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
