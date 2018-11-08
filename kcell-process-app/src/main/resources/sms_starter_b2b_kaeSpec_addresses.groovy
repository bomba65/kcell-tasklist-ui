import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def getStarterEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService

    identityService.createUserQuery().userId(starter).list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getB2BEmails(DelegateExecution execution){
    def identityService = execution.processEngineServices.identityService
    identityService.createUserQuery().memberOfGroup("sms_b2b").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())
}

def getKAESpecEmails(DelegateExecution execution){
    Set<String> set = new HashSet<String>();
    set.add(execution.getVariableTyped("kaeSpecialistEmail"));
    set
}

def getStarterB2BAddresses(DelegateExecution execution) {
    def starterEmails = getStarterEmails(execution)//["asdf@gasdf.coasdf"]
    def btbEmails = getB2BEmails(execution)
    def kaeEmail = getKAESpecEmails(execution)
    starterEmails.addAll(kaeEmail.value)
    btbEmails.addAll(starterEmails)
    def result = btbEmails.stream().collect(Collectors.joining(","))
    println 'get users:'
    println result
    result
}

getStarterB2BAddresses(execution)
//getStarterB2BAddresses(null)

