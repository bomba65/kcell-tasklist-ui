package kz.kcell.bpm;

import org.apache.commons.mail.HtmlEmail;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;

import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        HtmlEmail email = new HtmlEmail();
        email.setCharset("utf-8");
        email.setHostName(configuration.getProperties().getProperty("mail.smtp.host", "mail"));
        email.setSmtpPort(Integer.valueOf(configuration.getProperties().getProperty("mail.smtp.port", "1025")));

        try {
            email.setFrom(configuration.getSender());
            email.setSubject("Task assigned: " + delegateTask.getName());

            final String baseUrl = configuration.getProperties().getProperty("mail.message.baseurl", "http://localhost");
            email.setMsg(String.format("В рамках процесса одобрения заявок на проведение работ, в системе Kcell Workflow создана заявка, ожидающая вашего участия. Для просмотра заявки необходимо пройти по следующей ссылке: <a href='%1$s'>%1$s</a> \n" +
                    "Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы: <a href='https://hub.kcell.kz/x/kYNoAg'>https://hub.kcell.kz/x/kYNoAg</a>\n" +
                    "\n" +
                    " \n" +
                    "Открыть Kcell Workflow вы можете пройдя по следующей ссылке: <b><a href='https://flow.kcell.kz'>https://flow.kcell.kz</a></b>\n" +
                    "\n" +
                    "Для входа в систему используйте свой корпоративный логин (Name.Surname@kcell.kz)* и пароль.\n" +
                    "\n" +
                    " \n" +
                    "При возникновении каких-либо проблем в работе с системой, отправьте письмо в <b><a href='mailto:support_flow@kcell.kz'>support_flow@kcell.kz</a></b> с описанием возникшей проблемы.\n" +
                    "\n" +
                    " \n" +
                    "*-имя и фамилию в логине нужно писать с заглавной буквы. Например: <b><a href='mailto:Petr.Petrov@kcell.kz'>Petr.Petrov@kcell.kz</a></b>", baseUrl + "/kcell-tasklist-ui/#/?task=" + taskId));

            email.addTo(recipient);
            email.setBcc(Arrays.asList(InternetAddress.parse("Askar.Slambekov@kcell.kz, Yernaz.Kalingarayev@kcell.kz")));

            email.send();
            LOGGER.info("Task Assignment Email successfully sent to user '" + assignee + "' with address '" + recipient + "'.");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not send email to assignee", e);
        }
    }
}
