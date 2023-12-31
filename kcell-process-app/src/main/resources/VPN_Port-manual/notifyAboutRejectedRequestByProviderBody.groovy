import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def channel = execution.getVariable("channel").toString()
def request_type = execution.getVariable("request_type").toString()
def request_number = execution.processBusinessKey
def initiator = execution.getVariable("starter").toString()
def priority = execution.getVariable("priority").toString()
def provider_comments = execution.getVariable("inputTheProvidersNorTaskComment").toString()

def channel_rus
if (channel == "Port") {
    channel_rus = "порта"
} else {
    channel_rus = "VPN"
}

def request_type_rus
if (request_type == "Organize") {
    request_type_rus = "организацию"
} else if (request_type == "Modify") {
    request_type_rus = "изменение"
} else if (request_type == "Disband") {
    request_type_rus = "расформирование"
}

def binding = ["request_number"  : request_number,
               "request_type_rus"   : request_type_rus,
               "channel_rus"        : channel_rus,
               "initiator"          : initiator,
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
            yield "Запрос # " + request_number + " на " + request_type_rus + " " + channel_rus + " завершен с отказом со стороны Провайдера."
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
