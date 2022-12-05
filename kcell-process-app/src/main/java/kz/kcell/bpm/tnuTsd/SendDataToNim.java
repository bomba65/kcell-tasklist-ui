package kz.kcell.bpm.tnuTsd;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Log
public class SendDataToNim implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        log.info("sending data to NiM");
    }
}
