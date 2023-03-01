package revision

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import java.text.SimpleDateFormat

def procInst = execution.getProcessEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
def format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

def startTime = Calendar.getInstance();
if (procInst != null) {
    startTime.setTime(procInst.getStartTime());
} else {
    startTime.setTime(new Date());
}

def binding = [
        "jrNumber" : jrNumber,
        "site_name": site_name,
        "startTime": format.format(startTime.getTime()),
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
        p("Заявка процесса Revision " + jrNumber + " завершена.")
        newLine()
        p("Site: " + site_name)
        newLine()
        p("Дата создания заявки: " + startTime)
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
