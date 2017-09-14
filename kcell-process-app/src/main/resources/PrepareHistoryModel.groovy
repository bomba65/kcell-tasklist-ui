import groovy.json.JsonSlurperClassic
import org.camunda.bpm.engine.delegate.DelegateExecution

def getUserEmail(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def jsonSlurper = new JsonSlurperClassic()
    def resolutions = jsonSlurper.parseText(execution.getVariable("resolutions").toString())

    def taskMap = [:]
    def assigneeMap = [:]

    resolutions.each {
        def user = identityService.createUserQuery().userId(it["assignee"]).singleResult()
        assigneeMap.put(user.id, (user.firstName != null ? user.firstName : "")+" " +(user.lastName !=null ? user.lastName : ""))
        taskMap.put(it.taskId, execution.processEngineServices.historyService.createHistoricTaskInstanceQuery().taskId(it.taskId).singleResult())
    }

    def historyModel = [:]
    historyModel.put("assigneesMap", assigneeMap)
    historyModel.put("tasksMap", taskMap)
    println "_________before historyModel"
    println historyModel
    println "_________after historyModel"
    historyModel
}

getUserEmail(execution)