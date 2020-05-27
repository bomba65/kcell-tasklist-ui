package tnuTsd

import org.camunda.bpm.engine.delegate.DelegateExecution

def getEmails(DelegateExecution execution) {
    def starter = execution.getVariable("starter").toString()

    def identityService = execution.processEngineServices.identityService
    def userList = identityService.createUserQuery().userId(starter).list();
    def starterEmail = null;
    if(userList.size() > 0){
        starterEmail = userList.get(0).email;
    }

    String region_name = execution.getVariable("region_name").toString();
    Map<String, String> map = new HashMap<>();
    map.put("alm", "Technologies-AR-TNU@kcell.kz");
    map.put("astana", "Tech-Astana-TNU@kcell.kz");
    map.put("nc", "Technologies-NR-TNU@kcell.kz");
    map.put("east", "Tech-East-TNU@kcell.kz");
    map.put("south", "Technologies-SR-TNU@kcell.kz");
    map.put("west", "Tech-West-TNU@kcell.kz");
    String emails  = (starterEmail!=null?starterEmail + ",":"") + "Technologies-N&ITIS-S&FM-OP@kcell.kz," + map.get(region_name)
    emails
}

getEmails(execution)
