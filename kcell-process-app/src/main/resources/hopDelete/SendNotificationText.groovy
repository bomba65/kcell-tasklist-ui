package revision

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.camunda.bpm.engine.impl.util.json.JSONObject

import java.text.SimpleDateFormat

JSONObject tsd = new JSONObject(execution.getVariable("selectedTsd").toString());
String ne_site_name = tsd.getJSONObject("nearend_id").getString("site_name");
String fe_site_name = tsd.getJSONObject("farend_id").getString("site_name");
String regionName = execution.getVariable("regionName").toString();
String cancelTsdNumber = execution.getVariable("cancel-tsdNumber").toString();
String starter = execution.getVariable("starter").toString();

def formatDateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm")
def calendar = Calendar.getInstance();
calendar.setTime(requestedDate);
calendar.add(Calendar.HOUR, 6);
def date = formatDateTime.format(calendar.getTime())

def binding = [
    "regionName" : regionName,
    "siteName" : ne_site_name + "-" + fe_site_name,
    "number" : cancelTsdNumber,
    "starter" : starter,
    "date" : date,
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
        p("В рамках рассмотрения заявок по <b>Cancel TSD</b> в системе <b>Kcell Workflow</b> был запущен процесс по <b>отмене TSD</b> по заявке <b>#" + number + "</b> ожидающая Вашего участия.")
        newLine()
        table(class:"table", style:"font-size:12px;") {
            tr {
                td (width:"40%", style:"border:1px solid", "Процесс")
                td (width:"60%", style:"font-weight: bold; border:1px solid", "Cancel TSD")
            }
            tr {
                td (width:"40%", style:"border:1px solid", "Номер заявки")
                td (width:"60%", style:"font-weight: bold; border:1px solid", number)
            }
            tr {
                td (width:"40%", style:"border:1px solid", "Инициатор")
                td (width:"60%", style:"font-weight: bold; border:1px solid") {
                    a(href: "mailto:support_flow@kcell.kz", starter)
                }
                
            }
            tr {
                td (width:"40%",style:"border:1px solid", "Дата создания")
                td (width:"60%",style:"font-weight: bold;border:1px solid", date)
            }
            tr {
                td (width:"40%",style:"border:1px solid", "Сайт")
                td (width:"60%",style:"font-weight: bold;border:1px solid", siteName)
            }
            tr {
                td (width:"40%",style:"border:1px solid", "Статус")
                td (width:"60%",style:"font-weight: bold;border:1px solid", "Cancel TSD")
            }     
        }  
        newLine()
        p {
            yield "Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: "
            b {
                a(href: "https://flow.kcell.kz", "https://flow.kcell.kz")
            }
        } 
        newLine()
        p {
            yield "При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в "
            b {
                a(href: "mailto:support_flow@kcell.kz", "support_flow@kcell.kz")
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

