package revision

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import java.text.SimpleDateFormat

def jrNumberObj = (jrNumber != null ? jrNumber : '########')
def formatDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm")
def calendar = Calendar.getInstance();
calendar.setTime(requestedDate);
calendar.add(Calendar.HOUR, 6);
def requestedDate = formatDateTime.format(calendar.getTime())

def initiatorObj = new JsonSlurper().parseText(initiatorFull.toString())

def binding = [
    "jrNumber" : jrNumberObj, 
    "cancelPrComment" : cancelPrComment,
    "initiatorFull" : initiatorObj.firstName + " " + initiatorObj.lastName,
    "requestDate" : requestedDate,
    "site_name": site_name,
    "priority": priority
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
        p("В рамках рассмотрения заявок по Revision в системе Kcell Workflow  по заявке " + jrNumber + "джоб был отменен.")
        newLine()
        p {
            yield 'Для просмотра заявки Вам необходимо пройти по следующей '
            b {
                a(href: "https://flow.kcell.kz", "ссылке:")
            }
        }
        p('Процесс:|Revision|')
        p('Номер заявки: ' + jrNumber)
        table(class:"table", style:"font-size:12px;") {
            tr {
                td (width:"40%", style:"border:1px solid", "Инициатор")
                td (width:"60%", style:"font-weight: bold; border:1px solid", initiatorFull)
            }
            tr {
                td (width:"40%",style:"border:1px solid", "Дата создания")
                td (width:"60%",style:"font-weight: bold;border:1px solid", requestDate)
            }
            tr {
                td (width:"40%",style:"border:1px solid", "Сайт")
                td (width:"60%",style:"font-weight: bold;border:1px solid", site_name)
            }
            tr {
                td (width:"40%",style:"border:1px solid", "Статус")
                td (width:"60%",style:"font-weight: bold;border:1px solid", "Cancelled")
            }
            if(priority.equals("emergency")) {
                tr {
                    td (width:"40%",style:"border:1px solid", "Приориет")
                    td (width:"60%",style:"font-weight: bold;border:1px solid", "Emergency")
                }
            } 
        }  
        newLine()
        p("Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: https://flow.kcell.kz")
        newLine()
        p {
            yield "При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в "
            b {
                a(href: "mailto:support_flow@kcell.kz?subject="+subject, "support_flow@kcell.kz")
            }
            yield " с описанием."
        } 
        p ("Greetings,<br>Kcell Flow")
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result

