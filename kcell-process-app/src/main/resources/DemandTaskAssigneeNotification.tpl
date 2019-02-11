yieldUnescaped '<!DOCTYPE html>'

def processName = delegateTask.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateTask.getProcessDefinitionId()).getName()
edf procInst = delegateTask.getProcessEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(delegateTask.getProcessInstanceId()).singleResult()
def startTime = new Date()
if (procInst != null) startTime = procInst.getStartTime()
def general = delegateTask.getVariable('generalData').unwrap().get('general')

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p('В рамках процесса рассмотрения заявок по <b>' + processName + '</b>, в системе <b>Kcell Workflow</b> на Вас назначена активность <b>' + delegateTask.getName() + '</b> по заявке # <b>' + businessKey + '</b>, ожидающая Вашего участия.')
        newLine()
        p {
            yield 'Для просмотра заявки Вам необходимо пройти по следующей '
            a(href: taskUrl, 'ссылке')
            yield '.'
        }
        newLine()
        table {
            tr {
                td('<b>Процесс:</b> ')
                td(processName)
            }
            tr {
                td('<b>Номер заявки:</b> ')
                td(businessKey)
            }
            tr {
                td('<b>Статус:</b> ')
                td(delegateTask.getVariable('status'))
            }
            tr {
                td('<b>Инициатор:</b> ')
                td(general.get('demandOwner').asText())
            }
            tr {
                td('<b>Дата создания:</b> ')
                td(startTime.format('dd.MM.yyyy HH:mm'))
            }
            tr {
                td('<b>Имя заявки:</b> ')
                td(delegateTask.getVariable('demandName'))
            }
            tr {
                td('<b>Описание:</b> ')
                td(general.get('description').asText())
            }
        }
        newLine()

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
                a(href: 'mailto:support_flow@kcell.kz?subject='+subject, 'support_flow@kcell.kz')
            }
            yield ' с описанием.'
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
