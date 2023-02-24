package kz.kcell.flow.vpnportprocess;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class VpnPortStub implements JavaDelegate {
        @Override
        public void execute(org.camunda.bpm.engine.delegate.DelegateExecution execution) throws Exception {
            System.out.println("VpnPortStub");
        }
}
