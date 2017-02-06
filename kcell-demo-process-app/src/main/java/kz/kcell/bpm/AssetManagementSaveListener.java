package kz.kcell.bpm;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.net.URI;
public class AssetManagementSaveListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            System.out.println("SENDIGN");
            System.out.println("{\"params\":" + delegateTask.getVariableTyped("jobWorks").getValue().toString() + "}");
            System.out.println("==============");
            StringEntity inputData = new StringEntity("{\"params\":" + delegateTask.getVariableTyped("jobWorks").getValue().toString() + "}");
            HttpPatch httpPatch = new HttpPatch(new URI("http://assets:8080/asset-management/api/installationInstances/1"));
            httpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPatch.setEntity(inputData);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpPatch);
            System.out.println(response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
