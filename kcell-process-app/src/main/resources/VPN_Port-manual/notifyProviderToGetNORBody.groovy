import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def channel = execution.getVariable("channel").toString()
def request_type = execution.getVariable("request_type").toString()
def request_number = execution.processBusinessKey
def initiator = execution.getVariable("starter").toString()
def priority = execution.getVariable("priority").toString()
def addedPorts = new JsonSlurper().parseText(execution.getVariable("addedPorts").toString())
def addedServices = new JsonSlurper().parseText(execution.getVariable("addedServices").toString())
def modifyPorts = new JsonSlurper().parseText(execution.getVariable("modifyPorts").toString())
def modifyServices = new JsonSlurper().parseText(execution.getVariable("modifyServices").toString())
def disbandPorts = new JsonSlurper().parseText(execution.getVariable("disbandPorts").toString())
def disbandServices = new JsonSlurper().parseText(execution.getVariable("disbandServices").toString())

def channel_rus
if (channel == "Port") {
    channel_rus = "порта"
} else {
    channel_rus = "VPN"
}

def request_type_rus
if (request_type == "Organize") {
    request_type_rus = "организовать"
} else if (request_type == "Modify") {
    request_type_rus = "изменить"
} else if (request_type == "Disband") {
    request_type_rus = "расформировать"
}

def makeStringAddress(address) {
    def addressString = ""
    if (address != null) {
        if (address.city_id != null) {
            if (address.city_id.district_id != null) {
                if (address.city_id.district_id.oblast_id != null) {
                    if (address.city_id.district_id.oblast_id.name != null) {
                        addressString += address.city_id.district_id.oblast_id.name
                    }
                }
                if (address.city_id.district_id.name != null) {
                    addressString += " " + address.city_id.district_id.name
                }
            }
            if (address.city_id.name != null) {
                addressString += " " + address.city_id.name
            }
        }
        if (address.street != null) {
            addressString += " " + address.street
        }
        if (address.building != null) {
            addressString += " " + address.building
        }
    }
    addressString
}

for (port in addedPorts) {
    port.port_termination_point = makeStringAddress(port.port_termination_point)
}
for (port in modifyPorts) {
    port.port_termination_point = makeStringAddress(port.port_termination_point)
}
for (port in disbandPorts) {
    port.port_termination_point = makeStringAddress(port.port_termination_point)
}

for (service in addedServices) {
    service.port.port_termination_point = service?.port?.port_termination_point != null ? makeStringAddress(service.port.port_termination_point) : null
    service.vpn_termination_point_2 = makeStringAddress(service.vpn_termination_point_2)
}
for (service in modifyServices) {
    service.port.port_termination_point = service?.port?.port_termination_point != null ? makeStringAddress(service.port.port_termination_point) : null
    service.vpn_termination_point_2 = makeStringAddress(service.vpn_termination_point_2)
}
for (service in disbandServices) {
    service.port.port_termination_point = service?.port?.port_termination_point != null ? makeStringAddress(service.port.port_termination_point) : null
    service.vpn_termination_point_2 = makeStringAddress(service.vpn_termination_point_2)
}

def link = System.getenv('CATALOG_URL') ?: 'https://catalogs.test-flow.kcell.kz'
def url = new URL(link + "/camunda/catalogs/api/get/id/83")
def connection = url.openConnection()
connection.setRequestMethod("GET")

def response = connection.getInputStream().getText()
def responseJson = new JsonSlurper().parseText(response)
def serviceCatalog = responseJson.data.$list

