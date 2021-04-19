package kz.kcell.flow.ma;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.script.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service("monthlyActSaveActs")
@Log
public class MonthlyActSaveActs implements JavaDelegate {

    @Autowired
    JavaMailSender mailSender;

    @Value("${mail.sender:flow@kcell.kz}")
    private String sender;

    @Autowired
    ScriptEngineManager manager;

    @Autowired
    IdentityService identityService;

    @Autowired
    Minio minioClient;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();

        String maNumber = delegateExecution.getVariable("maNumber").toString();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
        messageHelper.setFrom(sender);
        messageHelper.setSubject("Monthly Act " + maNumber + " act");

        ScriptEngine groovy = manager.getEngineByName("groovy");
        ScriptEngine groovyEngine = groovy;
        InputStreamReader reader = new InputStreamReader(MonthlyActSaveActs.class.getResourceAsStream("/monthlyAct/newActNotificationText.groovy"));
        final CompiledScript template = ((Compilable) groovy).compile(reader);

        Bindings bindings = groovyEngine.createBindings();
        bindings.put("maNumber", maNumber);
        messageHelper.setText(String.valueOf(template.eval(bindings)), true);

        Set<String> contractorEmails  = identityService.createUserQuery().memberOfGroup("hq_contractor_" + delegateExecution.getVariable("contractor")).list().stream()
            .map(User::getEmail)
            .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
            .collect(Collectors.toSet());

        if("true".equals(delegateExecution.getVariable("hasRollout"))){
            contractorEmails.addAll(identityService.createUserQuery().memberOfGroup("hq_rollout").list().stream()
                .map(User::getEmail)
                .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
                .collect(Collectors.toSet()));
        }
        if("true".equals(delegateExecution.getVariable("hasPO"))){
            contractorEmails.addAll(identityService.createUserQuery().memberOfGroup("hq_optimization_specialist").list().stream()
                .map(User::getEmail)
                .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
                .collect(Collectors.toSet()));
        }
        if("true".equals(delegateExecution.getVariable("hasTNU1")) || "true".equals(delegateExecution.getVariable("hasTNU2")) || "true".equals(delegateExecution.getVariable("hasTNU3"))){
            contractorEmails.addAll(identityService.createUserQuery().memberOfGroup("hq_transmission_specialist").list().stream()
                .map(User::getEmail)
                .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
                .collect(Collectors.toSet()));
        }
        if("true".equals(delegateExecution.getVariable("hasSFM"))){
            contractorEmails.addAll(identityService.createUserQuery().memberOfGroup("hq_infrastructure_specialist").list().stream()
                .map(User::getEmail)
                .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
                .collect(Collectors.toSet()));
        }
        if("true".equals(delegateExecution.getVariable("hasSAO"))){
            contractorEmails.addAll(identityService.createUserQuery().memberOfGroup("hq_operation_specialist").list().stream()
                .map(User::getEmail)
                .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
                .collect(Collectors.toSet()));
        }
        messageHelper.setTo(InternetAddress.parse(contractorEmails.stream().collect(Collectors.joining(","))));

        InputStreamReader reader1 = new InputStreamReader(MonthlyActSaveActs.class.getResourceAsStream("/monthlyAct/generatePdfAttachment.groovy"));
        final CompiledScript template1 = ((Compilable) groovy).compile(reader1);

        Bindings bindings1 = groovyEngine.createBindings();

        bindings1.put("businessKey", delegateExecution.getProcessBusinessKey());
        bindings1.put("selectedRevisions", delegateExecution.getVariable("selectedRevisions"));
        bindings1.put("monthOfFormalPeriod", delegateExecution.getVariable("monthOfFormalPeriod").toString());
        bindings1.put("yearOfFormalPeriod", delegateExecution.getVariable("yearOfFormalPeriod").toString());
        bindings1.put("subcontractor", delegateExecution.getVariable("subcontractor").toString());

        String result = String.valueOf(template1.eval(bindings1));
        InputStream is = new ByteArrayInputStream(result.getBytes());
        String fileName = maNumber + ".html";
        DataSource source = new ByteArrayDataSource(is, "text/html");

        messageHelper.addAttachment(fileName, source);
        mailSender.send(message);

        Map<String, Object> variables = new HashMap<>();
        variables.put("acceptPerformedJob", "invoiced");
        variables.put("monthActNumber", delegateExecution.getVariable("maNumber"));
        variables.put("invoiceNumber", delegateExecution.getVariable("maNumber"));
        variables.put("invoiceDate", new Date());

        SpinJsonNode selectedRevisions = delegateExecution.<JsonValue>getVariableTyped("selectedRevisions").getValue();
        for(String revisionId: selectedRevisions.fieldNames()){
            runtimeService.setVariables(revisionId, variables);
        }
    }
}
