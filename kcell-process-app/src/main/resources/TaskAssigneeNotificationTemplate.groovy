import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration


def customVariables  = [:]
def taskUrl = baseUrl + '/kcell-tasklist-ui/#/tasks/' + delegateTask.id
def processName = delegateTask.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateTask.getProcessDefinitionId()).getName()
def priority = null;

// subject can contain ampersands ~ problematic
subject = java.net.URLEncoder.encode(subject, "UTF-8")
subject = subject.replaceAll('\\+', ' ')

if (processName=="Revision") {
    customVariables."Cайт"=delegateTask.getVariable('site_name')
    def statusObj = delegateTask.getVariable('status')
    if(statusObj!=null){
        statusObj = new JsonSlurper().parseText(statusObj.toString())
        customVariables."Статус" = statusObj.statusName
    }
    priority = delegateTask.getVariable('priority')
    if(priority!=null && priority == "emergency"){
        customVariables."Приоритет" = "Emergency";
        subject = subject + " - Emergency"
    }
}
if (processName=="TNU") {
    customVariables."Cайт"=delegateTask.getVariable('ne_sitename')
}

def binding = ["processName": processName,
                "activity": delegateTask.getName(),
                "taskUrl": taskUrl, "baseUrl": baseUrl,
                "businessKey": delegateTask.execution.processBusinessKey,
                "delegateTask": delegateTask,
                "subject": subject,
                "startTime": startTime,
                "customVariables": customVariables]

def template = this.getClass().getResource(templateName).text

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
