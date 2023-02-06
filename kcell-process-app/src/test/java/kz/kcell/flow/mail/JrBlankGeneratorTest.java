package kz.kcell.flow.mail;

import kz.kcell.Util;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JrBlankGeneratorTest {
    @InjectMocks
    JrBlankGenerator jrBlankGenerator;

    /**
     * Tests generate method in JrBlankGenerator on generating an output and saving a xlsx file
     */
    @Test
    public void generateBlankWithJobWorksGreaterThan17Test() throws Exception {
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        when(delegateExecution.getVariable("mainContract")).thenReturn("2022Work-agreement");
        when(delegateExecution.getVariable("reason")).thenReturn("4");
        when(delegateExecution.getVariable("project")).thenReturn("N/A");
        when(delegateExecution.getVariable("siteAddress")).thenReturn("West, Атырауская область, Атырау (г.а.), Атырау (село), ул. Кульманова, 111А, ТРЦ «Байзаар»");
        when(delegateExecution.getVariable("cityNameShow")).thenReturn("West");
        when(delegateExecution.getVariable("siteName")).thenReturn("61346");
        when(delegateExecution.getVariable("site_name")).thenReturn("61346BAIZAR");
        when(delegateExecution.getVariable("jrNumber")).thenReturn("З-ФОР-Э-23-0016З-ФОР-Э-23-0016");
        when(delegateExecution.getVariable("jobDescription")).thenReturn("None");
        when(delegateExecution.getVariable("explanation")).thenReturn("В связи с реконструкцией 3 этажа ТРЦ, просим произвести перенос всего нашего оборудования в специально отведенное помещение для серверного оборудования.");
        when(delegateExecution.getVariable("contractor")).thenReturn("10");
        when(delegateExecution.getVariable("regionApproval")).thenReturn("Admin Admin");
        when(delegateExecution.getVariable("centralApproval")).thenReturn("None");
        when(delegateExecution.getVariable("requestedDate")).thenReturn(format.parse("2023-02-01T18:16:31.627+0600"));
        when(delegateExecution.getVariable("workStartDate")).thenReturn(format.parse("2023-02-01T18:16:31.415+0600"));
        when(delegateExecution.getVariable("integrationRunDate")).thenReturn(format.parse("2023-02-01T18:16:31.415+0600"));
        when(delegateExecution.getVariable("workCompletionDate")).thenReturn(format.parse("2023-02-26T18:16:31.415+0600"));
        when(delegateExecution.getVariable("priority")).thenReturn("regular");

        String jobWorksString = Util.getResourceFileAsString("camunda-variables/job-works.json");
        TypedValue jobWorks = mock(TypedValue.class);
        when(jobWorks.getValue()).thenReturn(jobWorksString);
        when(delegateExecution.getVariableTyped("jobWorks")).thenReturn(jobWorks);

        String initiatorFullString = Util.getResourceFileAsString("camunda-variables/initiator-full.json");
        TypedValue initiatorFull = mock(TypedValue.class);
        when(initiatorFull.getValue()).thenReturn(initiatorFullString);
        when(delegateExecution.getVariableTyped("initiatorFull")).thenReturn(initiatorFull);

        when(delegateExecution.getVariable("unitWorkPrice_jr")).thenReturn("null");
        when(delegateExecution.getVariable("siteRegion")).thenReturn("west");
        when(delegateExecution.getVariable("oblastName")).thenReturn("Атырауская область");

        byte[] bytes = jrBlankGenerator.generate(delegateExecution);

        FileOutputStream fos = new FileOutputStream("output.xlsx");
        fos.write(bytes);
        fos.close();
        assertThat(bytes).isNotEmpty();
    }
}

