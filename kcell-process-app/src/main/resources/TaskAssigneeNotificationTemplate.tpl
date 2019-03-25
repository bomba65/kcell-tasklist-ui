yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p {
            yield 'В рамках рассмотрения заявок по '
            b(processName)
            yield ' в системе '
            b('Kcell Workflow')
            yield ' на вас назначена активность '
            b(activity)
            yield ' по заявке # '
            b(businessKey)
            yield ' ожидающая вашего участия.'
           }
        p {
            yield 'Для просмотра заявки Вам необходимо пройти по следующей '
            a(href: taskUrl, 'ссылкe.')
        }
        table {
            tr {
                td {
                    yield 'Процесс: '
                }
                td {
                    b(processName)
                }
             }
            tr {
             td {
                 yield 'Номер заявки: '
             }
             td {
                 b(businessKey)
             }
            }
            tr {
               td {
                   yield 'Cтатус: '
               }
               td {
                   b('')
               }
            }
            tr {
               td {
                   yield 'Инициатор: '
               }
               td {
                   b(delegateTask.getVariable("starter"))
               }
            }
            tr {
               td {
                   yield 'Дата создания: '
               }
               td {
                   b(startTime.format('dd.MM.yyyy HH:mm'))
               }
            }
         }
        newLine()

        hr()
        p {
         yield 'Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: '
         b {
             a(href: 'https://flow.kcell.kz', 'https://flow.kcell.kz')
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
                a(href: 'mailto:support_flow@kcell.kz?subject='+subject, 'support_flow@kcell.kz')
            }
            yield ' с описанием.'
        }
        p ('C уважением,<br>Kcell Flow')
    }
}
