import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def request_number = execution.processBusinessKey
def priority = execution.getVariable("priority").toString()
def provider_comments = execution.getVariable("inputTheProvidersNorTaskComment").toString()

def binding = ["request_number"  : request_number,
               "priority"           : priority,
               "provider_comments"  : provider_comments]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "Автоматический запрос # " + request_number + " на расширение VPN завершен с отказом со стороны Провайдера."
        }
        p { 
            yield "Комментарии: "
            span {
                yield provider_comments
            }
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
