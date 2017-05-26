package kz.kcell.bpm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;

import javax.mail.internet.InternetAddress;

public class MailTaskAssigneeListener implements TaskListener {

    private final static Logger LOGGER = Logger.getLogger(MailTaskAssigneeListener.class.getName());

    MailConfiguration configuration = MailConfigurationFactory.getConfiguration();

    public void notify(DelegateTask delegateTask) {
        if (!TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) {
            LOGGER.fine("Skip mailing, not an Assignment event");
            return;
        }

        String assignee = delegateTask.getAssignee();
        String taskId = delegateTask.getId();

        if (assignee == null) {
            LOGGER.warning("Assignee of Task " + taskId + " is null, aborting mail notification");
            return;
        }

        Authentication currentAuthentication = delegateTask.getProcessEngineServices().getIdentityService().getCurrentAuthentication();
        if (currentAuthentication != null) {
            String currentUserId = currentAuthentication.getUserId();

            if (assignee.equals(currentUserId)) {
                LOGGER.fine("Skip mailing, assignee is the current user");
                return;
            }

        }

        // Get User Profile from User Management
        IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
        User user = identityService.createUserQuery().userId(assignee).singleResult();

        if (user != null) {

            // Get Email Address from User Profile
            String recipient = user.getEmail();

            if (recipient != null && !recipient.isEmpty()) {

                sendMail(delegateTask, assignee, taskId, recipient);

            } else {
                LOGGER.warning("Not sending email to user " + assignee + "', user has no email address.");
            }

        } else {
            LOGGER.warning("Not sending email to user " + assignee + "', user is not enrolled with identity service.");
        }
    }

    protected void sendMail(DelegateTask delegateTask, String assignee, String taskId, String recipient) {
        Email email = new SimpleEmail();
        email.setCharset("utf-8");
        email.setHostName(configuration.getProperties().getProperty("mail.smtp.host", "mail"));
        email.setSmtpPort(Integer.valueOf(configuration.getProperties().getProperty("mail.smtp.port", "1025")));

        try {
            email.setFrom(configuration.getSender());
            email.setSubject("Task assigned: " + delegateTask.getName());

            final String baseUrl = configuration.getProperties().getProperty("mail.message.baseurl", "http://localhost");

            email.setMsg("Добрый день.\n" +
                    "\n" +
                    "В рамках процесса одобрения заявок на проведение работ, в системе Kcell Workflow создана заявка, ожидающая вашего одобрения. Для просмотра заявки необходимо пройти по следующей ссылке: " + baseUrl + "/camunda/app/tasklist/default/#/?task=" + taskId +
                    "\n" +
                    "\n" +
                    "Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы: https://hub.kcell.kz/x/kYNoAg");

            email.addTo(recipient);
            email.setBcc(Arrays.asList(InternetAddress.parse("Askar.Slambekov@kcell.kz, Yernaz.Kalingarayev@kcell.kz")));

            email.send();
            LOGGER.info("Task Assignment Email successfully sent to user '" + assignee + "' with address '" + recipient + "'.");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not send email to assignee", e);
        }
    }
}
