package kz.kcell.flow.ma;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service("monthlyActSaveToAssets")
public class MonthlyActSaveToAssets implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) {
        
    }
}
