package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class MyJavaDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(delegateExecution.getActivityInstanceId());
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
}
