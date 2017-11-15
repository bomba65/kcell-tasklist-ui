import groovy.json.JsonSlurper
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.Variables

def sharingPlanObj = new JsonSlurper().parseText(sharingPlan.toString())

def binding = [
        "execution": execution,
        "starter": starter,
        "sharingPlan": sharingPlanObj
]

//def template = this.getClass().getResource(templateName).text

def template = '''\
yieldUnescaped '<!DOCTYPE html>'

html(lang:'en') {
    head {
        meta('http-equiv':'"Content-Type" content="text/html; charset=utf-8"')
        title('My page')
    }
    newLine()
    style ('td { padding: 10px; }')
    newLine()
    body {
        p('Hi,' + starter)
        newLine()
        //p('Create PR for Job Request ' + jrNumber)
        newLine()
//        table {
//            tr {
//                td {
//                    b('Initiator')
//                    yield ': ' + initiatorFull.firstName + ' ' + initiatorFull.lastName
//                }
//                td {
//                    b('JR Number')
//                    yield ': ' + jrNumber
//                }
//                td()
//            }
//            tr {
//                td {
//                    b('Requested date')
//                    yield ': ' + requestedDate
//                }
//                td {
//                    b('Reason')
//                    yield ': ' + reasons
//                }
//                td{
//                    b('Materials required')
//                    yield ': ' + materialsRequired
//                }
//            }
//            tr {
//                td {
//                    b('Validity date')
//                    yield ': ' + validityDate
//                }
//                td {
//                    b('Contract')
//                    yield ': ' + service
//                }
//                td {
//                    b('Leasing required')
//                    yield ': ' + leasingRequired
//                }
//            }
//            tr {
//                td {
//                    b('SA&O Complaint Id')
//                    yield ': ' + soaComplaintId
//                }
//                td {
//                    b('Contractor')
//                    yield ': ' + contractor
//                }
//                td {
//                    b('Site (near end)')
//                    yield ': ' + siteName
//                }
//            }            
//            tr {
//                td {
//                    b('Power engineering required')
//                    yield ': ' + powerRequired
//                }
//                td {
//                    b('Project')
//                    yield ': ' + project
//                }
//                td {
//                    b('Site Name')
//                    yield ': ' + site_name
//                }
//            }
//            tr {
//                td {
//                    b('Works')
//                    yield ': '
//                }
//                td('')
//                td('')
//            }   
//            tr {
//                td(colspan: '3') {
//                    table (class : 'table table-condensed') {
//                        thead {
//                            tr { 
//                                th(width: '30%', 'Works')
//                                th('Works Qty')
//                                th('Materials Qty')
//                                th('Sites')
//                                th('Base Price')
//                                th('Base + Transportation +8%')
//                                th('1 work price per site Sum/site q-ty')
//                                th('Sum * works q-ty')
//                                th('Total')                            
//                            }
//                        }
//                        tbody {
//                            jobWorks.each { work ->
//                                tr {
//                                    td(worksTitle[work.sapServiceNumber])
//                                    td(work.quantity!=null?work.quantity:'')
//                                    td(work.materialQuantity!=null?work.materialQuantity:'')
//                                    td {
//                                        work.relatedSites.each { rs -> 
//                                            yield '' + (rs.site_name == site_name? 'Main Site: ':'') + rs.site_name + ','
//                                        }
//                                    }
//                                    td((work.unitWorkPrice!=null?work.unitWorkPrice:'N/A') + '&nbsp;&#8376;\')
//                                    td((work.unitWorkPricePlusTx!=null?work.unitWorkPricePlusTx:'N/A') + '&nbsp;&#8376;\')
//                                    td((work.unitWorkPricePerSite!=null?work.unitWorkPricePerSite:'N/A') + '&nbsp;&#8376;\')
//                                    td((work.netWorkPricePerSite!=null?work.netWorkPricePerSite:'N/A') + '&nbsp;&#8376;')
//                                    td(work.total!=null?work.total:'N/A')                                
//                                }
//                            }
//                            tr {
//                                th(colspan : '7', style : 'text-align: left;', 'Total')
//                                th()
//                                th(jobWorksTotal)
//                            }
//                        }
//                    }
//                }
//            }
//            tr {
//               td(colspan : '3'){
//                    b('Explanation of works')
//                    yield ': ' + explanation                    
//               } 
//            }   
//            tr {
//               td(colspan : '3'){
//                    h3('History')
//                    table {
//                        thead {
//                            th('Activity')
//                            th('Assignee')
//                            th('Resolution')
//                            th('Comment')                            
//                        }
//                        tbody {
//                            resolutions.each { resolution ->
//                                tr {
//                                    td(tasksMap[resolution.taskId]?.name!=null?tasksMap[resolution.taskId].name:'N/A')
//                                    td(resolution.assigneeName!=null? resolution.assigneeName:resolution.assignee)
//                                    td(i18n["task.resolution."+resolution.resolution]!=null?i18n["task.resolution."+resolution.resolution]: 'N/A')
//                                    td(tasksMap[resolution.taskId]?.endTime!=null?formatDateTime.format(tasksMap[resolution.taskId].endTime):'N/A')
//                                    td(resolution.comment!=null?resolution.comment:'N/A')
//                                }
//                            }
//                        }
//                    }
//               }                
//            }
//        }
        newLine()
        p {
            yield 'Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы:'
            a(href : 'https://hub.kcell.kz/x/kYNoAg', 'https://hub.kcell.kz/x/kYNoAg')
        }
        p ('Greetings,<br>Kcell Flow')
    }
}
'''

def config = new TemplateConfiguration()
config.setAutoNewLine(true)
config.setAutoIndent(true)

def engine = new MarkupTemplateEngine(config)
def result = engine.createTemplate(template).make(binding).toString()

execution.setVariable("actOfAcceptanceDocument", Variables.fileValue("actOfAcceptance.doc").file(result.getBytes("utf-8")).mimeType("application/msword").create());
