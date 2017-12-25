package kz.kcell.flow.sap;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.script.*;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

@Service("createJOFileAndSave")
@Log
public class CreateJOFileAndSave implements JavaDelegate {

    private final CompiledScript template;
    private Minio minioClient;

    @Autowired
    public CreateJOFileAndSave(ScriptEngineManager manager, Minio minioClient) throws ScriptException {

        ScriptEngine groovy = manager.getEngineByName("groovy");
        InputStreamReader reader = new InputStreamReader(TaskListener.class.getResourceAsStream("/sap/JoJr.groovy"));
        this.template = ((Compilable)groovy).compile(reader);
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        String path = delegateExecution.getProcessInstanceId() + "/" + delegateExecution.getVariable("jrNumber") + "_JoJr.txt";
        minioClient.saveFile(path, is, "text/plain");

        String json = "{\"name\" : \"" + delegateExecution.getVariable("jrNumber") + "_JoJr.txt" + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("JoJrFile", SpinValues.jsonValue(json));
    }
}
