def request_number = execution.processBusinessKey
def confirmerComments = execution.getVariable("confirmRequestTaskComment").toString()

def res = "Автоматический запрос # " + request_number + " на расширение VPN отклонен по следующей причине: " + confirmerComments

res

