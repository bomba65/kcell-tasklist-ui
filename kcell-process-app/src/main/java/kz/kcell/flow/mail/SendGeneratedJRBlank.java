package kz.kcell.flow.mail;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.spin.plugin.variable.SpinValues;
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
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service("sendJobRequestBlank")
@Log
public class SendGeneratedJRBlank implements JavaDelegate {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    JrBlankGenerator jrBlankGenerator;

    @Autowired
    private Minio minioClient;

    @Value("${mail.sender:flow@kcell.kz}")
    private String sender;

    @Autowired
    ScriptEngineManager manager;

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
            return Collections.unmodifiableMap(map);
        })).get();

    protected void sendMail(DelegateExecution delegateExecution, String assignee, String recipient) {
        try {
            String siteRegion = delegateExecution.getVariable("siteRegion").toString();
            String reason = delegateExecution.getVariable("reason").toString();
            String jrNumber = (String) delegateExecution.getVariable("jrNumber");
            String contractor = delegateExecution.getVariable("contractor").toString();
            byte[] jrBlank = jrBlankGenerator.generate(delegateExecution);
            ByteArrayInputStream is = new ByteArrayInputStream(jrBlank);

            DataSource source = new ByteArrayDataSource(is, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

            messageHelper.setFrom(sender);
            messageHelper.setSubject("JR " + jrNumber + " Blank");

            if (delegateExecution.getVariableLocal("sendToContractor") != null && delegateExecution.getVariableLocal("sendToContractor").toString().equals("yes")) {
                ScriptEngine groovy = manager.getEngineByName("groovy");
                ScriptEngine groovyEngine = groovy;
                InputStreamReader reader = new InputStreamReader(ExecutionListener.class.getResourceAsStream("/revision/ContractorJrBlankBodyHtml.groovy"));
                final CompiledScript template = ((Compilable) groovy).compile(reader);

                Bindings bindings = groovyEngine.createBindings();
                bindings.put("execution", delegateExecution);
                bindings.put("status", delegateExecution.getVariable("status"));
                bindings.put("starter", delegateExecution.getVariable("starter"));
                bindings.put("sitename", delegateExecution.getVariable("site_name"));
                messageHelper.setText(String.valueOf(template.eval(bindings)), true);
            } else {
                messageHelper.setText("Your JR Approved. JR Blank attached\n" +
                    "\n" +
                    "\n" +
                    "Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы: https://hub.kcell.kz/x/kYNoAg");
            }

            IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();

            Set<String> ccList = identityService.createUserQuery().userId(delegateExecution.getVariable("starter").toString()).list().stream().map(user -> user.getId()).collect(Collectors.toSet());
            ccList.addAll(Arrays.asList("Stanislav.Li@kcell.kz"));
            if (reason != null && reason.equals("3")) {
                ccList.add("Tatyana.Solovyova@kcell.kz");
            }
            if (delegateExecution.getVariableLocal("sendToContractor") != null && delegateExecution.getVariableLocal("sendToContractor").toString().equals("yes")) {
                String contractorGroup = siteRegion + "_contractor_" + contractorsCode.get(contractor);

                String recipientsSet = identityService.createUserQuery().memberOfGroup(contractorGroup).list().stream()
                    .map(User::getEmail)
                    .filter(userEmail -> userEmail != null && !userEmail.isEmpty())
                    .collect(Collectors.joining(","));

                messageHelper.setTo(InternetAddress.parse(recipientsSet));
                messageHelper.setCc(InternetAddress.parse(ccList.stream().collect(Collectors.joining(","))));
            } else {
                messageHelper.setTo(InternetAddress.parse(ccList.stream().collect(Collectors.joining(","))));
            }

            String path = delegateExecution.getProcessInstanceId() + "/" + jrNumber.replace("-####", "").replace("-##", "") + ".xlsx";
            String fileName = jrNumber.replace("-####", "").replace("-##", "") + ".xlsx";

            messageHelper.addAttachment(fileName, source);

            mailSender.send(message);

            log.info("Task Assignment Email successfully sent to user '" + assignee + "' with address '" + recipient + "'.");

            ByteArrayInputStream bis = new ByteArrayInputStream(jrBlank);
            minioClient.saveFile(path, bis, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            bis.close();

            String json = "{\"name\" :\"" + fileName + "\",\"path\" : \"" + path + "\"}";
            delegateExecution.setVariable("jrBlank", SpinValues.jsonValue(json));

            delegateExecution.setVariable("isNewProcessCreated", "false");

        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Could not send email to assignee", e);
        }
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String recipient = (String) delegateExecution.getVariable("starter");

        if (recipient == null) {
            log.warning("Recipient is null for activity instance " + delegateExecution.getActivityInstanceId() + ", aborting mail notification");
            return;
        }

        IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
        User user = identityService.createUserQuery().userId(recipient).singleResult();

        if (user != null) {

            // Get Email Address from User Profile
            String recipientEmail = user.getEmail();

            if (recipient != null && !recipient.isEmpty()) {

                sendMail(delegateExecution, recipient, recipientEmail);

            } else {
                log.warning("Not sending email to user " + recipient + "', user has no email address.");
            }

        } else {
            log.warning("Not sending email to user " + recipient + "', user is not enrolled with identity service.");
        }
    }
}
