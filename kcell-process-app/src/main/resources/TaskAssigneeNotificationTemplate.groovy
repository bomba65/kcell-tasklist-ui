import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def taskUrl = baseUrl + '/kcell-tasklist-ui/#/?task=' + delegateTask.id

def binding = ["taskUrl": taskUrl, "businessKey": delegateTask.execution.businessKey]


def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p {
            yield 'В рамках процесса одобрения заявок на проведение работ, в системе Kcell Workflow создана заявка, ожидающая вашего участия. Для просмотра заявки необходимо пройти по следующей ссылке:'
            a(href: taskUrl, taskUrl)
        }
        p {
            yield 'Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы:'
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
