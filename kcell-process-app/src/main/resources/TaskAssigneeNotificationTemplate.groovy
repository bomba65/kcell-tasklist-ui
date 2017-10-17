import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def taskUrl = baseUrl + '/kcell-tasklist-ui/#/?task=' + delegateTask.id

def binding = ["taskUrl": taskUrl, "businessKey": delegateTask.execution.processBusinessKey, "siteName": delegateTask.getVariable("siteName")]

def template = this.getClass().getResource(templateName).text

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

println(result)

result
