package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.net.URI;
import java.util.Set;

public class AssetManagementSaveListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (delegateTask.getVariableTyped("fillSite") != null && delegateTask.getVariableTyped("fillSite").getValue() != null) {
                JsonNode fillSite = mapper.readTree(delegateTask.getVariableTyped("fillSite").getValue().toString());
                JsonNode equipment = fillSite.get("microWave").get("equipment");
                JsonNode installation = fillSite.get("microWave").get("installation");
                System.out.println("SENDING: =================");
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println(equipment.toString());
                System.out.println(installation.toString());
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("SENT: =================");
                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}");
                String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + fillSite.get("microWave").get("installation").get("id").toString().replace("\"","");
                System.out.println(installationUrl);
                HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                installationHttpPatch.setEntity(installationInputData);

                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());

                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}");
                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + fillSite.get("microWave").get("equipment").get("id").toString().replace("\"","");
                System.out.println(equipmentUrl);
                HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
                equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                equipmentHttpPatch.setEntity(equipmentInputData);

                CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
                CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
                System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
            } else {
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("ERROR: =================");
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
