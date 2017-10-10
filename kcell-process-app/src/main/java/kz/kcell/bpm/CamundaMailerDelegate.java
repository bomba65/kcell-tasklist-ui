package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.internet.MimeMessage;

public class CamundaMailerDelegate implements JavaDelegate {

    @Autowired
    private JavaMailSender sender;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo((String)delegateExecution.getVariable("to"));
        helper.setText((String) delegateExecution.getVariable("html"), true);
        helper.setSubject((String) delegateExecution.getVariable("subject"));

        sender.send(message);
    }
}