import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def taskUrl = baseUrl + '/kcell-tasklist-ui/#/tasks/' + delegateTask.id

def binding = ["taskUrl": taskUrl, "baseUrl": baseUrl, "businessKey": delegateTask.execution.processBusinessKey, "delegateTask": delegateTask, "subject": subject]

def template = this.getClass().getResource(templateName).text

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
