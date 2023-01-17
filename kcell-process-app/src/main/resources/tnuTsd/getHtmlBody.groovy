package tnuTsd

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.springframework.core.io.ClassPathResource

import java.text.SimpleDateFormat

def link = System.getenv('ASSET_URL') ?: 'https://asset.test-flow.kcell.kz'

def tsdMwId = execution.getVariable("tsdMwId")
link = link + "/kcell-tasklist-ui/#/assets/tsd/" + tsdMwId

def startTime = execution.getProcessEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).list().get(0).getStartTime();
Calendar c = Calendar.getInstance();
c.setTime(startTime);
def binding = [
        "link": link,
        "tsdMwId": tsdMwId,
        "businessKey": execution.getBusinessKey(),
        "execution": execution,
        "startTime": new SimpleDateFormat("yyyy-MM-dd HH:mm").format(c.getTime())
]

def template = '''\
yieldUnescaped '<!DOCTYPE html>\'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p {
            yield 'В рамках рассмотрения заявок по \'
            b('Create new TSD')
            yield ' в системе \'
            b('Kcell Workflow')
            yield ' на Вас назначена активность \'
            yield ' по заявке # \'
            b(businessKey)
            yield ' ожидающая Вашего участия.\'
           }
        p {
            yield 'Для просмотра заявки Вам необходимо пройти по следующей ссылке \'
            a(href: link, link)
        }
        table {
            tr {
                td {
                    yield 'Процесс: \'
                }
                td {
                    b('Create new TSD')
                }
             }
            tr {
             td {
                 yield 'Номер заявки: \'
             }
             td {
                 b(businessKey)
             }
            }
            tr {
               td {
                   yield 'Инициатор: \'
               }
               td {
                   b(execution.getVariable("starter"))
               }
            }
            tr {
               td {
                   yield 'Дата создания: \'
               }
               td {
                   b(startTime)
               }
            }
            
         }
        newLine()
        hr()
        p {
         yield 'Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: \'
         b {
             a(href: 'https://flow.kcell.kz', 'https://flow.kcell.kz')
         }
        }
        p {
            yield 'При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в \'
            b {
                a(href: 'mailto:support_flow@kcell.kz?subject='+subject, 'support_flow@kcell.kz')
            }
            yield ' с описанием.\'
        }
        p ('C уважением,<br>Kcell Flow')
    }
}

'''


def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
