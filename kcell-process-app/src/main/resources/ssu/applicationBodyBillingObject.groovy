package ssu

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import java.text.SimpleDateFormat

/*
def bin = '850101305704'
def subtype = '850101305704'
def gender = '850101305704'
def cp_name = '850101305704'
def cp_surname = '850101305704'
def cp_phone_number = '850101305704'
def legal_add = '850101305704'
def post_ind = '850101305704'
def delivery_email = '850101305704'
def billingObject = 'ffb'
def contract_date = new Date()
def delivery_add = '850101305704'
def delivery_index = '850101305704'
def acc_region = '850101305704'
def acc_template = '850101305704'
def acc_format = '850101305704'
*/

def calendar = Calendar.getInstance();
if(contract_date!=null){
    calendar.setTime(contract_date);
}
calendar.add(Calendar.HOUR, 6);

def formatDate = new SimpleDateFormat("dd.MM.yyyy")

def binding = ["bin":bin, "delivery_email":delivery_email,"requestor": requestor,
               "contract_date": contract_date!=null ? formatDate.format(calendar.getTime()) : null,
               "acc_template":acc_template, "credit_limit":credit_limit,"contract_num":contract_num,
                "change_contract_num": change_contract_num, "change_contract_num": change_contract_date, "change_credit_limit": change_credit_limit, "change_email": change_email]

def template = '''\
html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    body {
        p('БИН: ' + bin + '#')
        p('Отправитель: ' + requestor + '#')
        p('Номер договора: ' + contract_num + '#')
        p('Дата договора: ' + contract_date + '#')
        p('Шаблон счета: ' + acc_template + '#')
        p('Кредитный лимит: ' + credit_limit + '#')
        p('Email доставки: ' + delivery_email + '#')
        newLine()
        p('Изменение номера договора: ' + change_contract_num + '#')
        p('Изменение даты договора: ' + change_contract_num + '#')
        p('Изменение кредитного лимита: ' + change_credit_limit + '#')
        p('Изменение Email: ' + change_email + '#')
        newLine()
        p('End.')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
