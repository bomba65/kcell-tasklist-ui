package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegionGroupAssignmentHandler implements TaskListener {

    public List<String> findRegionGroupHeadAssignee(String reason) {
        if (reason != null) {
            if (reason.equals("1")) {
                return Arrays.asList("Kerey.Zatilda@kcell.kz");
            } else if (reason.equals("2")) {
                return Arrays.asList("Maulen.Kempirbayev@kcell.kz");
            } else if (reason.equals("3")) {
                return Arrays.asList("Kali.Esimbekov@kcell.kz", "Maulen.Kempirbayev@kcell.kz", "Samat.Akhmetov@kcell.kz", "Zhanat.Seitkanov@kcell.kz");
            } else if (reason.equals("4")) {
                return new ArrayList<String>();
            } else {
                return new ArrayList<String>();
            }
        } else {
            return new ArrayList<String>();
        }
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateUsers(Arrays.asList("Alexey.Kolyagin@kcell.kz", "Anastassiya.Shenojak@kcell.kz", "Dmitriy.Saidashev@kcell.kz", "Lazizbek.Kurbantayev@kcell.kz", "Nurzhan.Kochshigulov@kcell.kz", "Sergey.Michshenko@kcell.kz", "Kanat.Kulmukhambetov@kcell.kz", "Kerey.Zatilda@kcell.kz"));
            } else if (reason.equals("2")) {
                delegateTask.addCandidateUsers(Arrays.asList("Beibit.Bitenov@kcell.kz", "Nurzhan.Mynbayev@kcell.kz", "Asset.Rashitov@kcell.kz", "Vladimir.Yefanov@kcell.kz", "Aslan.Shalov@kcell.kz"));
            } else if (reason.equals("3")) {
                delegateTask.addCandidateUsers(Arrays.asList("Alexey.Khudaev@kcell.kz", "Evgeniy.Semenovkcell.kz", "Sergey.Chekh@kcell.kz", "Sergey.Lee@kcell.kz", "Vladimir.Yefanov@kcell.kz", "Yevgeniy.Elunin@kcell.kz", "Yermek.Tanabekov@kcell.kz", "Andrei.Lugovoy@kcell.kz", "Alexey.Kolesnikov@kcell.kz"));
            } else if (reason.equals("4")) {
                delegateTask.addCandidateUsers(new ArrayList<String>());
            }
        }
    }
}
