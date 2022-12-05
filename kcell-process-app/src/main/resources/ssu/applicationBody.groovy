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

def binding = ["bin":bin, "subtype":subtype, "gender":gender, "cp_name":cp_name, "cp_surname":cp_surname, "billingObject":billingObject, "requestor": requestor,
               "cp_phone_number":cp_phone_number, "legal_add":legal_add, "post_ind":post_ind, "delivery_email":delivery_email,
               "contract_date": contract_date!=null ? formatDate.format(calendar.getTime()) : null, "delivery_add":delivery_add, "delivery_index":delivery_index,
               "acc_region":acc_region, "acc_template":acc_template, "acc_format":acc_format,"credit_limit":credit_limit,"contract_num":contract_num, "addition": addition]
def template = '''\
html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    body {
        p('Биллинговый объект: ' + billingObject + '#')
        p('БИН: ' + bin + '#')
        p('Отправитель: ' + requestor + '#')
        p('Подтип выставления: ' + subtype + '#')
        p('Пол: ' + gender + '#')
        p('Имя: ' + cp_name + '#')
        p('Фамилия: ' + cp_surname + '#')
        p('Телефон: ' + cp_phone_number + '#')
        p('Юридический адрес: ' + legal_add + '#')
        p('Почтовый индекс: ' + post_ind + '#')
        p('Email доставки: ' + delivery_email + '#')
        p('Дополнительно: ' + addition + '#')
        if (billingObject != 'no') {
            newLine()
            p('Номер договора: ' + contract_num + '#')
            p('Дата договора: ' + contract_date + '#')
            p('Адрес доставки: ' + delivery_add + '#')
            p('Индекс адреса доставки: ' + delivery_index + '#')
            p('Регион счета: ' + acc_region + '#')
            p('Шаблон счета: ' + acc_template + '#')
            p('Формат счета: ' + acc_format + '#')
        }
        p('Кредитный лимит: ' + credit_limit + '#')
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
