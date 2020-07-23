import org.camunda.bpm.engine.delegate.DelegateExecution

import java.util.stream.Collectors

def getSalesRepresentativeEmails(DelegateExecution execution) {
    def identityService = execution.processEngineServices.identityService
    def list = identityService.createUserQuery().memberOfGroup("delivery_transmission_network_operations").list().stream()
            .map{it.email}
            .filter{it != null && !it.empty}
            .collect(Collectors.toSet())

    def ts = execution.getVariable("technicalSpecifications").unwrap()
    list.add(ts.get('technicalEmail').asText());
    def result = list.stream().collect(Collectors.joining(","))
    result
}

getSalesRepresentativeEmails(execution)
