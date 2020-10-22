package tnuTsd

import org.camunda.bpm.engine.delegate.DelegateExecution

import java.util.stream.Collectors

def getEmails(DelegateExecution execution) {
    def starter = execution.getVariable("starter").toString()

    def identityService = execution.processEngineServices.identityService
    def userList = identityService.createUserQuery().userId(starter).list();
    def starterEmail = null;
    if(userList.size() > 0){
        starterEmail = userList.get(0).email;
    }
    String region_name = execution.getVariable("region_name").toString();

    def groupName = region_name + "_tn_engineer";
    def groupUserList = identityService.createUserQuery().memberOfGroup(groupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def result = groupUserList.stream().collect(Collectors.joining(","))

    def hqGroupName = "hq_permission";
    def hqGroupNameUserList = identityService.createUserQuery().memberOfGroup(hqGroupName).list().stream()
            .map{it.getEmail()}
            .filter{it != null && !it.isEmpty()}
            .collect(Collectors.toSet())
    def hqResult = hqGroupNameUserList.stream().collect(Collectors.joining(","))


    String emails  = (starterEmail!=null?starterEmail + ",":"") + "Technologies-N&ITIS-S&FM-OP@kcell.kz" + result + "," + hqResult
    println 'get users emails:'
    println emails
    emails
}

getEmails(execution)
