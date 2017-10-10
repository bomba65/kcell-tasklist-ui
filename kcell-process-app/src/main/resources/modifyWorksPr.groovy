import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def jrNumberObj = (jrNumber != null ? jrNumber : '########')

def binding = ["jrNumber" : jrNumberObj, "cancelPrComment" : cancelPrComment]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Уважаемые коллеги!')
        newLine()
        p('Примите во внимание, что набор работ в  JR ' + jrNumber + ' был изменен в системе Workflow')
        newLine()        
        p('JR был изменен с таким комментарием: ' + cancelPrComment)
        newLine()        
        p('Просим проверить был ли создан PR по данному JR, и, в случае необходимости, произвести отмену.')
        newLine()        
        p('С Уважением,')
        newLine()
        p('Kcell Flow')
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
