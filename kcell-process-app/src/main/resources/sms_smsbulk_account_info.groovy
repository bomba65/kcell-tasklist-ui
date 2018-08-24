import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource

def binding = ["finalIDs" : finalIDs, "smsbulkLogin" : smsbulkLogin, "smsbulkPass" : smsbulkPass]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p('Добрый день!')
        newLine()
        p('Подключена услуга SMS-рассылка.')
        newLine()
        p('Ссылка на web-интерфейс:')
        p('http://smsbulk.kcell.kz/ru/app/auth')
        //p('Идентификаторы: ' + finalIDs)
        newLine()
        p('Логин: ' + smsbulkLogin)
        newLine()
        p('Пароль: ' + smsbulkPass)
        newLine()
        p('Инструкция во вложении')
        p('Спасибо за сотрудничество!')
        newLine()
        newLine()
        p('С Уважением,')
        newLine()
        p('АО «Кселл»')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
