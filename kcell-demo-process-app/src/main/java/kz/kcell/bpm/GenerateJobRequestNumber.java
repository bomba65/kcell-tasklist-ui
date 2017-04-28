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
        //"###-"+$scope.project.split(' ').join('')+"-"+(new Date().getFullYear().toString().substr(-2))+"####_"+$scope.siteName;
        String project = (String) delegateTask.getVariable("project");
        String siteName = (String) delegateTask.getVariable("siteName");
        String site = (String) delegateTask.getVariable("site");
        int counter = getNextSiteCounter(site);
        String jrNumber = String.format("%s-%s-%ty-%04d_%s", "###", project.replaceAll(" ", ""), new Date(), counter, siteName);
        delegateTask.setVariable("jrNumber", jrNumber);
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
