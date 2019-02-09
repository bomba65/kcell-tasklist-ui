yieldUnescaped '<!DOCTYPE html>'

def businessKey = delegateTask.getVariable('businessKey')
def general = delegateTask.getVariable('generalData').unwrap().get('general')

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p('В рамках процесса рассмотрения заявок по Demand, в системе Kcell Workflow на Вас назначена активность ' + delegateTask.getName() + ' по заявке # ' + businessKey + ', ожидающая Вашего участия.')
        newLine()
        p {
            yield 'Для просмотра заявки Вам необходимо пройти по следующей '
            a(href: taskUrl, 'ссылке')
            yield '.'
        }
        newLine()
        p('<b>Процесс:</b> Demand')
        newLine()
        p('<b>Номер заявки:</b> ' + businessKey)
        newLine()
        p('<b>Статус:</b> ' + delegateTask.getVariable('status'))
        newLine()
        p('<b>Инициатор:</b> ' + general.get('demandOwner').asText())
        newLine()
        p('<b>Дата создания:</b> ' + delegateTask.getVariable('processCreateDate'))
        newLine()
        p('<b>Имя заявки:</b> ' + delegateTask.getVariable('demandName'))
        newLine()
        p('<b>Описание:</b> ' + general.get('description').asText())

        hr()

        p {
            yield 'Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: '
            b {
                a(href: taskUrl, 'https://preprod.test-flow.kcell.kz')
            }
        }
        p {
            yield 'Для входа в систему используйте свой корпоративный логин (Name.Surname@kcell.kz)* и пароль.'
            br()
            yield '* имя и фамилия в логине начинается с заглавной буквы. Например: '
            b {
                a(href: 'mailto:Petr.Petrov@kcell.kz', 'Petr.Petrov@kcell.kz')
            }
        }
        p {
            yield 'При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в '
            b {
                a(href: 'mailto:support_flow@kcell.kz', 'support_flow@kcell.kz')
            }
            yield ' с описанием.'
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
