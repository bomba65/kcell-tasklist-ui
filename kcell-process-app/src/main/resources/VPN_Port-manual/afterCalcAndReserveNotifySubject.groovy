def channel = execution.getVariable("channel").toString()
def request_type = execution.getVariable("request_type").toString()
def request_number = execution.processBusinessKey

def channel_rus
if (channel == "Port") {
    channel_rus = "порта"
} else {
    channel_rus = "VPN"
}

def request_type_rus
if (request_type == "Organize") {
    request_type_rus = "организацию"
} else if (request_type == "Modify") {
    request_type_rus = "изменение"
} else if (request_type == "Disband") {
    request_type_rus = "расформирование"
}

def res = "Запрос на подтверждение заявки на " + request_type_rus + " " + channel_rus + " # " + request_number

res

