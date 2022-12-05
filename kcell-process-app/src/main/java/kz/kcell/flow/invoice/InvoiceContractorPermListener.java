package kz.kcell.flow.invoice;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.identity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;

@Log
@Service("invoiceContractorPermListener")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InvoiceContractorPermListener implements ExecutionListener {

    @Autowired
    IdentityService identityService;

    @Autowired
    AuthorizationService authorizationService;

    @Override
    public void notify(DelegateExecution delegateExecution) {

        String starter = String.valueOf(delegateExecution.getVariable("starter"));
        List<Group> contractorGroups = identityService.createGroupQuery().groupNameLike("hq_contractor_%").groupMember(starter).list();

        if(contractorGroups.size() > 0){
            String groupName = contractorGroups.get(0).getName();

            Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            authorization.setResourceType(8); //ProcessInstance
            authorization.setResourceId(delegateExecution.getProcessInstanceId());
            authorization.addPermission(Permissions.READ);
            authorization.setGroupId("contractor_users_" + groupName.replace("hq_contractor_",""));

            authorizationService.saveAuthorization(authorization);
        } else {
            String siteRegion = delegateExecution.getVariable("siteRegion").toString();
            List<Group> kcellGroups = identityService.createGroupQuery().groupId(siteRegion + "_engineer").groupMember(starter).list();
            if(kcellGroups.size() > 0){
                for(Group kcellGroup: kcellGroups){
                    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                    authorization.setResourceType(8); //ProcessInstance
                    authorization.setResourceId(delegateExecution.getProcessInstanceId());
                    authorization.addPermission(Permissions.READ);
                    authorization.setGroupId(kcellGroup.getId());
                    authorizationService.saveAuthorization(authorization);
                }
            } else {
                log.warning("No contractor group found for user: " + starter + " in invoice process id " + delegateExecution.getProcessInstanceId());
            }
        }
    }
}
