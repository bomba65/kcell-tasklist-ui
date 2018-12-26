import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def lastResolution = resolutions.unwrap()[0]
def subject = businessKey + ", "
def resolution = lastResolution.get('resolution').asText()
if (resolution == 'Approve') resolution = 'Approved'
else if (resolution == 'Cancel') resolution = 'Canceled'
else resolution = 'Approved conditionally'
subject += resolution + " on " + lastResolution.get("taskName").asText()

def binding = ["subject" : subject, "name" : demandName, "description": generalData.unwrap().get('general').get('description').asText()]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p(subject)
        newLine()
        p('<b>Demand Name:</b> ' + name)
        newLine()
        p('<b>Demand description:</b> ' + description)
        newLine()
        p ('Greetings,<br>Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

println(result)

result
