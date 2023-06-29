import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

def request_number = execution.processBusinessKey
def priority = execution.getVariable("priority").toString()
def automodifyServices = new JsonSlurper().parseText(execution.getVariable("automodifyServices").toString())

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

for (service in automodifyServices) {
    service.vpn_termination_point_2 = makeStringAddress(service.vpn_termination_point_2)
    service.port?.port_termination_point = makeStringAddress(service.port?.port_termination_point)
}

def link = System.getenv('CATALOG_URL') ?: 'https://catalogs.test-flow.kcell.kz'
def url = new URL(link + "/camunda/catalogs/api/get/id/83")
def connection = url.openConnection()
connection.setRequestMethod("GET")

def response = connection.getInputStream().getText()
def responseJson = new JsonSlurper().parseText(response)
def serviceCatalog = responseJson.data.$list

for (service in automodifyServices) {
    if (service.service_type_id != null) {
        service.type = serviceCatalog.find { it.id == service.service_type_id }.value
    }
}

def binding = ["request_number"  : request_number,
               "automodifyServices" : automodifyServices,
               "priority"           : priority]

def template = '''
yieldUnescaped '<!DOCTYPE html>'
html(lang:'ru') {
    head {
        meta(httpEquiv:'Content-Type', content:'text/html; charset=utf-8')
        title('My page')
    }
    body {
        p { 
            yield "На основании запроса # " + request_number + ", просим расширить VPN АО «Казахтелеком» в населенных пунктах и с техническими параметрами, указанными в Таблице №1."
        }
        p {
            span("Статус: " + priority)
        }
        p {
            yield "Со стороны АО «Кселл» к принятию в эксплуатацию Услуг - всё готово."
        }
        newLine()
        p {
            span("Таблица №1")
        }
        
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
                    th('Near end address')
                    th('Port ID')
                    th('Channel Type')
                    th('Port Type')
                    th('Far end address')
                }
            }
            tbody {
                automodifyServices.eachWithIndex { service, index ->
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
                        td(service.vpn_termination_point_2)
                        td(service.port?.port_number)
                        td(service.port?.channel_type)
                        td(service.port?.port_type)
                        td(service.port?.port_termination_point)
                    }
                }
            }
        }
        
        p {
            yield "Срок и время проведения работ просим согласовать с представителями АО «Кселл»:"
        }
        p {
            yield "- Дежурный инженер,  +7 701 211 49 83, kcellnor@kcell.kz"
        }
        p {
            yield "- Павел Романов, +7 701 211 19 60, Pavel.Romanov@kcell.kz"
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
