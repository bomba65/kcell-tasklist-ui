package kz.kcell.flow.fixedInternet.ATLAS;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


@Service("CreateSubscriptionReserve")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CreateSubscriptionReserve implements JavaDelegate {

    private CreateSubscriptionReserveRecurring createSubscriptionReserveRecurring;
    private CreateSubscriptionReserveOnetime createSubscriptionReserveOnetime;

    @Autowired
    public void setCreateSubscriptionReserveRecurring(CreateSubscriptionReserveRecurring createSubscriptionReserveRecurring) {
        this.createSubscriptionReserveRecurring = createSubscriptionReserveRecurring;
    }

    @Autowired
    public void setCreateSubscriptionReserveOnetime(CreateSubscriptionReserveOnetime createSubscriptionReserveOnetime) {
        this.createSubscriptionReserveOnetime = createSubscriptionReserveOnetime;
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        createSubscriptionReserveOnetime.execute(execution);
        createSubscriptionReserveRecurring.execute(execution);
    }
}
