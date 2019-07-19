package kz.kcell.flow.mail;

import kz.kcell.flow.repository.ReportController;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class CamundaMailerDelegate implements JavaDelegate {

    @Value("${mail.sender:flow@kcell.kz}")
    private String sender;

    @Autowired
    private JavaMailSender mailSender;

    private final List<String> disabledProcesses = Arrays.asList("AftersalesPBX", "PBX");

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        boolean isProcessDisabled =
            delegateExecution
                .getProcessEngineServices()
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(delegateExecution.getProcessDefinitionId())
                .list()
                .stream()
                .filter(e-> disabledProcesses.contains(e.getKey()))
                .findAny()
                .isPresent();

        if (isProcessDisabled) return;

        String subject = String.valueOf(delegateExecution.getVariableLocal("subject"));
        String sendInstruction = String.valueOf(delegateExecution.getVariableLocal("sendInstruction"));
        String businessKey = delegateExecution.getProcessInstance().getBusinessKey();
        boolean isFreephone = delegateExecution
            .getProcessEngineServices()
            .getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionId(delegateExecution.getProcessDefinitionId())
            .list()
            .stream()
            .filter(e-> e.getKey().equals("freephone"))
            .findAny()
            .isPresent();
        boolean isBulkSms = delegateExecution
            .getProcessEngineServices()
            .getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionId(delegateExecution.getProcessDefinitionId())
            .list()
            .stream()
            .filter(e-> e.getKey().equals("bulksmsConnectionKAE"))
            .findAny()
            .isPresent();

        if(!(isFreephone &&  "SendTask_0t8xjuw".equals(delegateExecution.getCurrentActivityId()))  && !(isBulkSms &&  "SendTask_1wvfpnd".equals(delegateExecution.getCurrentActivityId()))  && subject!=null && businessKey!=null && !businessKey.equals("null") && !subject.contains(businessKey)){
            subject = businessKey + ", " + subject;
        }
        String addresses = String.valueOf(delegateExecution.getVariableLocal("to"));
        String ccAddresses = String.valueOf(delegateExecution.getVariableLocal("cc"));
        long isRevisionMonthlyActCount =
            delegateExecution
                .getProcessEngineServices()
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(delegateExecution.getProcessDefinitionId())
                .list()
                .stream()
                .filter(e-> e.getKey().equals("Revision") || e.getKey().equals("Invoice"))
                .count();

        if(isRevisionMonthlyActCount > 0 && addresses!=null && !addresses.contains("Yernaz.Kalingarayev@kcell.kz")){
            addresses = addresses + ",Yernaz.Kalingarayev@kcell.kz";
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        if (sendInstruction.equals("send")) {
            InputStream fis = CamundaMailerDelegate.class.getResourceAsStream("/instruction/instruction.pdf");
            byte[] bytes = IOUtils.toByteArray(fis);

            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            DataSource source = new ByteArrayDataSource(is, "application/pdf");
            helper.addAttachment("instruction.pdf", source);
        }
        String[] emails = separateEmails(addresses);
        if (isFreephone &&  "SendTask_0t8xjuw".equals(delegateExecution.getCurrentActivityId())) {
            sender = "freephone@kcell.kz";
            if (ccAddresses!=null) {
                String[] ccEmails = separateEmails(ccAddresses);
                for(String cc : ccEmails) {
                    helper.addCc(cc);
                }
            }
        }
        else if (isBulkSms &&  "SendTask_1wvfpnd".equals(delegateExecution.getCurrentActivityId())) {
            sender = "bulksms@kcell.kz";
            if (ccAddresses!=null) {
                String[] ccEmails = separateEmails(ccAddresses);
                for(String cc : ccEmails) {
                    helper.addCc(cc);
                }
            }
            String connectionType =  String.valueOf(delegateExecution.getVariable("connectionType"));
            if (connectionType!=null) {
                String fileName="";
                switch(connectionType) {
                    case "rest":
                        fileName = "REST_API.pdf";
                        break;
                    case "smpp":
                        fileName = "SMPP-v3.4.rus.pdf";
                        break;
                }
                InputStream fis = CamundaMailerDelegate.class.getResourceAsStream("/instruction/"+fileName);
                byte[] bytes = IOUtils.toByteArray(fis);
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                DataSource source = new ByteArrayDataSource(is, "application/pdf");
                helper.addAttachment("instruction.pdf", source);
            }
        } else {
            sender = "flow@kcell.kz";
        }


        if (emails.length > 0) {
            helper.setTo(emails);
            helper.setSubject(subject);
            helper.setText(String.valueOf(delegateExecution.getVariableLocal("html")), true);
            helper.setFrom(sender);

            mailSender.send(message);
        }
    }

    private String[] separateEmails(String addresses){
        if(addresses!=null && !"".equals(addresses)){
            return addresses.split(",");
        }
        return new String[0];
    }
}
