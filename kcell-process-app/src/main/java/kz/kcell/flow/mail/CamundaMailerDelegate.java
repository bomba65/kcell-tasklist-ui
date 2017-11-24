package kz.kcell.flow.mail;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.internet.MimeMessage;

public class CamundaMailerDelegate implements JavaDelegate {

    @Value("${mail.sender:flow@kcell.kz}")
    private String sender;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo(separateEmails(String.valueOf(delegateExecution.getVariableLocal("to"))));
        helper.setText(String.valueOf(delegateExecution.getVariableLocal("html")), true);
        helper.setSubject(String.valueOf(delegateExecution.getVariableLocal("subject")));
        helper.setFrom(sender);

        mailSender.send(message);
    }

    private String[] separateEmails(String addresses){
        if(addresses!=null && !"".equals(addresses)){
            return addresses.split(",");
        }
        return new String[0];
    }
}
