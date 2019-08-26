package kz.kcell.flow.demand;

import kz.kcell.flow.files.Minio;
import kz.kcell.flow.revision.sap.SftpConfig;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


@Service("DemandSendApprovedUATPDF")
@Log
public class SendApprovedUATPDF implements JavaDelegate {

    @Value("${mail.sender:flow@kcell.kz}")
    private String sender;

    @Autowired
    private JavaMailSender mailSender;

    private Minio minioClient;

    @Autowired
    public SendApprovedUATPDF(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String fileName = String.valueOf(delegateExecution.getVariable("approvedUATPDFFileName"));
        String filePath = String.valueOf(delegateExecution.getVariable("approvedUATPDFFilePath"));
        InputStream inputStream = minioClient.getObject(filePath);
        byte[] bytes = IOUtils.toByteArray(inputStream);

        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        DataSource source = new ByteArrayDataSource(is, "application/pdf");
        helper.addAttachment(fileName, source);

        String subject = String.valueOf(delegateExecution.getVariableLocal("subject"));
        String addresses = String.valueOf(delegateExecution.getVariableLocal("to"));
        String html = String.valueOf(delegateExecution.getVariableLocal("html"));
        String[] emails = separateEmails(addresses);

        if (emails.length > 0) {
            helper.setTo(emails);
            helper.setSubject(subject);
            helper.setText(html, true);
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
