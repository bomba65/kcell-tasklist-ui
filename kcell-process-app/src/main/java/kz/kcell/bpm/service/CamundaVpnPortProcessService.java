package kz.kcell.bpm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.springframework.stereotype.Service;
import spinjar.com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CamundaVpnPortProcessService {
    private final HistoryService historyService;
    private final RepositoryService repositoryService;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private final static String VPN_PORT_PROCESS = "VPN_Port_process";
    private final static String VPN_PORT_AUTO_PROCESS = "VPN_Port_auto_process";
    
    public List<Map<String, String>> getVpnPortProcess(
        String process,
        String channel,
        String requestType,
        String currentActivity,
        String priority,
        String dateCreated,
        String requestNumber,
        String portId,
        String vpnId
    ) throws ParseException {
        HistoricProcessInstanceQuery vpnPortProcessHistoryQuery = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(VPN_PORT_PROCESS);
        HistoricProcessInstanceQuery vpnPortAutoProcessHistoryQuery = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(VPN_PORT_AUTO_PROCESS);

        if (channel != null) {
            vpnPortProcessHistoryQuery.variableValueEquals("channel", channel);
            vpnPortAutoProcessHistoryQuery.variableValueEquals("channel", channel);
        }

        if (requestType != null) {
            vpnPortProcessHistoryQuery.variableValueEquals("request_type", requestType);
            vpnPortAutoProcessHistoryQuery.variableValueEquals("request_type", requestType);
        }

        if (priority != null) {
            vpnPortProcessHistoryQuery.variableValueEquals("priority", priority);
            vpnPortAutoProcessHistoryQuery.variableValueEquals("priority", priority);
        }

        if (requestNumber != null) {
            vpnPortProcessHistoryQuery.processInstanceBusinessKey(requestNumber);
            vpnPortAutoProcessHistoryQuery.processInstanceBusinessKey(requestNumber);
        }

        if (dateCreated != null) {
            String[] stringDates = dateCreated.split(" - ");
            Date beginDate = new SimpleDateFormat("dd.MM.yy").parse(stringDates[0]);
            Date endDate = new Date(new SimpleDateFormat("dd.MM.yy").parse(stringDates[1]).getTime() + 24 * 60 * 60 * 1000);
            vpnPortProcessHistoryQuery.startedAfter(beginDate);
            vpnPortProcessHistoryQuery.startedBefore(endDate);
            vpnPortAutoProcessHistoryQuery.startedAfter(beginDate);
            vpnPortAutoProcessHistoryQuery.startedBefore(endDate);
        }

        if (currentActivity != null) {
            vpnPortProcessHistoryQuery.activeActivityIdIn(currentActivity);
            vpnPortAutoProcessHistoryQuery.activeActivityIdIn(currentActivity);
        }

        List<HistoricProcessInstance> historicProcessInstances = new ArrayList<>();
        if (process != null && process.equals(VPN_PORT_PROCESS)) {
            historicProcessInstances.addAll(vpnPortProcessHistoryQuery.list());
        } else if (process != null && process.equals(VPN_PORT_AUTO_PROCESS)) {
            historicProcessInstances.addAll(vpnPortAutoProcessHistoryQuery.list());
        } else {
            historicProcessInstances.addAll(vpnPortProcessHistoryQuery.list());
            historicProcessInstances.addAll(vpnPortAutoProcessHistoryQuery.list());
        }

        if (portId != null) {
            historicProcessInstances = historicProcessInstances.stream()
                .filter(e -> getPortNumbers(e).contains(portId)).collect(Collectors.toList());
        }

        if (vpnId != null) {
            historicProcessInstances = historicProcessInstances.stream()
                .filter(e -> getVpnNumbers(e).contains(vpnId)).collect(Collectors.toList());
        }

        return historicProcessInstances.stream().map(processInstance -> {
            Map<String, String> map = getVariables(processInstance);
            map.put("processInstance", writeValueAsString(processInstance));
            map.put("portId", writeValueAsString(getPortNumbers(processInstance)));
            map.put("vpnId", writeValueAsString(getVpnNumbers(processInstance)));
            map.put("taskActivities", writeValueAsString(historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).list()));
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, String> getTaskList() {
        String vpnPortProcessDefinitionId = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(VPN_PORT_PROCESS)
            .orderByProcessInstanceStartTime().desc().list().stream()
            .map(HistoricProcessInstance::getProcessDefinitionId).findFirst().orElse(null);

        String vpnPortAutoProcessDefinitionId = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(VPN_PORT_AUTO_PROCESS)
            .orderByProcessInstanceStartTime().desc().list().stream()
            .map(HistoricProcessInstance::getProcessDefinitionId).findFirst().orElse(null);

        Map<String, String> taskNames = new HashMap<>();
        if (vpnPortProcessDefinitionId != null) {
            taskNames.putAll(((ProcessDefinitionEntity) repositoryService.getProcessDefinition(vpnPortProcessDefinitionId))
                .getTaskDefinitions().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getNameExpression().getExpressionText()
                )));
        } else if (vpnPortAutoProcessDefinitionId != null) {
            taskNames.putAll(((ProcessDefinitionEntity) repositoryService.getProcessDefinition(vpnPortAutoProcessDefinitionId))
                .getTaskDefinitions().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getNameExpression().getExpressionText()
                )));
        }

        return taskNames;
    }

    private List<String> getVpnNumbers(HistoricProcessInstance processInstance) {
        List<HistoricVariableInstance> vpnVars = historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.getId())
            .variableNameLike("%Services").list();
        return vpnVars.stream()
            .filter(o -> !o.getName().equals("preModifiedAddedServices"))
            .map(vpnVar -> ((JacksonJsonNode) vpnVar.getValue())).flatMap(node -> node.elements().stream())
            .map(e -> ((JacksonJsonNode) e).unwrap().findValue("vpn_number"))
            .filter(Objects::nonNull).map(JsonNode::toString).map(s -> s.replace("\"", "")).collect(Collectors.toList());
    }

    private Set<String> getPortNumbers(HistoricProcessInstance processInstance) {
        List<HistoricVariableInstance> vpnVars = historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.getId())
            .variableNameLike("%Services").list();
        Set<String> set = vpnVars.stream()
            .filter(o -> !o.getName().equals("preModifiedAddedServices"))
            .map(vpnVar -> ((JacksonJsonNode) vpnVar.getValue())).flatMap(node -> node.elements().stream())
            .map(e -> ((JacksonJsonNode) e).unwrap().findValue("port_id") != null ?
                ((JacksonJsonNode) e).unwrap().findValue("port_id") : ((JacksonJsonNode) e).unwrap().findValue("port_number"))
            .filter(Objects::nonNull).map(JsonNode::toString).map(s -> s.replace("\"", "")).collect(Collectors.toSet());

        List<HistoricVariableInstance> portVars = historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.getId())
            .variableNameLike("%Ports").list();
        set.addAll(portVars.stream()
            .map(portVar -> ((JacksonJsonNode) portVar.getValue())).flatMap(node -> node.elements().stream())
            .map(e -> ((JacksonJsonNode) e).unwrap().findValue("port_id") != null ?
                ((JacksonJsonNode) e).unwrap().findValue("port_id") : ((JacksonJsonNode) e).unwrap().findValue("port_number"))
            .filter(Objects::nonNull).map(JsonNode::toString).map(s -> s.replace("\"", "")).collect(Collectors.toList()));

        return set;
    }

    private <T> String writeValueAsString(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<String, String> getVariables(HistoricProcessInstance processInstance) {
        return historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.getId())
            .list().stream().collect(Collectors.toMap(
                HistoricVariableInstance::getName,
                e -> e.getValue().toString(),
                (oldValue, newValue) -> newValue
            ));
    }
}
