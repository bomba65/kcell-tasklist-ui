import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration


def customVariables  = [:]
def taskUrl = baseUrl + '/kcell-tasklist-ui/#/tasks/' + delegateExecution.id
def processName = delegateExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateExecution.getProcessDefinitionId()).getName()
if (processName=="Revision") {
    customVariables."Cайт"=delegateExecution.getVariable('site_name')
    def statusObj = delegateExecution.getVariable('status')
    if(statusObj!=null){
        statusObj = new JsonSlurper().parseText(statusObj.toString())
        customVariables."Статус" = statusObj.statusName
    }
}
// subject can contain ampersands ~ problematic
subject = java.net.URLEncoder.encode(subject, "UTF-8")
subject = subject.replaceAll('\\+', ' ')
def binding = ["processName": processName,
                "starter": starter,
                "taskUrl": taskUrl, "baseUrl": baseUrl,
                "businessKey": delegateExecution.processBusinessKey,
                "delegateExecution": delegateExecution,
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
