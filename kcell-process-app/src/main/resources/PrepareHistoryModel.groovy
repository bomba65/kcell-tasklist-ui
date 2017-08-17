import groovy.json.JsonSlurperClassic
import org.camunda.bpm.engine.delegate.DelegateExecution

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def jsonSlurper = new JsonSlurperClassic()
    println "resolutions_________"
    println execution.getVariable("resolutions")
    println execution.getVariable("resolutions").toString()

    def resolutions = jsonSlurper.parseText(execution.getVariable("resolutions").toString())
    println "_________resolutions"
    println resolutions
    println "_________resolutions12313"

    def taskMap = [:]
    def assigneeMap = [:]

    resolutions.each {
        println it
        def user = identityService.createUserQuery().userId(it["assignee"]).singleResult()
        println user
        assigneeMap.put(user.id, (user.firstName != null ? user.firstName : "")+" " +(user.lastName !=null ? user.lastName : ""))
        taskMap.put(it.taskId, execution.processEngineServices.historyService.createHistoricTaskInstanceQuery().taskId(it.taskId).singleResult())
    }

    def historyModel = [:]
    historyModel.put("assigneesMap", assigneeMap)
    historyModel.put("tasksMap", taskMap)
    historyModel
}

getUserEmail(execution)