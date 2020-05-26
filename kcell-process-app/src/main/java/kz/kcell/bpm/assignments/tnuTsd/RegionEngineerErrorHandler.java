package kz.kcell.bpm.assignments.tnuTsd;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.HashMap;
import java.util.Map;

public class RegionEngineerErrorHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String region_name = delegateTask.getVariable("region_name").toString();
        Map<String, String> map = new HashMap<>();
        map.put("alm", "Andrey.Ivanov@kcell.kz");
        map.put("astana", "Alexandr.Galat@kcell.kz, Marat.Abdin@kcell.kz");
        map.put("nc", "Alexandr.Galat@kcell.kz, Marat.Abdin@kcell.kz");
        map.put("east", "Nikolay.Ustinov@kcell.kz");
        map.put("south", "Samat.Dautov@kcell.kz");
        map.put("west", "Nikolay.Ustinov@kcell.kz");

        delegateTask.addCandidateUser(map.get(region_name));


    }
}
