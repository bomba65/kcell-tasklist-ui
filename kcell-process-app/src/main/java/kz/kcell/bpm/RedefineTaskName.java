package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class RedefineTaskName implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String fillApplied = String.valueOf(delegateTask.getVariable("fillApplied"));
        if("notEnough".equals(fillApplied)){
            if(!delegateTask.getName().contains(" (additional material request)")){
                delegateTask.setName(delegateTask.getName() + " (additional material request)");
            }
            if(!delegateTask.getDescription().contains(" (additional material request)")){
                delegateTask.setDescription(delegateTask.getDescription() + " (additional material request)");
            }
        }
    }
}
