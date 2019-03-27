import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration


def customVariables  = [:]
def taskUrl = baseUrl + '/kcell-tasklist-ui/#/tasks/' + delegateTask.id
def processName = delegateTask.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateTask.getProcessDefinitionId()).getName()
def procInst = delegateTask.getProcessEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).singleResult()
def startTime = new Date()
if (procInst != null) startTime = procInst.getStartTime()
def status = ""
statusObj = delegateTask.getVariable('status')
if (statusObj!=null) {
    statusObj = new JsonSlurper().parseText(statusObj.toString())
    status = statusObj.statusName
}
def siteName = delegateTask.getVariable('site_name')
if (siteName!=null) {
    customVariables."Cайт"=siteName
}
def binding = ["processName": processName,
                "activity": delegateTask.getName(),
                "taskUrl": taskUrl, "baseUrl": baseUrl,
                "businessKey": delegateTask.execution.processBusinessKey,
                "delegateTask": delegateTask,
                "subject": subject,
                "startTime": startTime,
                "customVariables": customVariables,
                "status": status]

def template = this.getClass().getResource(templateName).text

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
