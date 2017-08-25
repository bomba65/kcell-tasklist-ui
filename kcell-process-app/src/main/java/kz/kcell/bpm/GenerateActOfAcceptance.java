package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;

import java.io.UnsupportedEncodingException;

public class GenerateActOfAcceptance implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        ExecutableScript actOfAcceptanceScript = Context.getProcessEngineConfiguration().getScriptFactory().createScriptFromResource("freemarker", "actOfAcceptance.ftl");
        String actOfAcceptanceHtml = Context.getProcessEngineConfiguration().getScriptingEnvironment().execute(actOfAcceptanceScript, delegateTask).toString();

        FileValue actOfAcceptanceDocument = null;
        try {
            actOfAcceptanceDocument = Variables.fileValue("actOfAcceptance.doc").file(actOfAcceptanceHtml.getBytes("utf-8")).mimeType("application/msword").create();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        delegateTask.setVariable("actOfAcceptanceDocument", actOfAcceptanceDocument);
    }
}
