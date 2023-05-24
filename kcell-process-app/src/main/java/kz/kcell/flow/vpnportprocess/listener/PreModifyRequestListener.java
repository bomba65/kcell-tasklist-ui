package kz.kcell.flow.vpnportprocess.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.SneakyThrows;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.stereotype.Component;

@Component
public class PreModifyRequestListener implements ExecutionListener {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public void notify(DelegateExecution execution) {
        String channel = execution.getVariable("channel").toString();
        String requestType = execution.getVariable("request_type").toString();

        if (requestType.equals("Organize") && channel.equals("VPN")) {
            VpnCamVar[] addedServices = objectMapper.readValue(execution.getVariable("addedServices").toString(), VpnCamVar[].class);
            execution.setVariable("preModifiedAddedServices", SpinValues.jsonValue(objectMapper.writeValueAsString(addedServices)).create());
        }
    }
}
