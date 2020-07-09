package revision

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import java.text.SimpleDateFormat

/*
def jrNumber = "Alm-LSE-P&O-17-5267"
def requestedDate = new Date()
def jobWorks = '[{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1"}],"sapServiceNumber":"8","materialUnit":"site/сайт","quantity":2,"materialQuantity":1,"expenseType":"CAPEX","fixedAssetClass":"234234234","fixedAssetNumber":"234234234","costCenter":1,"controllingArea":2},{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1","$$hashKey":"object:1979"}],"sapServiceNumber":"12","materialUnit":"pc/шт","quantity":2,"materialQuantity":1,"definition":{"id":12,"costType":"CAPEX","contractor":{"id":5,"name":"Kcell_region"},"region":{"id":7,"name":"Almaty"},"service":{"id":2,"name":"Rollout","sapCode":"Y"},"sapServiceNumber":"12","sapPOServiceName":"12.BattBackSys chg/repl on-air (^2 sets)","displayServiceName":"12.Battery Backup System change/replacement with on-air if required ( up to 2 sets) / Замена/перемещение системы резервных батарей с активацией, если необходимо (до 2 комплектов).","currency":"KZT","faClass":"29403422","materialGroup":"STD0025","year":2016,"spp":"RN-0502-33-0067","createDate":1496599200000,"units":"pc/шт","vendor":"280209"},"fixedAssetClass":"2342342","fixedAssetNumber":"234234234","costCenter":3,"controllingArea":4}]';
def contractor = 4
def sloc = 'S333'
def workPrices = '[{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1"}],"sapServiceNumber":"8","materialUnit":"site/сайт","quantity":2,"materialQuantity":1,"unitWorkPricePerSite":"669468.24","netWorkPricePerSite":"1338936.48","unitWorkPrice":"619878.00","unitWorkPricePlusTx":"669468.24","basePriceByQuantity":"1239756.00","total":"1338936.48","basePrice":"619878.00"},{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1"}],"sapServiceNumber":"12","materialUnit":"pc/шт","quantity":2,"materialQuantity":1,"unitWorkPricePerSite":"69337.79","netWorkPricePerSite":"138675.57","unitWorkPrice":"64201.65","unitWorkPricePlusTx":"69337.79","basePriceByQuantity":"128403.30","total":"138675.57","basePrice":"64201.65"}]';
def reason = "1";
def siteRegion = "alm";
def site_name = "00SITE1";
def jobWorksTotal = "70134.76";
def sapFaList = '[{"faClass":"31231","sloc":"K207","faNumber":"111222"}]'
def workDefinitionMap = '{"8":{"id":8,"contractor":{"id":5,"name":"Kcell_region"},"region":{"id":7,"name":"Almaty"},"service":{"id":2,"name":"Rollout","sapCode":"Y"},"sapServiceNumber":"8","sapPOServiceName":"8.Site chge/repl (All equips)","displayServiceName":"8.Site change/replacement (All equipments, RF &TR& Infrastructure including packaging according Kcell required) / Замена/перемещение  сайта (все оборудование, RF &TR& Infrastructure, включая упаковку согласно стандартам Kcell)","currency":"KZT","faClass":"29403422","materialGroup":"STD0021","year":2016,"spp":"RN-0502-33-0067","createDate":1496599200000,"units":"site/сайт","vendor":"280209"}}'

"1": "Optimization works",
"2": "Transmission works",
"3": "Infrastructure works",
"4": "Operation works"
*/

def capexWorks = ['1','2','3','4','5','8','10','11','12','14','15','16','17','19','20','22','23','25','26','28','29','31','32','34',
                  '35','36','38','42','45', '46', '47', '48', '49', '50', '54', '55', '56', '57', '60', '62', '65', '66', '71', '72',
                  '77', '78', '79', '80', '81', '86', '87', '88', '91', '94', '97', '100', '103', '104', '105', '106', '112', '113',
                  '114', '115', '122', '131', '134', '138', '141', '144', '147', '150', '151', '155', '156', '157', '158', '159', '160',
                  '161', '162', '165', '168', '169', '172', '173']

