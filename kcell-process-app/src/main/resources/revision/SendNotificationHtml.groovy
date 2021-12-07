package revision

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import java.text.SimpleDateFormat

def processName = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getName()
def procInst = execution.getProcessEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
def format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

def statusObj = new JsonSlurper().parseText(status.toString())

def startTime = Calendar.getInstance();
if (procInst != null) {
    startTime.setTime(procInst.getStartTime());
} else {
    startTime.setTime(new Date());
}
startTime.add(Calendar.HOUR, 6);

def binding = ["processName": processName,
               "businessKey": execution.processBusinessKey,
               "starter": starter,
               "startTime": format.format(startTime.getTime()),
               "status": statusObj.statusName,
               "sitename": site_name,
               "priority": priority
]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p {
            yield 'В рамках рассмотрения заявок по '
            b(processName)
            yield ', в системе '
            b('Kcell Workflow')
            yield ' был присвоен номер джобу участником которого вы являетесь. '
           }
        table {
            tr {
                td {
                    yield 'Процесс: '
                }
                td {
                    b(processName)
                }
             }
            tr {
             td {
                 yield 'Номер заявки: '
             }
             td {
                 b(businessKey)
             }
            }
            tr {
               td {
                   yield 'Инициатор: '
               }
               td {
                   b(starter)
               }
            }
            tr {
               td {
                   yield 'Сайт: '
               }
               td {
                   b(sitename)
               }
            }
            tr {
               td {
                   yield 'Статус: '
               }
               td {
                   b(status)
               }
            }
            if(priority.equals("emergency")) {
                tr {
                    td {
                       yield 'Приориет: '
                    }
                    td {
                        b(Emergency)
                    }
                }
            }
         }
        newLine()
        hr()
        p {
         yield 'Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: '
         b {
             a(href: 'https://flow.kcell.kz', 'https://flow.kcell.kz')
         }
        }
        p {
            yield 'При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в '
            b {
                a(href: 'mailto:support_flow@kcell.kz?subject='+processName, 'support_flow@kcell.kz')
            }
            yield ' с описанием.'
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

print result

result
