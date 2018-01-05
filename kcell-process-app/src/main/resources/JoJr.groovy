import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

import java.text.SimpleDateFormat

/*
def jrNumber = "Alm-LSE-P&O-17-5267"
def contractor = 4
def site = "00596"
def site_name = "00596MARIOTDOM"
def requestedDate = new Date()//2017-10-16T14:14:37.000+0600
def sloc = "S666"
*/

def contractorsTitle = new JsonSlurper().parseText(this.getClass().getResource("/dictionary/contractor.json").text)
def subcontractorsTitle = new JsonSlurper().parseText(this.getClass().getResource("/dictionary/subcontractor.json").text)

def formatDate = new SimpleDateFormat("dd.MM.yyyy")
def jrNumberObj = (jrNumber != null ? jrNumber : '########')
def contractorObj = (contractorsTitle[contractor.toString()] != null ? contractorsTitle[contractor.toString()].code : contractorsTitle["0"].id)
def requestedDateObj = formatDate.format(requestedDate)
def subcontructerId = (subcontractorsTitle[reason] != null ? subcontractorsTitle[reason].code : "10")
def plant = "0201"

def binding = ["jrNumber" : jrNumberObj, "contractor":contractorObj, "site_name":site_name, "site":site, "subcontructerId":subcontructerId,
               "requestDate":requestedDateObj, "plant":plant, "sloc":sloc]

/*
 323-8989-DM1	323-8989 DILARA MEHMET	Z	C	X	323-8989 DILARA MEHMET TEST 2	000010	22.08.2017	0201	4131

FIELD DESCRIPTION | VALUE            | Примечание
JO Number         | ALM-MAIN-09-28   | Передаем номер JR/JO присвоенный в системе KWMS
Job Order Descr.  | REVISION         | Так как работа Revision то передаем Revision
Job Requester     | L                | 1 SEG, 2 Avrora, 3 Aicom, L LSE, M MICRO, O Others, Z ZTE также в будущем омгут быть добавлены другие подрядчики
Requester type    | Revision         | M Maintenance, N New Site, O Optimization, R Revision
Status            | Approved         | По умолчанию (так как это статус JR и мы передаем запрос на создание PR только после получения апрува)
Job Detail        | ALMATY,ALIMZH51A | Краткое описание работы? Подставить siteid и sitename
Subcontructer ID  | 10               | Из списка SubcontractorID
Job Order Date    | 22.08.2017       | KWMS – creation date
Plant             | 0201             | 0201 ALMATY для всех регионов по умолчанию
SLoc              | К001             | Sloc from "ZKZMM0022 - Site List"

*/

def template = '''\
yieldUnescaped '' + jrNumber + '\t' + 'REVISION\t' + contractor + '\tRevision\tApproved\t' + site + ',' + site_name + '\t' + subcontructerId + '\t' + requestDate + '\t' + plant + '\t' + sloc
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