def undefinedWorks = ['39', '40', '41', '43', '61', '63', '67', '68', '73', '74', '82', '84', '85', '89', '92', '95', '98', '101', '107',
                      '108', '116', '117', '118', '123', '125', '126', '127', '128', '129', '130', '132', '135', '137', '139', '142', '145',
                      '148', '152', '153', '154', '166']

def opexWorks = ['6', '7', '9', '13', '18', '21', '24', '27', '30', '33', '37', '44', '51', '52', '53', '58', '59', '64', '69', '70', '75',
                 '76', '83', '90', '93', '96', '99', '102', '109', '110', '111', '119', '120', '121', '124', '133', '136', '140', '143',
                 '146', '149', '163', '164', '167', '170', '171']
// requestedDateChangerAssigneeName
// expenseTypeAssigneeName
// prItemTextAssigneeName
// wbsElementAssigneeName
// amountTextAssigneeName
// controllingAreaAssigneeName
// activityServiceNumberAssigneeName
// wRequestedDateAssigneeName
// wdeliveryDateAssigneeName
def cal = Calendar.instance
def yearEndDate = "31.12."+cal.get(Calendar.YEAR)

def formatDate = new SimpleDateFormat("dd.MM.yyyy")
def jobWorksObj = new JsonSlurper().parseText(jobWorksValueTemp.toString())
def workPricesObj = new JsonSlurper().parseText(workPrices.toString())
def requestDateObj = formatDate.format(requestedDateObject)
def sapFaListObj = new JsonSlurper().parseText("[]")
if(hasCapexWorks == 'true'){
    sapFaListObj = new JsonSlurper().parseText(sapFaList.toString());
}
def contractorsTitle = new JsonSlurper().parseText(this.getClass().getResource("/dictionary/contractor.json").text)
def subcontractorsTitle = new JsonSlurper().parseText(this.getClass().getResource("/dictionary/subcontractor.json").text)
def subcontructerName = (subcontractorsTitle[reason] != null ? subcontractorsTitle[reason].responsible : "-")

def documentType = ["1":"ZK73-02", "2":"ZK73-03", "3":"ZK73-04", "4":"ZK73-01"]
def requestedBy = (reason == '4' ? '252' : '251')

def tnuSiteLocationsObj = new JsonSlurper();
if('2' == reason){
    tnuSiteLocationsObj = new JsonSlurper().parseText(tnuSiteLocations.toString())
}
def workDefinitionMapObj = new JsonSlurper().parseText(workDefinitionMap.toString())

jobWorksObj.each { work ->
    if(capexWorks.contains(work.sapServiceNumber)){
        work.expenseType = 'CAPEX'
    } else if(opexWorks.contains(work.sapServiceNumber)){
        work.expenseType = 'OPEX'
    }
    if ('CAPEX' == work.expenseType){
        work.costType = 'Y'
    } else if ('OPEX' == work.expenseType){
        work.costType = 'K'
    }
    work.contractorNo = contractorsTitle[contractor.toString()].contract.service
    work.costCenter = '25510'

    if(reason == '2'){
        if('CAPEX' == work.expenseType) {
            work.relatedSites.each { rs ->
                sapFaListObj.each { fa ->
                    if (fa.faClass == workDefinitionMapObj[work.sapServiceNumber].faClass && fa.sloc == tnuSiteLocationsObj[rs.site_name].siteLocation) {
                        tnuSiteLocationsObj[rs.site_name].work[work.sapServiceNumber].fixedAssetNumber = fa.faNumber
                    }
                }
            }
        }
    } else {
        if('CAPEX' == work.expenseType) {
            sapFaListObj.each { fa ->
                if (fa.faClass == workDefinitionMapObj[work.sapServiceNumber].faClass && fa.sloc == sloc) {
                    work.fixedAssetNumber = fa.faNumber
                }
            }
        }
    }
}

jobWorksObj.each { work ->
    work.price = workPricesObj.find {
        it.sapServiceNumber == work.sapServiceNumber
    }
}

