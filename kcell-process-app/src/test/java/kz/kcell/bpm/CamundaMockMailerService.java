package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class CamundaMockMailerService implements JavaDelegate {

    private final Logger LOGGER = Logger.getLogger( CamundaMockMailerService.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("\n\n  ... CamundaMockMailerService call \n\n");

        String to = (String) execution.getVariable("to");
        String subject = (String) execution.getVariable("subject");
        String html = (String) execution.getVariable("html");

        LOGGER.info("to: " + to);
        LOGGER.info("subject: " + subject);
        LOGGER.info("html: " + html);
    }
}
