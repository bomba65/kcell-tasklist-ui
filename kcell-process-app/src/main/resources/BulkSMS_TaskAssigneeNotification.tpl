yieldUnescaped '<!DOCTYPE html>'
def processName = delegateTask.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateTask.getProcessDefinitionId()).getName()
html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        h2{ yield processName }
        p {
            yield 'В рамках процесса ' + processName + ', в системе Kcell Workflow создана заявка, ожидающая вашего участия. Для просмотра заявки необходимо пройти по следующей ссылке:'
            a(href: taskUrl, taskUrl)
        }
        p {
            yield 'Инициатор: ' + delegateTask.getVariable("starter") + '.'
            br()
            if(delegateTask.getVariable("finalID")) {
                yield 'Идентификатор: ' + delegateTask.getVariable("finalID") + '.'
                br()
            }
            yield 'Process Instance ID: ' + delegateTask.processInstanceId
            br()
            yield 'Task name: ' + delegateTask.name
        }
        p {
            yield 'Открыть Kcell Workflow вы можете пройдя по следующей ссылке: '
            b {
                a(href: 'https://flow.kcell.kz', 'https://flow.kcell.kz')
            }
        }
        p {
            yield 'Для входа в систему используйте свой корпоративный логин (Name.Surname@kcell.kz)* и пароль.'
        }
        p {
            yield 'При возникновении каких-либо проблем в работе с системой, отправьте письмо в '
            b {
                a(href: 'mailto:support_flow@kcell.kz', 'support_flow@kcell.kz')
            }
            yield ' с описанием возникшей проблемы.'
        }
        p {
            yield '*-имя и фамилию в логине нужно писать с заглавной буквы. Например: '
            b {
                a(href: 'mailto:Petr.Petrov@kcell.kz', 'Petr.Petrov@kcell.kz')
            }
        }
        p {
            yield 'Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы:'
            a(href : 'https://hub.kcell.kz/x/kYNoAg', 'https://hub.kcell.kz/x/kYNoAg')
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
