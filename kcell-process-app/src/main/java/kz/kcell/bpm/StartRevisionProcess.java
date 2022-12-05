package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.spin.plugin.variable.SpinValues;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StartRevisionProcess implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        execution.setVariable("isNewProcessCreated", "true");
        execution.setVariable("prCreationInProgress", "false");
    }
}
