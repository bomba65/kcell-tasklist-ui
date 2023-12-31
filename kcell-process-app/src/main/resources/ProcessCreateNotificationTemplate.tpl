yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p {
            yield 'В рамках рассмотрения заявок по '
            b(processName)
            yield ', в системе '
            b('Kcell Workflow')
            yield ' пользователь '
            b(starter)
            yield ' создал(а) заявку #'
            b(businessKey)
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
                   yield 'Инициатор: '
               }
               td {
                   b(starter)
               }
            }
            if (!processName.equals("Revision")) {
                tr {
                   td {
                       yield 'Дата создания: '
                   }
                   td {
                       b(startTime)
                   }
                }
            }

        for ( e in customVariables ) {
                    tr {
                        td {
                            yield '' + e.key + ': '

                        }
                        td {
                            b(e.value)
                        }
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
            yield 'При возникновении каких-либо проблем при работе с системой, а также, пожеланий и предложений, отправьте пожалуйста письмо в '
            b {
                a(href: 'mailto:support_flow@kcell.kz?subject='+subject, 'support_flow@kcell.kz')
            }
            yield ' с описанием.'
        }
        p ('C уважением,<br>Kcell Flow')
    }
}
