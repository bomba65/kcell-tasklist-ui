import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import java.text.SimpleDateFormat

/*
def jrNumber = "Alm-LSE-P&O-17-5267"
def requestedDate = new Date()
def jobWorks = '[{"relatedSites":[{"name":"00010","id":"28096","site_name":"00010OKZHETPES","$$hashKey":"object:3868"}],"sapServiceNumber":"5","materialUnit":"site/сайт","quantity":1,"materialQuantity":1,"definition":{"id":5,"costType":"CAPEX","contractor":{"id":5,"name":"JSC Kcell"},"region":{"id":7,"name":"Almaty"},"service":{"id":2,"name":"Rollout","sapCode":"Y"},"sapServiceNumber":"5","sapPOServiceName":"5.RBS/BTS add(BBS+cab+ant+feed+pole)oair","displayServiceName":"5.RBS/BTS cell addition(BBS+cabinet+antennas+feeders+poles) with on-air / Добавление сотового сегмента к  RBS/BTS (BBS+кабинет+антенны+фидер+  трубостойкb) с активацией","currency":"KZT","faClass":"29403422","materialGroup":"STD0018","year":2016,"spp":"RN-0502-33-0067","createDate":1496599200000,"units":"site/сайт","vendor":"280209"},"fixedAssetClass":"29403422","fixedAssetNumber":"29403422"}]';
def contractor = 4
def sloc = 'S333'
*/
def cal = Calendar.instance
def yearEndDate = "31.12."+cal.get(Calendar.YEAR)

def formatDate = new SimpleDateFormat("dd.MM.yyyy")
def jobWorksObj = new JsonSlurper().parseText(jobWorks.toString())
def requestDateObj = formatDate.format(requestedDate)
def contractorsTitle = new JsonSlurper().parseText(this.getClass().getResource("/dictionary/contractor.json").text)

jobWorksObj.each { work ->
    if ('CAPEX' == work.definition.costType){
        work.costType = 'Y'
    } else if ('OPEX' == work.definition.costType){
        work.costType = 'K'
    }
    work.contractorNo = contractorsTitle[contractor.toString()].contract.service
}

def binding = ["jobWorksObj":jobWorksObj, "jrNumber":jrNumber, "requestDate": requestDateObj, "yearEndDate":yearEndDate, "sloc":sloc]

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
jobWorksObj.each { w ->
    yieldUnescaped 'ZK73-01\t' + w.costType + '\t' + jrNumber + '\tapproved\t' + requestDate + '\t' + w.definition.vendor + '\t' + w.definition.region.id +
          '\tY\tinstallation service\t' + w.contractorNo + '\t' + w.definition.sapServiceNumber + '\t' + yearEndDate + '\t' +
          w.definition.spp + '\t' + jrNumber + '\t' + sloc + '\t' + (w.fixedAssetNumber!=null?w.fixedAssetNumber:'DUMMY') + '\t25510\t3020\t7016000 '
    newLine()
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
