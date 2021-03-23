import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def maNumberObj = (maNumber != null ? maNumber : '########')

def binding = ["maNumber" : maNumberObj]

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    body {
        p("Уважаемые коллеги,")
        newLine()
        newLine()        
        p("Ваш месячный акт " + maNumber + " был подтвержден всеми центральными группами. Просьба распечатать вложенный акт в трёх номер акта экземплярах, приложить все подписанные заявки вошедшие в акт в одном экземпляре. Также просьба распечатать Акт Выполненных работ и Электронную Счёт Фактуру.")
        newLine()
        newLine()        
        p("Все документы передать Бегалы Кокину 87012114338 в офис АО Кселл по адресу Алимжанова 51.")
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