def binding = ["documentType": documentType[reason],"jobWorksObj":jobWorksObj, "workPricesObj": workPricesObj, "jrNumber":jrNumber,
               "requestDate": requestDateObj, "yearEndDate":yearEndDate, "sloc":sloc, "subcontructerName":subcontructerName,
               "site_name":site_name, "requestedBy":requestedBy, "reason": reason, "tnuSiteLocations":tnuSiteLocationsObj,
               "jobWorksTotal": jobWorksTotal, "sapFaList": sapFaListObj, "workDefinitionMap":workDefinitionMapObj]

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
Quantity             2                      3                       Количество работ
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
if (reason == '2') {
    jobWorksObj.each { w ->
        yieldUnescaped '' + documentType + '\t' + w.costType + '\t' + jrNumber + '\tapproved\t' + requestDate + '\t' + workDefinitionMap[w.sapServiceNumber].vendor + '\t' + 
              '7\tY\tinstallation service ' + w.r.site_name + '\t' + w.contractorNo + '\t' + workDefinitionMap[w.sapServiceNumber].sapServiceNumber + '\t' + w.price.quantity + '\t' +
              yearEndDate + '\t' + w.wbsElement + '\t' + jrNumber + '\t' + tnuSiteLocations[w.r.site_name].siteLocation + '\t' + 
              (w.expenseType == 'CAPEX'?tnuSiteLocations[w.r.site_name].work[w.sapServiceNumber].fixedAssetNumber:'DUMMY') + '\t' +
              w.costCenter + '\t' + w.controllingArea + '\t' + w.activityServiceNumber + '\t' + w.price.unitWorkPricePerSite + '\t' +
              subcontructerName + '\t131\t' + requestedBy + '\t' +
              '1.Purchase description: Revision works for site ' + w.r.site_name + ' JR# ' + jrNumber + ' dated ' + requestDate + ' ' +
              '2.Budgeted or not: yes ' + workDefinitionMap[w.sapServiceNumber].wbsElement + ' ' +
              '3.Main project for Fintur: revision works ' +
              '4.Describe the need of this purchase for this year: necessary for revision works ' +
              '5.Contact person: ' + subcontructerName + ' ' +
              '6. Vendor: Line System Engineering LLP ' +
              '8. Total sum: ' + jobWorksTotal + ''
        newLine()
    }
} else {
    jobWorksObj.each { w ->
        yieldUnescaped '' + documentType + '\t' + w.costType + '\t' + jrNumber + '\tapproved\t' + requestDate + '\t' + workDefinitionMap[w.sapServiceNumber].vendor + '\t' + 
              '7\tY\tinstallation service ' + site_name + '\t' + w.contractorNo + '\t' + workDefinitionMap[w.sapServiceNumber].sapServiceNumber + '\t' + w.quantity + '\t' +
              yearEndDate + '\t' + w.wbsElement + '\t' + jrNumber + '\t' + sloc + '\t' + (w.fixedAssetNumber!=null?w.fixedAssetNumber:'DUMMY') + '\t' + 
              w.costCenter + '\t' + w.controllingArea + '\t' + w.activityServiceNumber + '\t' + w.price.unitWorkPricePlusTx + '\t' + 
              subcontructerName + '\t131\t' + requestedBy + '\t' +
              '1.Purchase description: Revision works for site ' + site_name + ' JR# ' + jrNumber + ' dated ' + requestDate + ' ' +
              '2.Budgeted or not: yes ' + workDefinitionMap[w.sapServiceNumber].spp + ' ' +
              '3.Main project for Fintur: revision works ' +
              '4.Describe the need of this purchase for this year: necessary for revision works ' +
              '5.Contact person: ' + subcontructerName + ' ' +
              '6. Vendor: Line System Engineering LLP ' +
              '8. Total sum: ' + jobWorksTotal + ''
        newLine()
    }
}
'''
/*
Briefly answer the following questions:
1.Purchase description: Revision works for site 00000ALMATY
JR# West-P&O-17-0048 dated 01.12.2017
2.Budgeted or not: yes
RN-0502-33-0150; 251-70160-1
CIF's # 0150 (на 2018 будет меняться каждый год)
3.Main project for Fintur: revision works
4.Describe the need of this purchase for this year: necessary for revision works
5.Contact person: Keremet Ibragimova
6. Vendor: Line System Engineering LLP
8. Total sum: ______
*/

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

result
