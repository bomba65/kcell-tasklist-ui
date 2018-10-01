import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

/*
def reason = "1";
def jobWorks = '[{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1"}],"sapServiceNumber":"8","materialUnit":"site/сайт","quantity":2,"materialQuantity":1,"expenseType":"CAPEX","fixedAssetClass":"234234234","fixedAssetNumber":"234234234","costCenter":1,"controllingArea":2},{"relatedSites":[{"name":"00SITE1","id":"1","site_name":"00SITE1","$$hashKey":"object:1979"}],"sapServiceNumber":"12","materialUnit":"pc/шт","quantity":2,"materialQuantity":1,"definition":{"id":12,"costType":"CAPEX","contractor":{"id":5,"name":"JSC Kcell"},"region":{"id":7,"name":"Almaty"},"service":{"id":2,"name":"Rollout","sapCode":"Y"},"sapServiceNumber":"12","sapPOServiceName":"12.BattBackSys chg/repl on-air (^2 sets)","displayServiceName":"12.Battery Backup System change/replacement with on-air if required ( up to 2 sets) / Замена/перемещение системы резервных батарей с активацией, если необходимо (до 2 комплектов).","currency":"KZT","faClass":"29403422","materialGroup":"STD0025","year":2016,"spp":"RN-0502-33-0067","createDate":1496599200000,"units":"pc/шт","vendor":"280209"},"fixedAssetClass":"2342342","fixedAssetNumber":"234234234","costCenter":3,"controllingArea":4}]';
def sloc = 'S333'
def workDefinitionMap = '{"8":{"id":8,"contractor":{"id":5,"name":"JSC Kcell"},"region":{"id":7,"name":"Almaty"},"service":{"id":2,"name":"Rollout","sapCode":"Y"},"sapServiceNumber":"8","sapPOServiceName":"8.Site chge/repl (All equips)","displayServiceName":"8.Site change/replacement (All equipments, RF &TR& Infrastructure including packaging according Kcell required) / Замена/перемещение  сайта (все оборудование, RF &TR& Infrastructure, включая упаковку согласно стандартам Kcell)","currency":"KZT","faClass":"29403422","materialGroup":"STD0021","year":2016,"spp":"RN-0502-33-0067","createDate":1496599200000,"units":"site/сайт","vendor":"280209"}}'
*/

def jobWorksObj = new JsonSlurper().parseText(jobWorks.toString())
def tnuSiteLocationsObj = new JsonSlurper();
if('2' == reason){
    tnuSiteLocationsObj = new JsonSlurper().parseText(tnuSiteLocations.toString())
}
def workDefinitionMapObj = new JsonSlurper().parseText(workDefinitionMap.toString())

def binding = ["jobWorksObj":jobWorksObj, "sloc":sloc, "reason": reason, "tnuSiteLocations":tnuSiteLocationsObj, "workDefinitionMap":workDefinitionMapObj]

print 'CreateFA:'
print workDefinitionMap

def template = '''\
if (reason == '2') {
    jobWorksObj.each { w ->
        w.relatedSites.each { r ->
            yieldUnescaped '' + workDefinitionMap[w.sapServiceNumber].faClass + '\t' + tnuSiteLocations[r.id].siteLocation
            newLine()
        }
    }    
} else {
    jobWorksObj.each { w ->
        yieldUnescaped '' + workDefinitionMap[w.sapServiceNumber].faClass + '\t' + sloc
        newLine()
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
