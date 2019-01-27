yieldUnescaped '<!DOCTYPE html>'
def processName = delegateTask.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateTask.getProcessDefinitionId()).getName()
def businessKey = delegateTask.getExecution().getProcessBusinessKey()
def legalInfo = delegateTask.getVariable("legalInfo").unwrap()
def workTypeJson = delegateTask.getVariable("workType").unwrap()
def workType = ''
for (int i = 0; i < workTypeJson.size(); i++) {
    if (i > 0) workType += ', '
    workType += workTypeJson[i].asText()
}
html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        p {
            yield 'В рамках процесса рассмотрения заявок по ' + processName + ', в системе Kcell Workflow создана заявка ' + businessKey + '.'
        }
        p {
            yield 'Process: ' + processName
            br()
            yield 'Client: ' + legalInfo.get('legalName').asText() + ' ' + delegateTask.getVariable("BIN")
            br()
            yield 'Service: PBX'
            br()
            yield 'Type of work: ' + workType
            br()
            yield 'Priority: ' + legalInfo.get('clientPriority').asText()
            br()
        }
        p {
            yield 'Открыть Kcell Workflow Вы можете пройдя по следующей ссылке: '
            b {
                a(href: taskUrl, 'https://flow.kcell.kz')
            }
        }
        p {
            yield 'Для входа в систему используйте свой корпоративный логин (Name.Surname@kcell.kz)* и пароль.'
            br()
            yield '*-имя и фамилия в логине начинается с заглавной буквы. Например: '
            b {
                a(href: 'mailto:Petr.Petrov@kcell.kz', 'Petr.Petrov@kcell.kz')
            }
        }
        p {
        }
        p {
            yield 'При возникновении каких-либо проблем в работе с системой, отправьте письмо в '
            b {
                a(href: 'mailto:support_flow@kcell.kz', 'support_flow@kcell.kz')
            }
            yield ' с описанием возникшей проблемы.'
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
