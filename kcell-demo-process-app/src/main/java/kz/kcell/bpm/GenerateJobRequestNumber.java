package kz.kcell.bpm;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.net.URI;
import java.util.Date;
import java.util.Scanner;

public class GenerateJobRequestNumber implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
//        String project = (String) delegateTask.getVariable("project");
//        String siteName = (String) delegateTask.getVariable("siteName");
//        String site = (String) delegateTask.getVariable("site");
//        String counterNumber = (String) delegateTask.getVariable("counterNumber");
//        if (counterNumber == null) {
//            int counter = getNextSiteCounter(site);
//            counterNumber = String.format("%04d", counter);
//        }
//        delegateTask.setVariable("counterNumber", counterNumber);
//        String jrNumber = String.format("%s-%s-%ty-%s_%s", "###", project.replaceAll(" ", ""), new Date(), counterNumber, siteName);
//        delegateTask.setVariable("jrNumber", jrNumber);
    }

    public int getNextSiteCounter(String siteId) {
        try {
            HttpGet httpGet = new HttpGet(new URI("http://assets:8080/asset-management/sitecounter/" + siteId));
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Scanner scanner = new Scanner(httpResponse.getEntity().getContent());
            return scanner.nextInt();
        } catch (Exception e) {
            throw new RuntimeException("Could not get next SiteCounter for SiteID: " + siteId, e);
        }
    }
}
