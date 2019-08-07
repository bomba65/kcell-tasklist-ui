package revision

import org.camunda.bpm.engine.delegate.DelegateExecution

def getUserEmail(DelegateExecution execution, String userId) {
    def identityService = execution.processEngineServices.identityService
    def userList = identityService.createUserQuery().userId(userId).list()
    userList[0]?.email
}

getUserEmail(execution, starter)
