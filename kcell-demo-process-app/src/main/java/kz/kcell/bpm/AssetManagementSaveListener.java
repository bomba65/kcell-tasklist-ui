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
                //MicroWave section
                System.out.println("========== MICROWAVE =========");
                try {
                    JsonNode equipment = fillSite.get("microWave").get("equipment");
                    JsonNode installation = fillSite.get("microWave").get("installation");
                    StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}");
                    String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + fillSite.get("microWave").get("installation").get("id").toString().replace("\"", "");
                    System.out.println(installationUrl);
                    HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                    installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                    installationHttpPatch.setEntity(installationInputData);

                    CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                    CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                    System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());

                    StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}");
                    String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + fillSite.get("microWave").get("equipment").get("id").toString().replace("\"", "");
                    System.out.println(equipmentUrl);
                    HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
                    equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                    equipmentHttpPatch.setEntity(equipmentInputData);

                    CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
                    CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
                    System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Facility section
                System.out.println("========== FACILITY =========");
                try {
                    for (JsonNode facility : fillSite.get("facilities")) {
                        String definitionId = facility.get("definition").textValue();
                        String definitionUrl = "/asset-management/api/facilityDefinitions/";
                        if (definitionId != null && !definitionId.isEmpty()) {
                            definitionUrl += definitionId;
                        }
                        System.out.println(facility.toString());
                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facility.get("_facilityId").toString().replace("\"", "");
                        StringEntity installationInputData = new StringEntity("{\"params\":" + facility.get("object").toString() + ", \"definition\": \"" + definitionUrl + "\"}");
                        HttpPatch facilityHttpPatch = new HttpPatch(new URI(facilityUrl));
                        facilityHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                        facilityHttpPatch.setEntity(installationInputData);

                        CloseableHttpClient facilityHttpClient = HttpClients.createDefault();
                        CloseableHttpResponse facilityResponse = facilityHttpClient.execute(facilityHttpPatch);
                        System.out.println("facilityResponse: " + facilityResponse.getStatusLine().getStatusCode());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Cabinets sections
                System.out.println("========== CABINETS =========");
                try {
                    for (JsonNode cabinet : fillSite.get("cabinets")) {
                        JsonNode equipment = cabinet.get("equipment").get("object");
                        JsonNode installation = cabinet.get("installation").get("object");
                        StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}");
                        String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + cabinet.get("_installationId").toString().replace("\"", "");
                        System.out.println(installationUrl);
                        HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                        installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                        installationHttpPatch.setEntity(installationInputData);

                        CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                        CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                        System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());

                        StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}");
                        String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + cabinet.get("_equipmentId").toString().replace("\"", "");
                        System.out.println(equipmentUrl);
                        HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
                        equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                        equipmentHttpPatch.setEntity(equipmentInputData);

                        CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
                        CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
                        System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
