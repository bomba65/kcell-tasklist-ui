package kz.kcell.bpm.ma;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class MaRemoveFromMainList implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        SpinJsonNode selectedRevisions = delegateTask.<JsonValue>getVariableTyped("selectedRevisions").getValue();
        List<String> selectedList = new ArrayList<>();
        List<String> reasonList = new ArrayList<>();

        if("ma_rollout_modify".equals(delegateTask.getTaskDefinitionKey())){
            for(String revisionId: selectedRevisions.fieldNames()){
                if(selectedRevisions.prop(revisionId).prop("reason").equals("5")){
                    selectedList.add(revisionId);
                }
            }

            SpinJsonNode rolloutRegularWorks = delegateTask.<JsonValue>getVariableTyped("rolloutRegularWorks").getValue();
            SpinJsonNode rolloutEmergencyWorks = delegateTask.<JsonValue>getVariableTyped("rolloutEmergencyWorks").getValue();

            rolloutRegularWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });
            rolloutEmergencyWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });

            for(String selected: selectedList){
                if(!reasonList.contains(selected)){
                    selectedRevisions.remove(selected);
                }
            }
        } else if("ma_po_modify".equals(delegateTask.getTaskDefinitionKey())){
            for(String revisionId: selectedRevisions.fieldNames()){
                if(selectedRevisions.prop(revisionId).prop("reason").equals("1")){
                    selectedList.add(revisionId);
                }
            }

            SpinJsonNode poRegularWorks = delegateTask.<JsonValue>getVariableTyped("poRegularWorks").getValue();
            SpinJsonNode poEmergencyWorks = delegateTask.<JsonValue>getVariableTyped("poEmergencyWorks").getValue();

            poRegularWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });
            poEmergencyWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });

            for(String selected: selectedList){
                if(!reasonList.contains(selected)){
                    selectedRevisions.remove(selected);
                }
            }
        } else if("ma_tnu1_modify".equals(delegateTask.getTaskDefinitionKey())){
            for(String revisionId: selectedRevisions.fieldNames()){
                if(selectedRevisions.prop(revisionId).prop("reason").equals("2") && (selectedRevisions.prop(revisionId).prop("siteRegion").equals("nc") || selectedRevisions.prop(revisionId).prop("siteRegion").equals("astana"))){
                    selectedList.add(revisionId);
                }
            }

            SpinJsonNode tnuRegularWorks = delegateTask.<JsonValue>getVariableTyped("tnuRegularWorks").getValue();
            SpinJsonNode tnuEmergencyWorks = delegateTask.<JsonValue>getVariableTyped("tnuEmergencyWorks").getValue();

            tnuRegularWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });
            tnuEmergencyWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });

            for(String selected: selectedList){
                if(!reasonList.contains(selected)){
                    selectedRevisions.remove(selected);
                }
            }
        } else if("ma_tnu2_modify".equals(delegateTask.getTaskDefinitionKey())){
            for(String revisionId: selectedRevisions.fieldNames()){
                if(selectedRevisions.prop(revisionId).prop("reason").equals("2") && (selectedRevisions.prop(revisionId).prop("siteRegion").equals("east") || selectedRevisions.prop(revisionId).prop("siteRegion").equals("south"))){
                    selectedList.add(revisionId);
                }
            }

            SpinJsonNode tnuRegularWorks = delegateTask.<JsonValue>getVariableTyped("tnuRegularWorks").getValue();
            SpinJsonNode tnuEmergencyWorks = delegateTask.<JsonValue>getVariableTyped("tnuEmergencyWorks").getValue();

            tnuRegularWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });
            tnuEmergencyWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });

            for(String selected: selectedList){
                if(!reasonList.contains(selected)){
                    selectedRevisions.remove(selected);
                }
            }
        } else if("ma_tnu3_modify".equals(delegateTask.getTaskDefinitionKey())){
            for(String revisionId: selectedRevisions.fieldNames()){
                if(selectedRevisions.prop(revisionId).prop("reason").equals("2") && (selectedRevisions.prop(revisionId).prop("siteRegion").equals("alm") || selectedRevisions.prop(revisionId).prop("siteRegion").equals("west"))){
                    selectedList.add(revisionId);
                }
            }

            SpinJsonNode tnuRegularWorks = delegateTask.<JsonValue>getVariableTyped("tnuRegularWorks").getValue();
            SpinJsonNode tnuEmergencyWorks = delegateTask.<JsonValue>getVariableTyped("tnuEmergencyWorks").getValue();

            tnuRegularWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });
            tnuEmergencyWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });

            for(String selected: selectedList){
                if(!reasonList.contains(selected)){
                    selectedRevisions.remove(selected);
                }
            }
        } else if("ma_sfm_modify".equals(delegateTask.getTaskDefinitionKey())){
            for(String revisionId: selectedRevisions.fieldNames()){
                if(selectedRevisions.prop(revisionId).prop("reason").equals("3")){
                    selectedList.add(revisionId);
                }
            }

            SpinJsonNode sfmRegularWorks = delegateTask.<JsonValue>getVariableTyped("sfmRegularWorks").getValue();
            SpinJsonNode sfmEmergencyWorks = delegateTask.<JsonValue>getVariableTyped("sfmEmergencyWorks").getValue();

            sfmRegularWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });
            sfmEmergencyWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });

            for(String selected: selectedList){
                if(!reasonList.contains(selected)){
                    selectedRevisions.remove(selected);
                }
            }
        } else if("ma_sao_modify".equals(delegateTask.getTaskDefinitionKey())){
            for(String revisionId: selectedRevisions.fieldNames()){
                if(selectedRevisions.prop(revisionId).prop("reason").equals("4")){
                    selectedList.add(revisionId);
                }
            }

            SpinJsonNode saoRegularWorks = delegateTask.<JsonValue>getVariableTyped("saoRegularWorks").getValue();
            SpinJsonNode saoEmergencyWorks = delegateTask.<JsonValue>getVariableTyped("saoEmergencyWorks").getValue();

            saoRegularWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });
            saoEmergencyWorks.elements().forEach(work -> {
                reasonList.add(work.prop("processInstanceId").stringValue());
            });

            for(String selected: selectedList){
                if(!reasonList.contains(selected)){
                    selectedRevisions.remove(selected);
                }
            }
        }

        delegateTask.setVariable("selectedRevisions", SpinJsonNode.JSON(selectedRevisions.toString()));
    }
}
