import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration


def tsdMwId = execution.getVariable("tsdMwId")
def link = "https://asset.test-flow.kcell.kz/kcell-tasklist-ui/#/assets/tsd/" + tsdMwId

def binding = [
        "link": link,
        "tsdMwId": tsdMwId,
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
        p(link)
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
