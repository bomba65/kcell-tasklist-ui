package kz.kcell.bpm;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MailTaskCandidatesListener implements TaskListener {

    private final static Logger LOGGER = Logger.getLogger(MailTaskCandidatesListener.class.getName());

    MailConfiguration configuration = MailConfigurationFactory.getConfiguration();

    public void notify(DelegateTask delegateTask) {
        Set<IdentityLink> candidates = delegateTask.getCandidates();
        String taskId = delegateTask.getId();

        if (candidates == null || candidates.isEmpty()) {
            LOGGER.fine("No candidates defined, aborting mail notification");
            return;
        }

        Authentication currentAuthentication = delegateTask.getProcessEngineServices().getIdentityService().getCurrentAuthentication();

        // Get User Profile from User Management
        IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
        String recipients = candidates.stream()
                .filter(identityLink -> identityLink.getUserId() != null)
                .map(IdentityLink::getUserId)
                .filter(userId -> currentAuthentication == null || !userId.equals(currentAuthentication.getUserId()))
                .flatMap(userId -> identityService.createUserQuery().userId(userId).list().stream())
                .map(User::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .collect(Collectors.joining(","));

        if (!recipients.isEmpty()) {
            sendMail(delegateTask, recipients, taskId);
        }

    }

    protected void sendMail(DelegateTask delegateTask, String recipients, String taskId) {
        Email email = new SimpleEmail();
        email.setCharset("utf-8");
        email.setHostName(configuration.getProperties().getProperty("mail.smtp.host", "mail"));
        email.setSmtpPort(Integer.valueOf(configuration.getProperties().getProperty("mail.smtp.port", "1025")));

        try {
            email.setFrom(configuration.getSender());
            email.setSubject("Task assigned: " + delegateTask.getName());

            final String baseUrl = configuration.getProperties().getProperty("mail.message.baseurl", "http://localhost");

            email.setMsg(String.format("В рамках процесса одобрения заявок на проведение работ, в системе Kcell Workflow создана заявка, ожидающая вашего участия. Для просмотра заявки необходимо пройти по следующей ссылке: %s \n" +
                    "Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы: https://hub.kcell.kz/x/kYNoAg\n" +
                    "\n" +
                    " \n" +
                    "Открыть Kcell Workflow вы можете пройдя по следующей ссылке: <b>https://flow.kcell.kz</b>\n" +
                    "\n" +
                    "Для входа в систему используйте свой корпоративный логин (Name.Surname@kcell.kz)* и пароль.\n" +
                    "\n" +
                    " \n" +
                    "При возникновении каких-либо проблем в работе с системой, отправьте письмо в <b>support_flow@kcell.kz</b> с описанием возникшей проблемы.\n" +
                    "\n" +
                    " \n" +
                    "*-имя и фамилию в логине нужно писать с заглавной буквы. Например: <b>Petr.Petrov@kcell.kz</b>", baseUrl + "/kcell-tasklist-ui/#/?task=" + taskId));

            email.setTo(Arrays.asList(InternetAddress.parse(recipients)));
            email.setBcc(Arrays.asList(InternetAddress.parse("Askar.Slambekov@kcell.kz, Yernaz.Kalingarayev@kcell.kz")));

            email.send();
            LOGGER.info("Task Assignment Email successfully sent to '" + recipients + "'.");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not send email", e);
        }
    }
}
