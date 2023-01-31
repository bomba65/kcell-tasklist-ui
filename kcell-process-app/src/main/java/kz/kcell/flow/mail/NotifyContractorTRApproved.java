package kz.kcell.flow.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.minio.MinioClient;
import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.spin.SpinList;
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
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.script.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service("notifyContractorTRApproved")
@Log
public class NotifyContractorTRApproved implements JavaDelegate {

    @Autowired
    JavaMailSender mailSender;


    @Autowired
    private Minio minioClient;

    @Value("${mail.sender:flow@kcell.kz}")
    private String sender;

    @Autowired
    ScriptEngineManager manager;

    @Autowired
    private boolean isEmailDoSend;

    private static final Map<String, String> contractorsCode =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("1", "avrora");
            map.put("2", "aicom");
            map.put("3", "spectr");
            map.put("4", "lse");
            map.put("5", "kcell");
            map.put("6", "alta");
            map.put("7", "logycom");
            map.put("8", "arlan");
            map.put("9", "inter");
            return Collections.unmodifiableMap(map);
        })).get();

    protected void workAgreement2022sendMail(DelegateExecution delegateExecution) {
        try {
            String siteRegion = delegateExecution.getVariable("siteRegion").toString();
            String contractor = delegateExecution.getVariable("contractor").toString();

            if("4".equals(contractor) && "nc".equals(siteRegion)){
                siteRegion = "astana";
            }
            Object files1=delegateExecution.getVariable("attach_tr_inhouseFiles");
            Object files2=delegateExecution.getVariable("attach_tr_inhouse_additionalFiles");
            Object files3=delegateExecution.getVariable("upload_additional_tr_contractorFiles");

            System.setProperty("mail.mime.splitlongparameters", "false");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setFrom(sender);
            messageHelper.setSubject("TR Approved");

            ScriptEngine groovy = manager.getEngineByName("groovy");
            InputStreamReader reader = new InputStreamReader(ExecutionListener.class.getResourceAsStream("/revision/TRApproved.groovy"));
            final CompiledScript template = ((Compilable) groovy).compile(reader);
            String variableName="upload_tr_contractorFiles";
            if (files1!=null) {
                variableName="attach_tr_inhouseFiles";
            } else if (files2!=null) {
                variableName="attach_tr_inhouse_additionalFiles";
            } else if (files3!=null) {
                variableName="upload_additional_tr_contractorFiles";
            }
                SpinJsonNode files = delegateExecution.<JsonValue>getVariableTyped(variableName).getValue();
                if (files.isArray()) {
                    SpinList<SpinJsonNode> filesList = files.elements();
                    for (SpinJsonNode file : filesList) {
                        String path = file.prop("path").stringValue();
                        String name = file.prop("name").stringValue();

                        InputStream is = minioClient.getObject(path);
                        DataSource source = new ByteArrayDataSource(is, file.prop("type").stringValue());
                        messageHelper.addAttachment(name, source);
                        is.close();
                    }
                }

            Bindings bindings = groovy.createBindings();
            bindings.put("execution", delegateExecution);

            messageHelper.setText(String.valueOf(template.eval(bindings)), true);

            IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
            String  contractorGroup = "5".equals(contractor) ? siteRegion + "_engineer" : siteRegion + "_contractor_" + contractorsCode.get(contractor);
            SpinJsonNode initiatorFull = delegateExecution.<JsonValue>getVariableTyped("initiatorFull").getValue();
            Set<String> contractorGroupSet = identityService.createUserQuery().memberOfGroup(contractorGroup).list().stream()
                .map(User::getEmail)
                .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
                .collect(Collectors.toSet());
            contractorGroupSet.add(initiatorFull.prop("email").stringValue());
            String recipients = String.join(",", contractorGroupSet);
            messageHelper.setTo(InternetAddress.parse(recipients));

            if (isEmailDoSend) {
                mailSender.send(message);
            }
            log.info("Task Assignment Email successfully sent to " + recipients + "'.");
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Could not send email to assignee", e);
        }
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        if (delegateExecution.getVariable("mainContract").equals("2022Work-agreement")
            && delegateExecution.getVariableLocal("trapproved") != null && delegateExecution.getVariableLocal("trapproved").toString().equals("yes")) {
            workAgreement2022sendMail(delegateExecution);
        }
    }
}
