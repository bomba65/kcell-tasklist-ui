import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def request_number = execution.processBusinessKey
def initiatorComments = execution.getVariable("initiatorComments").toString()


def binding = ["request_number"  : request_number,
               "initiatorComments"  : initiatorComments]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "Автоматический запрос # " + request_number + " на расширение VPN отклонен по следующей причине " + initiatorComments
        }
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