def table
if (request_type == "Organize") {
    if (channel == "Port") {
        table = '''
                table (border:"1") {
                    thead {
                        tr {
                            th('#')
                            th('Port ID')
                            th('Channel type')
                            th('Port type')
                            th('Capacity')
                            th('Capacity unit')
                            th('Termination point')
                        }
                    }
                    tbody {
                        addedPorts.eachWithIndex { port, index ->
                            tr {
                                td(index + 1)
                                td(port.port_number)
                                td(port.channel_type)
                                td(port.port_type)
                                td(port.port_capacity)
                                td(port.port_capacity_unit)
                                td(port.port_termination_point)
                            }
                        }
                    }
                }
                '''
    } else {
        for (service in addedServices) {
            if (service.service_type_id != null) {
                service.type = serviceCatalog.find { it.id == service.service_type_id }.value
            }
        }
        table = '''
                table (border:"1") {
                    thead {
                        tr {
                            th('#')
                            th('Service')
                            th('Service Type')
                            th('Capacity (Mbit/s)')
                            th('VLAN')
                            th('Provider IP')
                            th('Kcell IP')
                            th('Provider AS')
                            th('Kcell AS')
                            th('Port ID')
                            th('Channel Type')
                            th('Port Type')
                            th('Termination point 1')
                            th('Termination point 2')
                        }
                    }
                    tbody {
                        addedServices.eachWithIndex { service, index ->
                            tr {
                                td(index + 1)
                                td(service.service)
                                td(service.type)
                                td(service.service_capacity)
                                td(service.vlan)
                                td(service.provider_ip)
                                td(service.kcell_ip)
                                td(service.provider_as)
                                td(service.kcell_as)
                                td(service.port?.port_number)
                                td(service.port?.channel_type)
                                td(service.port?.port_type)
                                td(service.port?.port_termination_point)
                                td(service.vpn_termination_point_2)
                            }
                        }
                    }
                }
                '''
    }
} else if (request_type == "Modify") {
    if (channel == "Port") {
        table = '''
                table (border:"1") {
                    thead {
                        tr {
                            th('#')
                            th('Port ID')
                            th('Channel type')
                            th('Port type')
                            th('Capacity')
                            th('Capacity unit')
                            th('Modified Capacity')
                            th('Modified Capacity unit')
                            th('Termination point')
                        }
                    }
                    tbody {
                        modifyPorts.eachWithIndex { port, index ->
                            tr {
                                td(index + 1)
                                td(port.port_number)
                                td(port.channel_type)
                                td(port.port_type)
                                td(port.port_capacity)
                                td(port.port_capacity_unit)
                                td(port.modified_port_capacity)
                                td(port.modified_port_capacity_unit)
                                td(port.port_termination_point)
                            }
                        }
                    }
                }
                '''
    } else {
        for (service in modifyServices) {
            if (service.service_type_id != null) {
                service.type = serviceCatalog.find { it.id == service.service_type_id }.value
            }
        }
        table = '''
                table (border:"1") {
                    thead {
                        tr {
                            th('#')
                            th('Service')
                            th('Service Type')
                            th('Capacity (Mbit/s)')
                            th('Modified Capacity (Mbit/s)')
                            th('VLAN')
                            th('Provider IP')
                            th('Kcell IP')
                            th('Provider AS')
                            th('Kcell AS')
                            th('Port ID')
                            th('Channel Type')
                            th('Port Type')
                            th('Termination point 1')
                            th('Termination point 2')
                        }
                    }
                    tbody {
                        modifyServices.eachWithIndex { service, index ->
                            tr {
                                td(index + 1)
                                td(service.service)
                                td(service.type)
                                td(service.service_capacity)
                                td(service.modified_service_capacity)
                                td(service.vlan)
                                td(service.provider_ip)
                                td(service.kcell_ip)
                                td(service.provider_as)
                                td(service.kcell_as)
                                td(service.port?.port_number)
                                td(service.port?.channel_type)
                                td(service.port?.port_type)
                                td(service.port?.port_termination_point)
                                td(service.vpn_termination_point_2)
                            }
                        }
                    }
                }
                '''
    }
} else if (request_type == "Disband") {
    if (channel == "Port") {
        table = '''
                table (border:"1") {
                    thead {
                        tr {
                            th('#')
                            th('Port ID')
                            th('Channel type')
                            th('Port type')
                            th('Capacity')
                            th('Capacity unit')
                            th('Termination point')
                        }
                    }
                    tbody {
                        disbandPorts.eachWithIndex { port, index ->
                            tr {
                                td(index + 1)
                                td(port.port_number)
                                td(port.channel_type)
                                td(port.port_type)
                                td(port.port_capacity)
                                td(port.port_capacity_unit)
                                td(port.port_termination_point)
                            }
                        }
                    }
                }
                '''
    } else {
        for (service in disbandServices) {
            if (service.service_type_id != null) {
                service.type = serviceCatalog.find { it.id == service.service_type_id }.value
            }
        }
        table = '''
                table (border:"1") {
                    thead {
                        tr {
                            th('#')
                            th('VPN ID')
                            th('Service')
                            th('Service Type')
                            th('Capacity (Mbit/s)')
                            th('VLAN')
                            th('Provider IP')
                            th('Kcell IP')
                            th('Provider AS')
                            th('Kcell AS')
                            th('Port ID')
                            th('Channel Type')
                            th('Port Type')
                            th('Termination point 1')
                            th('Termination point 2')
                        }
                    }
                    tbody {
                        disbandServices.eachWithIndex { service, index ->
                            tr {
                                td(index + 1)
                                td(service.vpn_number)
                                td(service.service)
                                td(service.type)
                                td(service.service_capacity)
                                td(service.vlan)
                                td(service.provider_ip)
                                td(service.kcell_ip)
                                td(service.provider_as)
                                td(service.kcell_as)
                                td(service.port?.port_number)
                                td(service.port?.channel_type)
                                td(service.port?.port_type)
                                td(service.port?.port_termination_point)
                                td(service.vpn_termination_point_2)
                            }
                        }
                    }
                }
                '''
    }
}

def binding = ["request_number"  : request_number,
               "request_type_rus"   : request_type_rus,
               "channel_rus"        : channel_rus,
               "initiator"          : initiator,
               "priority"           : priority,
               "addedPorts"         : addedPorts,
               "addedServices"      : addedServices,
               "modifyPorts"        : modifyPorts,
               "modifyServices"     : modifyServices,
               "disbandPorts"      : disbandPorts,
               "disbandServices"   : disbandServices]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "АО «Кселл» в рамках заключенного Договора № 30/21-V от 22.02.2021 г., по тарифному плану «EXT-3», просит " + request_type_rus + " " + channel_rus + " в населенных пунктах и с техническими параметрами, указанными в Таблице №1."
        }
        p {
            span("Таблица №1")
        }
''' + table +
'''
        p {
            span("Запрос # " + request_number)
        }
        p {
            span("Инициатор: " + initiator)
        }
        p {
            span("Статус: " + priority)
        }
        p {
            span("Срок и время проведения работ просим согласовать с представителями АО «Кселл»:")
        }        
        p {
            span("- Дежурный инженер, +7 701 211 49 83, kcellnor@kcell.kz")
        }
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result

