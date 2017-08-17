package kz.kcell.bpm;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.junit.Rule;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class FreemarketTest {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();


    //@Test
    public void testActOfAcceptance() throws IOException {
        BpmnModelInstance done = Bpmn.createExecutableProcess("test-freemarker")
                .startEvent()
                .serviceTask()
                .camundaClass("kz.kcell.bpm.GenerateActOfAcceptance")
                .endEvent()
                .done();
        processEngineRule.getRepositoryService().createDeployment().addModelInstance("test-freemarket.bpmn", done).deploy();

        Map<String, Object> variables = new HashMap<>();

        JsonValue jsonValue = SpinValues.jsonValue("{\"shared_bs_id\":1,\"status\":1,\"accepted_date\":42935,\"city\":\"Uralsk\",\"oblast\":\"Западно-Казахстанская\",\"address\":\"Uralsk city, Dostyk-Druzhba ave., 148/1 Общежитие № 2, КСК\",\"host\":\"Kcell\",\"site_owner\":\"Kcell\",\"site_name\":\"81007URKSKUSTAZ\",\"site_id\":81007,\"cluster_name\":\"Uralsk_CL01\",\"current_rbs_type\":\"RBS8800\",\"current_vendor\":\"ZTE\",\"new_rbs_type\":\"\",\"rbs_location\":\"Indoor\",\"existing_bands_with_section_number\":\"3*2100+3*900+2*1800\",\"existing_bands_with_section_number_rounded\":\"3*2100+3*900+3*1800\",\"po_configuration\":\"SWAP RBS6601 3x4 WCDMA + 3x4 GSM1800 + 3x4 GSM900/Extension of Existing Site with L8+L18 Indoor\",\"in_plan\":\"yes\",\"in_plan_date\":42694,\"technology2g\":\"\",\"technology3g\":\"\",\"lte_band\":\"SWAP+LTE800\",\"band900\":742223,\"band1800\":\"\",\"band2100\":80010865,\"band800\":80010865,\"swap_antennas_po\":80010865,\"total_qty_of_antennas_per_sector\":2,\"jumper\":\"4.3-10(m)-7-16(m)\",\"transport_type\":\"FIber\",\"possibility_to_provide_100_mbps_in_2017\":\"yes\",\"possibility_to_increase_transmission\":\"yes\",\"ran_po_name\":\"1)PO 68025_348LTE; 2)PO 68790_ZTE SWAP 1\",\"po_item_number\":\"1)2.1.1 - 2.1.3; 2.1.5 - 2.1.6; 2)3.7 - 3.8; 4.1\",\"dispatched_hw_for_sites\":\"\",\"site_usage_type\":\"совместного\",\"infrastructure_owner\":\"Kcell\",\"transmission_owner\":\"Kcell\",\"transmission_channel_volume_granted_to_partner\":\"\",\"integration_city\":\"Уральск\",\"isSpecialSite\":\"true\",\"shared_sectors\":[{\"sector_name\":\"581007-100\",\"lac\":46,\"enodeb_id\":581007,\"shared_bs_id\":1,\"beeline_position_number\":\"\",\"kcell_position_number\":\"\",\"position_type\":\"покрытие\",\"location_type\":\"\",\"placement_type\":\"\",\"longitude\":61.3765,\"latitude\":61.19119,\"antenna_type\":80010865,\"height\":33,\"azimuth\":40,\"tilt\":\"2/6\",\"power_watt\":40,\"ems_limitations_info\":\"нет\",\"enodeb_range\":800,\"beeline_band\":10,\"kcell_band\":10,\"status\":\"\"},{\"sector_name\":\"581007-102\",\"lac\":46,\"enodeb_id\":581007,\"shared_bs_id\":1,\"beeline_position_number\":\"\",\"kcell_position_number\":\"\",\"position_type\":\"покрытие\",\"location_type\":\"\",\"placement_type\":\"\",\"longitude\":61.3765,\"latitude\":61.19119,\"antenna_type\":80010865,\"height\":33,\"azimuth\":160,\"tilt\":\"2/8\",\"power_watt\":40,\"ems_limitations_info\":\"нет\",\"enodeb_range\":800,\"beeline_band\":10,\"kcell_band\":10,\"status\":\"\"},{\"sector_name\":\"581007-104\",\"lac\":46,\"enodeb_id\":581007,\"shared_bs_id\":1,\"beeline_position_number\":\"\",\"kcell_position_number\":\"\",\"position_type\":\"покрытие\",\"location_type\":\"\",\"placement_type\":\"\",\"longitude\":61.3765,\"latitude\":61.19119,\"antenna_type\":80010865,\"height\":33,\"azimuth\":290,\"tilt\":\"2/6\",\"power_watt\":40,\"ems_limitations_info\":\"нет\",\"enodeb_range\":800,\"beeline_band\":10,\"kcell_band\":10,\"status\":\"\"}]}").create();

        variables.put("sharingPlan", jsonValue);

        ProcessInstance processInstance = processEngineRule.getRuntimeService().startProcessInstanceByKey("test-freemarker", variables);
        Object obj = processEngineRule.getHistoryService().createHistoricVariableInstanceQuery().variableName("actOfAcceptanceDocument").singleResult().getValue();

        Files.copy((InputStream) obj, Paths.get("nurlan.doc"), StandardCopyOption.REPLACE_EXISTING);
    }

    //@Test
    public void testCreatePR() throws ScriptException, IOException {
//        Object freemarker = processEngineRule.getProcessEngineConfiguration().getScriptingEngines().getScriptEngineForLanguage("freemarker").eval("/testCreatePR.ftl");
//        System.out.println(freemarker);

        BpmnModelInstance done = Bpmn.createExecutableProcess("test-freemarker")
                .startEvent()
                .scriptTask()
                    .scriptFormat("freemarker")
                    .camundaResource("testCreatePR.ftl")
                    .camundaResultVariable("result")
                .endEvent()
                .done();
        processEngineRule.getRepositoryService().createDeployment().addModelInstance("test-freemarker.bpmn", done).deploy();

        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> historyMap = new HashMap<>();
        Map<String, String> tasksMap = new HashMap<String, String>();
        tasksMap.put("83a118ac-8329-11e7-8181-da6044b09f14","ACCEPT");
        historyMap.put("tasksMap", tasksMap);
        variables.put("history", historyMap);

        ProcessInstance processInstance = processEngineRule.getRuntimeService().startProcessInstanceByKey("test-freemarker", variables);
        Object obj = processEngineRule.getHistoryService().createHistoricVariableInstanceQuery().variableName("result").singleResult().getValue();
        System.out.println(obj);
    }
}
