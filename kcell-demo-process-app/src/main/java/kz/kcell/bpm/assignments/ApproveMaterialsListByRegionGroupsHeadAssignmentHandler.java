package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by admin on 3/27/17.
 */
public class ApproveMaterialsListByRegionGroupsHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateUsers(Arrays.asList("Kerey.Zatilda@kcell.kz", "Kanat.Kulmukhambetov@kcell.kz"));
            } else if (reason.equals("2")) {
                delegateTask.addCandidateUsers(Arrays.asList("Nurzhan.Mynbayev@kcell.kz", "Asset.Rashitov@kcell.kz", "Vladimir.Yefanov@kcell.kz", "Aslan.Shalov@kcell.kz", "Shyngys.Kassabekov@kcell.kz"));
            } else if (reason.equals("3")) {
                delegateTask.addCandidateUsers(Arrays.asList("Alexey.Khudaev@kcell.kz", "Evgeniy.Semenov@kcell.kz", "Sergey.Chekh@kcell.kz", "Sergey.Lee@kcell.kz", "Vladimir.Yefanov@kcell.kz", "Yevgeniy.Elunin@kcell.kz", "Yermek.Tanabekov@kcell.kz", "Andrei.Lugovoy@kcell.kz", "Alexey.Kolesnikov@kcell.kz"));
            } else if (reason.equals("4")) {
                delegateTask.addCandidateUsers(Arrays.asList("Askar.Bekmurzayev@kcell.kz", "Temirlan.Kaliyev@kcell.kz"));
            }
        }
    }
}
