package revision

import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

/*
def reason = "1";
def jobWorks = '[{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1"}],"sapServiceNumber":"8","materialUnit":"site/сайт","quantity":2,"materialQuantity":1,"expenseType":"CAPEX","fixedAssetClass":"234234234","fixedAssetNumber":"234234234","costCenter":1,"controllingArea":2},{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1","$$hashKey":"object:1979"}],"sapServiceNumber":"12","materialUnit":"pc/шт","quantity":2,"materialQuantity":1,"definition":{"id":12,"costType":"CAPEX","contractor":{"id":5,"name":"Kcell_region"},"region":{"id":7,"name":"Almaty"},"service":{"id":2,"name":"Rollout","sapCode":"Y"},"sapServiceNumber":"12","sapPOServiceName":"12.BattBackSys chg/repl on-air (^2 sets)","displayServiceName":"12.Battery Backup System change/replacement with on-air if required ( up to 2 sets) / Замена/перемещение системы резервных батарей с активацией, если необходимо (до 2 комплектов).","currency":"KZT","faClass":"29403422","materialGroup":"STD0025","year":2016,"spp":"RN-0502-33-0067","createDate":1496599200000,"units":"pc/шт","vendor":"280209"},"fixedAssetClass":"2342342","fixedAssetNumber":"234234234","costCenter":3,"controllingArea":4}]';
def sloc = 'S333'
def workDefinitionMap = '{"8":{"id":8,"contractor":{"id":5,"name":"Kcell_region"},"region":{"id":7,"name":"Almaty"},"service":{"id":2,"name":"Rollout","sapCode":"Y"},"sapServiceNumber":"8","sapPOServiceName":"8.Site chge/repl (All equips)","displayServiceName":"8.Site change/replacement (All equipments, RF &TR& Infrastructure including packaging according Kcell required) / Замена/перемещение  сайта (все оборудование, RF &TR& Infrastructure, включая упаковку согласно стандартам Kcell)","currency":"KZT","faClass":"29403422","materialGroup":"STD0021","year":2016,"spp":"RN-0502-33-0067","createDate":1496599200000,"units":"site/сайт","vendor":"280209"}}'
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

def jobWorksObj = new JsonSlurper().parseText(jobWorks.toString())
def tnuSiteLocationsObj = new JsonSlurper();
if('2' == reason){
    tnuSiteLocationsObj = new JsonSlurper().parseText(tnuSiteLocations.toString())
}
def workDefinitionMapObj = new JsonSlurper().parseText(workDefinitionMap.toString())

def binding = ["jobWorksObj":jobWorksObj, "sloc":sloc, "reason": reason, "tnuSiteLocations":tnuSiteLocationsObj, "workDefinitionMap":workDefinitionMapObj,
               "capexWorks":capexWorks, "undefinedWorks":undefinedWorks, "opexWorks":opexWorks]

def template = '''\
if (reason == '2') {
    jobWorksObj.each { w ->
        if (capexWorks.contains(w.sapServiceNumber) || (undefinedWorks.contains(w.sapServiceNumber) && w.expenseType == 'CAPEX')) {
            w.relatedSites.each { r ->
                yieldUnescaped '' + workDefinitionMap[w.sapServiceNumber].faClass + '\t' + tnuSiteLocations[r.site_name].siteLocation
                newLine()
            }
        }
    }    
} else {
    if (capexWorks.contains(w.sapServiceNumber) || (undefinedWorks.contains(w.sapServiceNumber) && w.expenseType == 'CAPEX')) {
        jobWorksObj.each { w ->
            yieldUnescaped '' + workDefinitionMap[w.sapServiceNumber].faClass + '\t' + sloc
            newLine()
        }
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

print result

result
