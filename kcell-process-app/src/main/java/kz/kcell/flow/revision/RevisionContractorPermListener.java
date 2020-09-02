package kz.kcell.flow.revision;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@Service("revisionContractorPermListener")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RevisionContractorPermListener implements ExecutionListener {

    @Autowired
    IdentityService identityService;

    @Autowired
    AuthorizationService authorizationService;

    private final static Map<String, String> contractors;
    static
    {
        contractors = new HashMap<String, String>();
        contractors.put("4", "lse");
        contractors.put("6", "alta");
        contractors.put("7", "logycom");
        contractors.put("8", "arlan");
    }

    @Override
    public void notify(DelegateExecution delegateExecution) {

        String contractor = String.valueOf(delegateExecution.getVariable("contractor"));

        if(contractors.containsKey(contractor)){
            int authorizationSize = authorizationService
                .createAuthorizationQuery()
                .resourceType(8)
                .resourceId(delegateExecution.getProcessInstanceId())
                .hasPermission(Permissions.READ)
                .groupIdIn("contractor_users_" + contractors.get(contractor))
                .list()
                .size();

            if(authorizationSize == 0){
                Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                authorization.setResourceType(8); //ProcessInstance
                authorization.setResourceId(delegateExecution.getProcessInstanceId());
                authorization.addPermission(Permissions.READ);
                authorization.setGroupId("contractor_users_" + contractors.get(contractor));

                authorizationService.saveAuthorization(authorization);
            }
        } else {
            if(!"5".equals(contractor)){
                log.warning("No value found for contractor: " + contractor + " in contractors map in process id " + delegateExecution.getProcessInstanceId());
            }
        }
    }
}
