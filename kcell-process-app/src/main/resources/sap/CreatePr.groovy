import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import java.text.SimpleDateFormat

def baseUrl = "http://localhost"
def jrNumber = "Alm-LSE-P&O-17-5267"
def requestDate = new Date()
def jobWorksObj = '[{"relatedSites":[{"name":"00713","id":"28421","site_name":"00713ULZHAN2","$$hashKey":"object:790"}],"sapServiceNumber":"128","materialUnit":"site/сайт","quantity":1,"materialQuantity":1}]';
def contractor = 4

def cal = Calendar.instance
def yearEndDate = "31.12."+cal.get(Calendar.YEAR)

def formatDate = new SimpleDateFormat("dd.MM.yyyy")
def jobWorks = new JsonSlurper().parseText(jobWorksObj)
def catalog = new JsonSlurper().parseText(new URL(baseUrl + "/api/catalogs").text)
def works = catalog.works
def requestDateObj = formatDate.format(requestDate)
def contractorsTitle = new JsonSlurper().parseText(this.getClass().getResource("/dictionary/contractor.json").text)

jobWorks.each { work ->
    work.definition = works.find { it.sapServiceNumber == work.sapServiceNumber };
    if ('CAPEX' == work.definition.costType){
        work.costType = 'Y'
    } else if ('OPEX' == work.definition.costType){
        work.costType = 'K'
    }
    work.contractorNo = contractorsTitle[contractor.toString()].contract.service
}
def sloc = "S666"

def binding = ["jobWorks":jobWorks, "jrNumber":jrNumber, "requestDate": requestDateObj, "yearEndDate":yearEndDate, "sloc":sloc]

/*
FIELD DESCRIPTION	 For FA PRs (CAPEX)	    For Service PRs (OPEX)	Примечание
PR document type	 ZK73-01	            ZK73-01	                По умолчанию
Cost Type	         Y	                    K	                    в каждой строчке в зависитмости от работы
KWMS Number	         22092016101230	        22092016101230	        Номер JR из KWMS
KWMS Status	         approved               approved	            По умолчанию approved
KWMS Reuqest date	 22.09.2017	            22.09.2017	            Время создания JR в KWMS
Vendor	             280209	                280209	                Код вендора
                                                                    218545 ТОО Spectr energy group
                                                                    218743 ТОО AICOM
                                                                    280209 TOO Line System Engineering
                                                                    285767 ТОО Аврора Сервис
Region	             7	                    7	                    регион
Service Type	     Y	                    X	                    X Roll Out (CAPEX)
                                                                    Y Revision Works (CAPEX/OPEX)
                                                                    Z Electrical Works (CAPEX/OPEX)
PR Item text	     installation service	installation service	Краткое описание работы
CM Contract no	     56357	                56357	                56477 SEG Mat+Serv
                                                                    56357 LSE Mat+Serv
                                                                    56449 Aicom Mat+Serv
                                                                    56486 Avrora Serv
                                                                    56556 Avrora Mat
Service number	     1	                    1	                    Номер заказываемой работы по контракту
Delivery Date	     21.09.2017	            21.09.2017	            Взять дату до которой нужно выполнить работу из стартовой формы прибавить 1 неделю (31.12.currentYear)
WBS Element	         TN-0502-07-9995	    200-70160-1	            СПП из справочника works (необходимо получить справочники на 2017 и 2018 годы)
Job Order no	     A-TPOU-10-0230	        A-TPOU-10-0230	        Номер JR из KWMS (повторяется с KWMS Number) или может использовать тех номер из KWMS  в поле KWMS Number? KWMS Number – это ваш номер заявки, аналог номера PR.
                                                                    Job Order no – это номер JO, структура по созданию у вас есть.
Storage Location	 K001	                DUMMY	                Из базы Sloc          TODO
FA number	         294130000523	        DUMMY	                З шага «Get FA»       TODO
Cost center	         25510	                25510	                Нужно получить у бухгалтера? TODO
Controlling Area	 3020	                3020	                Нужно получить у бухгалтера? TODO
Activity Service Number	DUMMY	            7016000	                Нужно получить у бухгалтера? TODO
Price			    стоимость плюс транспортировка для трансмиссионных работ стоимость будет делиться в случае участия нескольких сайтов (считаться по формуле) TODO
User		        Из нового формата текстового файла	Зарегестрированный пользователь (необходим список) TODO
*/

def template = '''\
jobWorks.each { w ->
    yield 'ZK73-01\t' + w.costType + '\t' + jrNumber + '\tapproved\t' + requestDate + '\t' + w.definition.vendor + '\t' + w.definition.region.id +
          '\tY\tinstallation service\t' + w.contractorNo + '\t' + w.definition.sapServiceNumber + '\t' + yearEndDate + '\t' +
          w.definition.spp + '\t' + jrNumber + '\t' + sloc + '\t294130000523\t25510\t3020\t7016000 '
    newLine()
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
