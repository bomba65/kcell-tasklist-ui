package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class AssetManagementSaveListenerNew implements TaskListener {

    private final static Logger LOGGER = Logger.getLogger(DemoDataGenerator.class.getName());
    private static String baseUri = "http://assets:8080";

    public AssetManagementSaveListenerNew() {

    }

    public AssetManagementSaveListenerNew(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (delegateTask.getVariableTyped("fillSite") != null && delegateTask.getVariableTyped("fillSite").getValue() != null) {
                JsonNode fillSite = mapper.readTree(delegateTask.getVariableTyped("fillSite").getValue().toString());
                saveToAssetManagement(fillSite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpResponse postEquipment(JsonNode node, String definitionId) {
        try {
            StringEntity equipmentInputData = new StringEntity("{\"params\":" + node.get("equipment").get("params").toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/" + definitionId + "\"}", "UTF-8");
            String equipmentUrl = baseUri + "/asset-management/api/equipmentInstances";
            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            equipmentPost.setEntity(equipmentInputData);
            HttpClient equipmentHttpClient = HttpClients.createDefault();
            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
            EntityUtils.consume(equipmentResponse.getEntity());
            return equipmentResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse postInstallation(JsonNode node, String definitionId, String siteUrl, String facilityUrl, String equipmentInstanceUrl) {
        try {
            String installationUrl = baseUri + "/asset-management/api/installationInstances";

            StringEntity installationInputData = new StringEntity("{\"params\":" + node.get("params").toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"/installationDefinitions/" + definitionId + "\"}", "UTF-8");
            HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
            installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            installationHttpPost.setEntity(installationInputData);

            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
            return installationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse patchEquipment(JsonNode node) {
        try {
            StringEntity equipmentInputData = new StringEntity("{\"params\":" + node.get("equipment").get("params").toString() + "}", "UTF-8");
            String equipmentUrl = baseUri + "/asset-management/api/equipmentInstances/" + node.get("equipment").get("id").toString().replace("\"", "");
            HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
            equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
            equipmentHttpPatch.setEntity(equipmentInputData);

            CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
            CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
            return equipmentResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse patchInstallation(JsonNode node) {
        try {
            StringEntity installationInputData = new StringEntity("{\"params\":" + node.get("params").toString() + "}", "UTF-8");
            String installationUrl = baseUri + "/asset-management/api/installationInstances/" + node.get("id").toString().replace("\"", "");
            System.out.println(installationUrl);
            HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
            installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
            installationHttpPatch.setEntity(installationInputData);

            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
            return installationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse deleteInstallation(JsonNode node) {
        try {
            String installationDeleteUrl = baseUri + "/asset-management/api/installationInstances/" + node.get("id").toString().replace("\"", "");
            HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
            CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
            return deleteInstallationHttpClient.execute(httpDelete);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveToAssetManagement(JsonNode fillSite) {
        Map<String, String> facilityMap = new HashMap<String, String>();
        Map<String, String> cabinetsMap = new HashMap<String, String>();
        try {
            for (JsonNode facility : fillSite.get("facilities")) {
                String definitionId = null;
                if (facility.get("definitionId") != null) {
                    definitionId = facility.get("definitionId").textValue();
                }
                String definitionUrl = "/asset-management/api/facilityDefinitions/";
                if (definitionId != null && !definitionId.isEmpty()) {
                    definitionUrl += definitionId;
                }
                if (facility.get("id").textValue() != null && facility.get("id").textValue().startsWith("_NEW")) {
                    String facilityUrl = baseUri + "/asset-management/api/facilityInstances";
                    StringEntity installationInputData = new StringEntity("{\"params\":" + facility.get("params").toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");
                    HttpPost facilityHttpPost = new HttpPost(new URI(facilityUrl));
                    facilityHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                    facilityHttpPost.setEntity(installationInputData);
                    HttpClient facilityHttpClient = HttpClients.createDefault();
                    HttpResponse facilityResponse = facilityHttpClient.execute(facilityHttpPost);
                    EntityUtils.consume(facilityResponse.getEntity());

                    HttpPost postFacilityToSite = new HttpPost(new URI(baseUri + "/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "") + "/facilities"));
                    postFacilityToSite.addHeader("Content-Type", "text/uri-list;charset=UTF-8");
                    postFacilityToSite.setEntity(new StringEntity(facilityResponse.getFirstHeader("Location").getValue()));
                    CloseableHttpClient postFacilityToSiteHttpClient = HttpClients.createDefault();
                    postFacilityToSiteHttpClient.execute(postFacilityToSite);

                    String facilityNewIdUrl = facilityResponse.getFirstHeader("Location").getValue().substring(facilityResponse.getFirstHeader("Location").getValue().lastIndexOf('/') + 1);
                    facilityMap.put(facility.get("id").textValue(), "http://assets:8080/asset-management/api/facilityInstances/" + facilityNewIdUrl);
                } else {
                    facilityMap.put(facility.get("id").toString(), "http://assets:8080/asset-management/api/facilityInstances/" + facility.get("id").toString());
                    String facilityUrl = baseUri + "/asset-management/api/facilityInstances/" + facility.get("id").toString().replace("\"", "");
                    StringEntity installationInputData = new StringEntity("{\"params\":" + facility.get("params").toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");

                    HttpPatch facilityHttpPatch = new HttpPatch(new URI(facilityUrl));
                    facilityHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                    facilityHttpPatch.setEntity(installationInputData);

                    CloseableHttpClient facilityHttpClient = HttpClients.createDefault();
                    facilityHttpClient.execute(facilityHttpPatch);
                }
            }
            for (JsonNode cabinet : fillSite.get("cabinets")) {
                if (cabinet.get("id").textValue() != null && cabinet.get("id").textValue().startsWith("_NEW")) {
                    HttpResponse equipmentResponse = postEquipment(cabinet, "CABINET");

                    System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();
                    cabinetsMap.put(cabinet.get("id").textValue(), equipmentInstanceUrl);

                    String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(cabinet.get("facilityId").toString().replaceAll("\"", ""));
                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                    CloseableHttpResponse installationResponse = postInstallation(cabinet, "CABINET", siteUrl, facilityUrl, equipmentInstanceUrl);
                    System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                } else {
                    if (cabinet.get("action") != null && cabinet.get("action").textValue().equals("modify")) {
                        CloseableHttpResponse patchEquipmentResponse = patchEquipment(cabinet);
                        System.out.println("equipmentResponse: " + patchEquipmentResponse.getStatusLine().getStatusCode());

                        CloseableHttpResponse patchInstallationResponse = patchInstallation(cabinet);
                        System.out.println("installationResponse: " + patchInstallationResponse.getStatusLine().getStatusCode());
                    } else if (cabinet.get("action") != null && cabinet.get("action").textValue().equals("replace")) {
                        CloseableHttpResponse deleteInstallationResponse = deleteInstallation(cabinet);
                        System.out.println("installationDeleteResponse: " + deleteInstallationResponse.getStatusLine().getStatusCode());

                        HttpResponse equipmentResponse = postEquipment(cabinet, "CABINET");
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();
                        cabinetsMap.put(cabinet.get("id").textValue(), equipmentInstanceUrl);

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(cabinet.get("facilityId").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        postInstallation(cabinet, "CABINET", siteUrl, facilityUrl, equipmentInstanceUrl);
                    } else if (cabinet.get("action") != null && cabinet.get("action").textValue().equals("dismantle")) {
                        deleteInstallation(cabinet);
                    }
                }
            }
            for (JsonNode powerSource : fillSite.get("powerSources")) {
                if (powerSource.get("id") == null) {
                    String powerSourceUrl = baseUri + "/asset-management/api/powerSources";
                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                    StringEntity powerSourceInputData = new StringEntity("{\"params\":" + powerSource.get("params").toString() + ", \"site\": \"" + siteUrl + "\"}", "UTF-8");
                    System.out.println("[PowerSource:" + powerSourceInputData + "]");
                    System.out.println("{\"params\":" + powerSource.get("params").toString() + ", \"site\": \"" + siteUrl + "\"}");

                    HttpPost facilityHttpPost = new HttpPost(new URI(powerSourceUrl));
                    facilityHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                    facilityHttpPost.setEntity(powerSourceInputData);

                    CloseableHttpClient powerSourceHttpClient = HttpClients.createDefault();
                    CloseableHttpResponse powerSourceResponse = powerSourceHttpClient.execute(facilityHttpPost);
                    System.out.println("powerSourceResponse: " + powerSourceResponse.getStatusLine().getStatusCode());
                } else {
                    if (powerSource.get("action") != null && powerSource.get("action").textValue().equals("modify")) {
                        String powerSourceUrl = baseUri + "/asset-management/api/powerSources/" + powerSource.get("id").toString().replace("\"", "");
                        StringEntity powerSourceInputData = new StringEntity("{\"params\":" + powerSource.get("params").toString() + "}", "UTF-8");
                        System.out.println("[PowerSource:" + powerSourceInputData + "]");
                        HttpPatch facilityHttpPatch = new HttpPatch(new URI(powerSourceUrl));
                        facilityHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                        facilityHttpPatch.setEntity(powerSourceInputData);

                        CloseableHttpClient powerSourceHttpClient = HttpClients.createDefault();
                        CloseableHttpResponse powerSourceResponse = powerSourceHttpClient.execute(facilityHttpPatch);
                        System.out.println("powerSourceResponse: " + powerSourceResponse.getStatusLine().getStatusCode());
                    } else if (powerSource.get("action") != null && powerSource.get("action").textValue().equals("replace")) {
                        String powerDeleteUrl = baseUri + "/asset-management/api/powerSources/" + powerSource.get("id").toString().replace("\"", "");
                        HttpDelete httpDelete = new HttpDelete(new URI(powerDeleteUrl));
                        CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                        deleteInstallationHttpClient.execute(httpDelete);

                        String powerSourceUrl = "http://assets:8080/asset-management/api/powerSources";
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        StringEntity powerSourceInputData = new StringEntity("{\"params\":" + powerSource.get("params").toString() + ", \"site\": \"" + siteUrl + "\"}", "UTF-8");
                        System.out.println("[PowerSource:" + powerSourceInputData + "]");
                        System.out.println("{\"params\":" + powerSource.get("params").toString() + ", \"site\": \"" + siteUrl + "\"}");
                        HttpPost facilityHttpPost = new HttpPost(new URI(powerSourceUrl));
                        facilityHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                        facilityHttpPost.setEntity(powerSourceInputData);

                        CloseableHttpClient powerSourceHttpClient = HttpClients.createDefault();
                        CloseableHttpResponse powerSourceResponse = powerSourceHttpClient.execute(facilityHttpPost);
                        System.out.println("powerSourceResponse: " + powerSourceResponse.getStatusLine().getStatusCode());
                    } else if (powerSource.get("action") != null && powerSource.get("action").textValue().equals("dismantle")) {
                        String powerDeleteUrl = baseUri + "/asset-management/api/powerSources/" + powerSource.get("id").toString().replace("\"", "");
                        HttpDelete httpDelete = new HttpDelete(new URI(powerDeleteUrl));
                        CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                        deleteInstallationHttpClient.execute(httpDelete);
                    }
                }
            }
            for (Map.Entry<String, String> s : cabinetsMap.entrySet()) {
                System.out.println(s.getKey() + " " + s.getValue());
            }
            for (JsonNode ru : fillSite.get("rus")) {
                System.out.println("RU: " + ru.toString());
                if (ru.get("id") == null) {
                    HttpResponse equipmentResponse = postEquipment(ru, "RU");

                    System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                    String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(ru.get("facilityId").toString().replaceAll("\"", ""));
                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                    String cabinetId = ru.get("params").get("rbs_number").toString().replaceAll("\"", "");
                    ObjectNode params = (ObjectNode) ru.get("params");
                    params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                    ((ObjectNode) ru).replace("params", params);

                    CloseableHttpResponse installationResponse = postInstallation(ru, "RU", siteUrl, facilityUrl, equipmentInstanceUrl);
                    System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                } else {
                    if (ru.get("action") != null && ru.get("action").textValue().equals("modify")) {
                        CloseableHttpResponse patchEquipmentResponse = patchEquipment(ru);
                        System.out.println("equipmentResponse: " + patchEquipmentResponse.getStatusLine().getStatusCode());

                        String cabinetId = ru.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) ru.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) ru).replace("params", params);

                        CloseableHttpResponse patchInstallationResponse = patchInstallation(ru);
                        System.out.println("installationResponse: " + patchInstallationResponse.getStatusLine().getStatusCode());
                    } else if (ru.get("action") != null && ru.get("action").textValue().equals("replace")) {
                        CloseableHttpResponse deleteInstallationResponse = deleteInstallation(ru);
                        System.out.println("installationDeleteResponse: " + deleteInstallationResponse.getStatusLine().getStatusCode());

                        HttpResponse equipmentResponse = postEquipment(ru, "RU");
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String cabinetId = ru.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) ru.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) ru).replace("params", params);

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(ru.get("facilityId").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        postInstallation(ru, "RU", siteUrl, facilityUrl, equipmentInstanceUrl);
                    } else if (ru.get("action") != null && ru.get("action").textValue().equals("dismantle")) {
                        deleteInstallation(ru);
                    }
                }
            }
            for (JsonNode du : fillSite.get("dus")) {
                if (du.get("id") == null) {
                    HttpResponse equipmentResponse = postEquipment(du, "DU");

                    System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                    String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(du.get("facilityId").toString().replaceAll("\"", ""));
                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                    String cabinetId = du.get("params").get("rbs_number").toString().replaceAll("\"", "");
                    ObjectNode params = (ObjectNode) du.get("params");
                    params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                    ((ObjectNode) du).replace("params", params);

                    CloseableHttpResponse installationResponse = postInstallation(du, "DU", siteUrl, facilityUrl, equipmentInstanceUrl);
                    System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                } else {
                    if (du.get("action") != null && du.get("action").textValue().equals("modify")) {
                        CloseableHttpResponse patchEquipmentResponse = patchEquipment(du);
                        System.out.println("equipmentResponse: " + patchEquipmentResponse.getStatusLine().getStatusCode());

                        String cabinetId = du.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) du.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) du).replace("params", params);

                        CloseableHttpResponse patchInstallationResponse = patchInstallation(du);
                        System.out.println("installationResponse: " + patchInstallationResponse.getStatusLine().getStatusCode());
                    } else if (du.get("action") != null && du.get("action").textValue().equals("replace")) {
                        CloseableHttpResponse deleteInstallationResponse = deleteInstallation(du);
                        System.out.println("installationDeleteResponse: " + deleteInstallationResponse.getStatusLine().getStatusCode());

                        HttpResponse equipmentResponse = postEquipment(du, "DU");
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String cabinetId = du.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) du.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) du).replace("params", params);

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(du.get("facilityId").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        postInstallation(du, "DU", siteUrl, facilityUrl, equipmentInstanceUrl);
                    } else if (du.get("action") != null && du.get("action").textValue().equals("dismantle")) {
                        deleteInstallation(du);
                    }
                }
            }
            for (JsonNode supplementary : fillSite.get("supplementary")) {
                if (supplementary.get("id") == null) {
                    HttpResponse equipmentResponse = postEquipment(supplementary, "SUPPLEMENTARY");

                    System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                    String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(supplementary.get("facilityId").toString().replaceAll("\"", ""));
                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                    CloseableHttpResponse installationResponse = postInstallation(supplementary, "SUPPLEMENTARY", siteUrl, facilityUrl, equipmentInstanceUrl);
                    System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                } else {
                    if (supplementary.get("action") != null && supplementary.get("action").textValue().equals("modify")) {
                        CloseableHttpResponse patchEquipmentResponse = patchEquipment(supplementary);
                        System.out.println("equipmentResponse: " + patchEquipmentResponse.getStatusLine().getStatusCode());

                        CloseableHttpResponse patchInstallationResponse = patchInstallation(supplementary);
                        System.out.println("installationResponse: " + patchInstallationResponse.getStatusLine().getStatusCode());
                    } else if (supplementary.get("action") != null && supplementary.get("action").textValue().equals("replace")) {
                        CloseableHttpResponse deleteInstallationResponse = deleteInstallation(supplementary);
                        System.out.println("installationDeleteResponse: " + deleteInstallationResponse.getStatusLine().getStatusCode());

                        HttpResponse equipmentResponse = postEquipment(supplementary, "SUPPLEMENTARY");
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(supplementary.get("facilityId").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        postInstallation(supplementary, "SUPPLEMENTARY", siteUrl, facilityUrl, equipmentInstanceUrl);
                    } else if (supplementary.get("action") != null && supplementary.get("action").textValue().equals("dismantle")) {
                        deleteInstallation(supplementary);
                    }
                }
            }
            for (JsonNode antenna : fillSite.get("antennas")) {
                if (antenna.get("id") == null) {
                    HttpResponse equipmentResponse = postEquipment(antenna, "ANTENNA");

                    System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                    String cabinetId = antenna.get("params").get("rbs_number").toString().replaceAll("\"", "");
                    ObjectNode params = (ObjectNode) antenna.get("params");
                    params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                    ((ObjectNode) antenna).replace("params", params);

                    String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(antenna.get("facilityId").toString().replaceAll("\"", ""));
                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                    CloseableHttpResponse installationResponse = postInstallation(antenna, "ANTENNA", siteUrl, facilityUrl, equipmentInstanceUrl);
                    System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                } else {
                    if (antenna.get("action") != null && antenna.get("action").textValue().equals("modify")) {
                        CloseableHttpResponse patchEquipmentResponse = patchEquipment(antenna);
                        System.out.println("equipmentResponse: " + patchEquipmentResponse.getStatusLine().getStatusCode());

                        String cabinetId = antenna.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) antenna.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) antenna).replace("params", params);

                        CloseableHttpResponse patchInstallationResponse = patchInstallation(antenna);
                        System.out.println("installationResponse: " + patchInstallationResponse.getStatusLine().getStatusCode());
                    } else if (antenna.get("action") != null && antenna.get("action").textValue().equals("replace")) {
                        CloseableHttpResponse deleteInstallationResponse = deleteInstallation(antenna);
                        System.out.println("installationDeleteResponse: " + deleteInstallationResponse.getStatusLine().getStatusCode());

                        HttpResponse equipmentResponse = postEquipment(antenna, "ANTENNA");
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String cabinetId = antenna.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) antenna.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) antenna).replace("params", params);

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(antenna.get("facilityId").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        postInstallation(antenna, "ANTENNA", siteUrl, facilityUrl, equipmentInstanceUrl);
                    } else if (antenna.get("action") != null && antenna.get("action").textValue().equals("dismantle")) {
                        deleteInstallation(antenna);
                    }
                }
            }
            for (JsonNode transmission : fillSite.get("transmissions")) {
                JsonNode indoor = transmission.get("indoor");
                if(indoor.get("id") == null){

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
