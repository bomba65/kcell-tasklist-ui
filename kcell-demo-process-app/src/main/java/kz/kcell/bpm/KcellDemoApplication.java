package kz.kcell.bpm;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;

@ProcessApplication("Kcell Demo App")
public class KcellDemoApplication extends ServletProcessApplication {

    @PostDeploy
    public void startFirstProcess(ProcessEngine processEngine) {
        this.createUsers(processEngine);
    }

    private void createUsers(ProcessEngine processEngine) {
        (new DemoDataGenerator()).createUsers(processEngine);
    }
}
