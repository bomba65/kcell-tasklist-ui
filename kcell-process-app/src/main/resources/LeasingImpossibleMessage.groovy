import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def jrNumberObj = (jrNumber != null ? jrNumber : '########')
def leasingCompletionCommentObj = (leasingCompletionComment != null ? leasingCompletionComment : '(No comment)')

def binding = ["jrNumber" : jrNumberObj, "leasingCompletionComment" : leasingCompletionCommentObj]

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
        p('Leasing for Job Request ' + jrNumber + ' is impossible!')
        newLine()
        p('Request was rejected with comment: ' + leasingCompletionComment)
        br()
        p {
            yield 'Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы:'
            a(href : 'https://hub.kcell.kz/x/kYNoAg', 'https://hub.kcell.kz/x/kYNoAg')
        }
        p{
            yield 'Greetings,'
            br()
            yield 'Kcell Flow'            
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
