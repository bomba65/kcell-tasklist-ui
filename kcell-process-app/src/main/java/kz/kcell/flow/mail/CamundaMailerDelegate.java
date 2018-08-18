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

        String subject = String.valueOf(delegateExecution.getVariableLocal("subject"));
        String businessKey = delegateExecution.getProcessInstance().getBusinessKey();
        if(subject!=null && businessKey!=null && !businessKey.equals("null") && !subject.contains(businessKey)){
            subject = businessKey + ", " + subject;
        }

        String addresses = String.valueOf(delegateExecution.getVariableLocal("to"));

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
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo(separateEmails(addresses));
        helper.setSubject(subject);
        helper.setText(String.valueOf(delegateExecution.getVariableLocal("html")), true);
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
