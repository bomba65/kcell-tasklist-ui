package revision

import org.camunda.bpm.engine.delegate.DelegateExecution

def getUserEmail(DelegateExecution execution, String userId) {
    def identityService = execution.processEngineServices.identityService
    def userList = identityService.createUserQuery().userId(userId).list()
    if (userList.size() > 0) {
        if (userList[0].hasProperty("email")) {
            result = userList[0].email
        }
    }
    result

}

getUserEmail(execution, starter)