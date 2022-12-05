package revision

import org.camunda.bpm.engine.delegate.DelegateExecution

def getUserEmail(DelegateExecution execution, String userId) {
    def identityService = execution.processEngineServices.identityService
    def userList = identityService.createUserQuery().userId(userId).list()
    def result = "Begaly.Kokin@kcell.kz, Zhanar.Zhubantayeva@kcell.kz, Aidana.Abdrakhman@kcell.kz, Lyudmila.Vilkova@kcell.kz, "
    if (userList.size() > 0) {
        if (userList[0].hasProperty("email")) {
            result = result + userList[0].email
        }
    }
    result

}

getUserEmail(execution, starter)
