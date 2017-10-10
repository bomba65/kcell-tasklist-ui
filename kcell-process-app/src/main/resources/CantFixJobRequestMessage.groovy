import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def jrNumberObj = (jrNumber != null ? jrNumber : '########')
def regionGroupHeadApprovalCommentObj = (regionGroupHeadApprovalComment != null ? regionGroupHeadApprovalComment : '(No comment)')

def binding = ["jrNumber" : jrNumberObj, "regionGroupHeadApprovalComment" : regionGroupHeadApprovalCommentObj]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Hi,')
        newLine()
        p('Job Request ' + jrNumber + ' is impossible to execute!')
        newLine()
        p('Request was rejected with comment: ' + regionGroupHeadApprovalComment)
        newLine()
        br()
        p {
            yield 'Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы:\'
            a(href : 'https://hub.kcell.kz/x/kYNoAg', 'https://hub.kcell.kz/x/kYNoAg')
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result

