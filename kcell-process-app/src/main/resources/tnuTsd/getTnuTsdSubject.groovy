package tnuTsd

import org.camunda.bpm.engine.delegate.DelegateExecution

def getSubject(DelegateExecution execution) {
    def businessKey = execution.businessKey;
    String region_name = execution.getVariable("region_name").toString();
    Map<String, String> map = new HashMap<>();
    map.put("alm", "Almaty");
    map.put("astana", "Astana");
    map.put("nc", "N&C");
    map.put("east", "East");
    map.put("south", "South");
    map.put("west", "West");
    subject = "New TSD from " + map.get(region_name) + " region " + String.format("- %s", businessKey);
    subject
}

getSubject(execution)
