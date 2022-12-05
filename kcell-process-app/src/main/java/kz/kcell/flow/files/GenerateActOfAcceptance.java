package kz.kcell.flow.files;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.InputStreamReader;

import java.io.UnsupportedEncodingException;

//public class GenerateActOfAcceptance implements TaskListener {
//
//    @Override
//    public void notify(DelegateTask delegateTask) {
//        delegateTask.setVariableLocal("taskSubmitDate", new Date());
//        ExecutableScript actOfAcceptanceScript = Context.getProcessEngineConfiguration().getScriptFactory().createScriptFromResource("groovy", "actOfAcceptance.groovy");
//        String actOfAcceptanceHtml = Context.getProcessEngineConfiguration().getScriptingEnvironment().execute(actOfAcceptanceScript, delegateTask).toString();
//        FileValue actOfAcceptanceDocument = null;
//        try {
//            actOfAcceptanceDocument = Variables.fileValue("actOfAcceptance.doc").file(actOfAcceptanceHtml.getBytes("utf-8")).mimeType("application/msword").create();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e.getMessage());
//        }
//        delegateTask.setVariable("actOfAcceptanceDocument", actOfAcceptanceDocument);
//    }
//}




@Service("generateActOfAcceptance1")
public class GenerateActOfAcceptance implements TaskListener {

    private final CompiledScript template;

    @Autowired
    public GenerateActOfAcceptance(ScriptEngineManager manager) throws ScriptException {
        ScriptEngine groovy = manager.getEngineByName("groovy");
        InputStreamReader reader = new InputStreamReader(TaskListener.class.getResourceAsStream("/actOfAcceptance.groovy"));
        this.template = ((Compilable)groovy).compile(reader);
    }


    @Override
    public void notify(DelegateTask delegateTask) {

        try {
            Bindings bindings = template.getEngine().createBindings();
            bindings.put("delegateTask", delegateTask);
            String actOfAcceptanceHtml = String.valueOf(template.eval(bindings));

            FileValue actOfAcceptanceDocument = null;
            actOfAcceptanceDocument = Variables.fileValue("actOfAcceptance.doc").file(actOfAcceptanceHtml.getBytes("utf-8")).mimeType("application/msword").create();
            delegateTask.setVariable("actOfAcceptanceDocument", actOfAcceptanceDocument);
        } catch (ScriptException e) {
            throw new RuntimeException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
