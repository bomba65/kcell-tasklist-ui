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
        map.put("alm", "Andrey.Ivanov@kcell.kz,Nikolay.Ustinov@kcell.kz,Sheraly.Tolymbekov@kcell.kz");
        map.put("astana", "Alexandr.Galat@kcell.kz,Marat.Abdin@kcell.kz,Nikolay.Ustinov@kcell.kz,Sheraly.Tolymbekov@kcell.kz");
        map.put("nc", "Alexandr.Galat@kcell.kz,Marat.Abdin@kcell.kz,Nikolay.Ustinov@kcell.kz,Sheraly.Tolymbekov@kcell.kz");
        map.put("east", "Nikolay.Ustinov@kcell.kz,Sheraly.Tolymbekov@kcell.kz");
        map.put("south", "Sheraly.Tolymbekov@kcell.kz,Nikolay.Ustinov@kcell.kz");
        map.put("west", "Nikolay.Ustinov@kcell.kz,Sheraly.Tolymbekov@kcell.kz");

        if(map.get(region_name).contains(",")){
            for(String username: map.get(region_name).split(",")){
                delegateTask.addCandidateUser(username);
            }
        } else {
            delegateTask.addCandidateUser(map.get(region_name));
        }
    }
}
