import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors



def getEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    def groups = ["port_ipcore"]

    def userList = []
    for (group in groups) {
        userList.addAll(identityService.createUserQuery().memberOfGroup(group).list())
    }

    def delivery =userList.stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
            .stream().collect(Collectors.joining(","))

    delivery
}

getEmails(execution)
