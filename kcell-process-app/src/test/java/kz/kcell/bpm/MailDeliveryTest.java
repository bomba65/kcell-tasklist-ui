package kz.kcell.bpm;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.mail.config.PropertiesMailConfiguration;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.junit.BeforeClass;
import org.junit.Rule;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Deployment(resources = "revision.bpmn")
public class MailDeliveryTest {
    static {
        // this is to avoid MacOS DNS performance bug
        System.setProperty("mail.host", "localhost");
        System.setProperty("mail.smtp.localhost", "localhost");
        System.setProperty("mail.smtp.localaddress", "127.0.0.1");
    }

    static final Configuration cfg = new Configuration();

    @BeforeClass
    public static void init(){
        cfg.setClassForTemplateLoading(MailDeliveryTest.class, "/");
    }

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetup.verbose(ServerSetupTest.SMTP_IMAP));

    //@Test
    public void testDirectEmail() throws Exception {
        MailService mailService = MailServiceFactory.getService(new PropertiesMailConfiguration());
        Session smtpSession = mailService.getSession();

        Message msg = new MimeMessage(smtpSession);
        msg.setFrom(new InternetAddress("foo@example.com"));
        msg.addRecipient(Message.RecipientType.TO,
                new InternetAddress("bar@example.com"));

        String subject = "Привет";
        msg.setSubject(subject);

        Multipart multiPart = new MimeMultipart();

        String body = "<h1>Привет</h1>";
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(body, "text/plain; charset=UTF-8");
        multiPart.addBodyPart(htmlPart);

        msg.setContent(multiPart);
        Transport.send(msg);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);

        MimeMessage message = receivedMessages[0];

        assertEquals(subject, message.getSubject());

        assertTrue(message.getContent() instanceof MimeMultipart);

        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
        assertEquals(1, mimeMultipart.getCount());

        assertEquals(body, mimeMultipart.getBodyPart(0).getContent().toString().trim());

    }

    //@Test
    public void testGetStarterEmail() throws Exception {
        // Create the user that will be informed on assignment
        IdentityService identityService = processEngineRule.getIdentityService();
        User newUser = identityService.newUser("testGetStarterEmail");
        newUser.setEmail("testGetStarterEmail@kcell.kz");
        identityService.saveUser(newUser);

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessEngineServices()).thenReturn(processEngineRule);

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("starter", "testGetStarterEmail");
        bindings.put("execution", execution);

        Object result = engine.eval(new InputStreamReader(this.getClass().getResourceAsStream("/GetStarterEmail.groovy")), bindings);

        assertEquals("testGetStarterEmail@kcell.kz, Askar.Slambekov@kcell.kz, Yernaz.Kalingarayev@kcell.kz", result);
    }

    //@Test
    public void testCantFixNotification() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("starter", "cantfixuser");
        variables.put("jrNumber", "jrNumber");
        variables.put("regionGroupHeadApprovalComment", "Что-то там не так");
        variables.put("regionGroupHeadApprovalTaskResult", "cantFix");
        variables.put("createJRResult", "rejected");

        // Create the user that will be informed on assignment
        IdentityService identityService = processEngineRule.getIdentityService();
        User newUser = identityService.newUser("cantfixuser");
        newUser.setEmail("cantfixuser@kcell.kz");
        identityService.saveUser(newUser);

        identityService.setAuthenticatedUserId("demo");

        RuntimeService runtimeService = processEngineRule.getRuntimeService();

        ProcessInstanceWithVariables processInstanceWithVariables = runtimeService
                .createProcessInstanceByKey("Revision")
                .startBeforeActivity("Notify_Cant_Fix_JR")
                .setVariables(variables)
                .executeWithVariablesInReturn();

        assertTrue(processInstanceWithVariables.isEnded());

        MimeMessage[] notifications = greenMail.getReceivedMessages();

        assertTrue(notifications.length > 0);

        MimeMessage notification = notifications[0];

        assertEquals("Can't fix JR", notification.getSubject());
        assertTrue(GreenMailUtil.getAddressList(notification.getAllRecipients()).contains(newUser.getEmail()));

        Template template = cfg.getTemplate("CantFixJobRequestMessage.ftl");
        StringWriter writer = new StringWriter();
        template.process(variables, writer);

        String expectedMessage = writer.toString().replace("\n", "\r\n");

        assertTrue(notification.getContent() instanceof MimeMultipart);

        MimeMultipart mp = (MimeMultipart) notification.getContent();

        assertEquals(1, mp.getCount());

        assertEquals(expectedMessage.trim(), mp.getBodyPart(0).getContent().toString().trim());
    }
}
