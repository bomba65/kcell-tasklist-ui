import org.camunda.bpm.engine.delegate.DelegateExecution

def getEmails(DelegateExecution execution) {
    def starter = execution.getVariable("starter").toString()
    String region_name = delegateTask.getVariable("region_name").toString();
    Map<String, String> map = new HashMap<>();
    map.put("alm", "Technologies-AR-TNU@kcell.kz");
    map.put("astana", "Tech-Astana-TNU@kcell.kz");
    map.put("nc", "Technologies-NR-TNU@kcell.kz");
    map.put("east", "Tech-East-TNU@kcell.kz");
    map.put("south", "Technologies-SR-TNU@kcell.kz");
    map.put("west", "Tech-West-TNU@kcell.kz");
    String emails  = starter + ",Technologies-N&ITIS-S&FM-OP@kcell.kz," + map.get(region_name)
    emails
}

getEmails(execution)
