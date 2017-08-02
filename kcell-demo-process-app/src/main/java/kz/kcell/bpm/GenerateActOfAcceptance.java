package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;

public class GenerateActOfAcceptance implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        ExecutableScript actOfAcceptanceScript = Context.getProcessEngineConfiguration().getScriptFactory().createScriptFromResource("freemarker", "actOfAcceptance.ftl");
        String actOfAcceptanceHtml = Context.getProcessEngineConfiguration().getScriptingEnvironment().execute(actOfAcceptanceScript, execution).toString();

        FileValue actOfAcceptanceDocument = Variables.fileValue("actOfAcceptance.doc").file(actOfAcceptanceHtml.getBytes("utf-8")).mimeType("application/msword").create();
        execution.setVariable("actOfAcceptanceDocument", actOfAcceptanceDocument);

    }
}
