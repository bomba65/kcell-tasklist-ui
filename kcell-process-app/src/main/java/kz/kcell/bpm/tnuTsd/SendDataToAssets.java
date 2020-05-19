package kz.kcell.bpm.tnuTsd;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.BpmnError;

import java.util.Random;

@Log
public class SendDataToAssets implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("sending data to Assets");

        String[] list = {"400", "401", "403", "404", "500"};
        Random r = new Random();

        String errorMessage = list[r.nextInt(list.length)];

        throw new BpmnError("error", errorMessage);
    }
}
