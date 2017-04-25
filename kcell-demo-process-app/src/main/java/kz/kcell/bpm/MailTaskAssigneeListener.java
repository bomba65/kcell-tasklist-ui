package kz.kcell.bpm;

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

public class MailTaskAssigneeListener implements TaskListener {

    public static final String FLOW_MAIL_HOST = "mail";
    public static final int    FLOW_MAIL_PORT = 1025;
    public static final String FLOW_BASE_URL  = "http://localhost";

    private final static Logger LOGGER = Logger.getLogger(MailTaskAssigneeListener.class.getName());

    public void notify(DelegateTask delegateTask) {
        if (!TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) {
            LOGGER.fine("Skip mailing, not an Assignment event");
            return;
        }

        String assignee = delegateTask.getAssignee();
        String taskId = delegateTask.getId();

        if (assignee == null) {
            LOGGER.warning("Assignee is null, aborting mail notification");
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
        email.setHostName(System.getProperty("FLOW_MAIL_HOST", FLOW_MAIL_HOST));
        email.setSmtpPort(Integer.getInteger("FLOW_MAIL_PORT", FLOW_MAIL_PORT));

        try {
            email.setFrom("kcell_flow@kcell.kz");
            email.setSubject("Task assigned: " + delegateTask.getName());

            final String baseUrl = System.getProperty("FLOW_BASE_URL", FLOW_BASE_URL);

            email.setMsg("Please complete: " + baseUrl + "/camunda/app/tasklist/default/#/?task=" + taskId);

            email.addTo(recipient);

            email.send();
            LOGGER.info("Task Assignment Email successfully sent to user '" + assignee + "' with address '" + recipient + "'.");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not send email to assignee", e);
        }
    }
}
