package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import lombok.extern.java.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Log
public class AssetManagementSaveListenerNew implements TaskListener {

    private static String baseUri = "http://assets:8080";

    public AssetManagementSaveListenerNew(String baseUri) {
        this.baseUri = baseUri;
    }

    public AssetManagementSaveListenerNew() {}


    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (delegateTask.getVariableTyped("fillSite") != null && delegateTask.getVariableTyped("fillSite").getValue() != null) {
                //JsonNode fillSite = mapper.readTree(delegateTask.getVariableTyped("fillSite").getValue().toString());
                //saveToAssetManagement(fillSite);
                JsonNode commands = mapper.readTree(delegateTask.getVariableTyped("commands").getValue().toString());
                saveCommandsToAssetManagement(commands, delegateTask.getVariable("site").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        delegateTask.setVariable("performDate", calendar.getTime());
    }

    public void saveCommandsToAssetManagement(JsonNode summaries, String siteId) {
        try {
            StringEntity equipmentInputData = new StringEntity(summaries.toString(), ContentType.APPLICATION_JSON);
            HttpPost equipmentPost = new HttpPost(new URI(baseUri + "/asset-management/command/" + siteId));
            equipmentPost.setEntity(equipmentInputData);
            HttpClient equipmentHttpClient = HttpClients.createDefault();
            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
            EntityUtils.consume(equipmentResponse.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpResponse postEquipment(JsonNode node, String definitionId) {
        try {
            StringEntity equipmentInputData = new StringEntity("{\"params\":" + node.get("params").toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/" + definitionId + "\"}", "UTF-8");
            String equipmentUrl = baseUri + "/asset-management/api/equipmentInstances";
            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            equipmentPost.setEntity(equipmentInputData);
            HttpClient equipmentHttpClient = HttpClients.createDefault();
            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
            EntityUtils.consume(equipmentResponse.getEntity());
            log.warning("====================");
            log.warning(equipmentResponse.toString());
            return equipmentResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse postInstallation(JsonNode node, String definitionId, String siteUrl, String facilityUrl, String equipmentInstanceUrl) {
        try {
            System.out.println("definitionId: " + definitionId);
            System.out.println("siteUrl: " + siteUrl);
            System.out.println("facilityUrl: " + facilityUrl);
            System.out.println("equipmentInstanceUrl: " + equipmentInstanceUrl);
            String installationUrl = baseUri + "/asset-management/api/installationInstances";
            log.warning("{\"params\":" + node.get("params").toString() + ", \"site\":\"" + siteUrl + "\", " + (facilityUrl != null ? "\"facility\":\"" + facilityUrl + "\", " : "") + "\"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"/installationDefinitions/" + definitionId + "\"}");
            StringEntity installationInputData = new StringEntity("{\"params\":" + node.get("params").toString() + ", \"site\":\"" + siteUrl + "\", " + (facilityUrl != null ? "\"facility\":\"" + facilityUrl + "\", " : "") + "\"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"/installationDefinitions/" + definitionId + "\"}", "UTF-8");
            HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
            installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            installationHttpPost.setEntity(installationInputData);

            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
            log.warning("====================");
            log.warning(installationResponse.toString());
            return installationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse postConnection(JsonNode node, String definitionId, List<String> equipments) {
        try {
            String connectionUrl = baseUri + "/asset-management/api/connectionInstances";

            String equipmentUris = equipments.stream().collect(Collectors.joining("\",\"", "[\"", "\"]"));

            log.warning("{\"params\":" + node.get("params").toString() + ", \"definition\":\"/connectionDefinitions/" + definitionId + "\", \"equipments\": " + equipmentUris + "}");
            StringEntity connectionInputData = new StringEntity("{\"params\":" + node.get("params").toString() + ", \"definition\":\"/connectionDefinitions/" + definitionId + "\", \"equipments\": " + equipmentUris + "}", "UTF-8");
            HttpPost connectionHttpPost = new HttpPost(new URI(connectionUrl));
            connectionHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            connectionHttpPost.setEntity(connectionInputData);
            CloseableHttpClient connectionHttpClient = HttpClients.createDefault();
            CloseableHttpResponse connectionResponse = connectionHttpClient.execute(connectionHttpPost);

            String connectionInstanceUrl = connectionResponse.getFirstHeader("Location").getValue();

            HttpPost postFacilityToSite = new HttpPost(new URI(connectionInstanceUrl + "/equipments"));
            postFacilityToSite.addHeader("Content-Type", "text/uri-list;charset=UTF-8");
            postFacilityToSite.setEntity(new StringEntity(equipments.stream().collect(Collectors.joining(","))));
            CloseableHttpClient postFacilityToSiteHttpClient = HttpClients.createDefault();
            postFacilityToSiteHttpClient.execute(postFacilityToSite);

            log.warning("====================");
            log.warning(connectionResponse.toString());
            return connectionResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse patchEquipment(JsonNode node) {
        log.warning(node.toString());
        try {
            StringEntity equipmentInputData = new StringEntity("{\"params\":" + node.get("params").toString() + "}", "UTF-8");
            String equipmentUrl = baseUri + "/asset-management/api/equipmentInstances/" + node.get("id").toString().replaceAll("\"", "");
            HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
            equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
            equipmentHttpPatch.setEntity(equipmentInputData);

            CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
            CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
            log.warning("====================");
            log.warning(equipmentResponse.toString());
            return equipmentResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse patchInstallation(JsonNode node) {
        try {
            StringEntity installationInputData = new StringEntity("{\"params\":" + node.get("params").toString() + "}", "UTF-8");
            String installationUrl = baseUri + "/asset-management/api/installationInstances/" + node.get("id").toString().replaceAll("\"", "");
            System.out.println(installationUrl);
            HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
            installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
            installationHttpPatch.setEntity(installationInputData);

            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
            log.warning("====================");
            log.warning(installationResponse.toString());
            return installationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CloseableHttpResponse deleteInstallation(JsonNode node) {
        try {
            String installationDeleteUrl = baseUri + "/asset-management/api/installationInstances/" + node.get("id").toString().replaceAll("\"", "");
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
            if (fillSite.get("facilities") != null) {
                for (JsonNode facility : fillSite.get("facilities")) {
                    String definitionId = null;
                    if (facility.get("definitionId") != null) {
                        definitionId = facility.get("definitionId").textValue();
                    }
                    String definitionUrl = "/asset-management/api/facilityDefinitions/";
                    if (definitionId != null && !definitionId.isEmpty()) {
                        definitionUrl += definitionId;
                    }
                    if (facility.get("id").toString().replaceAll("\"", "").startsWith("_NEW")) {
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
                        facilityMap.put(facility.get("id").toString().replaceAll("\"", ""), "http://assets:8080/asset-management/api/facilityInstances/" + facilityNewIdUrl);
                    } else {
                        facilityMap.put(facility.get("id").toString().replaceAll("\"", ""), "http://assets:8080/asset-management/api/facilityInstances/" + facility.get("id").toString().replaceAll("\"", ""));
                        String facilityUrl = baseUri + "/asset-management/api/facilityInstances/" + facility.get("id").toString().replaceAll("\"", "");
                        StringEntity installationInputData = new StringEntity("{\"params\":" + facility.get("params").toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");

                        HttpPatch facilityHttpPatch = new HttpPatch(new URI(facilityUrl));
                        facilityHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                        facilityHttpPatch.setEntity(installationInputData);

                        CloseableHttpClient facilityHttpClient = HttpClients.createDefault();
                        facilityHttpClient.execute(facilityHttpPatch);
                    }
                }
            }
            System.out.println("============================");
            System.out.println("Facilities");
            for (Map.Entry<String, String> e : facilityMap.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
            if (fillSite.get("cabinets") != null) {
                for (JsonNode cabinet : fillSite.get("cabinets")) {
                    if (cabinet.get("id").toString().replaceAll("\"", "").startsWith("_NEW")) {
                        HttpResponse equipmentResponse = postEquipment(cabinet.get("equipment"), "CABINET");

                        System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();
                        cabinetsMap.put(cabinet.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(cabinet.get("facility").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        CloseableHttpResponse installationResponse = postInstallation(cabinet, "CABINET", siteUrl, facilityUrl, equipmentInstanceUrl);
                        System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                    } else {
                        if (cabinet.get("action") != null && cabinet.get("action").textValue().equals("modify")) {
                            CloseableHttpResponse patchEquipmentResponse = patchEquipment(cabinet.get("equipment"));
                            System.out.println("equipmentResponse: " + patchEquipmentResponse.getStatusLine().getStatusCode());

                            String equipmentInstanceUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + cabinet.get("id").toString().replaceAll("\"", "");
                            cabinetsMap.put(cabinet.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);

                            CloseableHttpResponse patchInstallationResponse = patchInstallation(cabinet);
                            System.out.println("installationResponse: " + patchInstallationResponse.getStatusLine().getStatusCode());
                        } else if (cabinet.get("action") != null && cabinet.get("action").textValue().equals("replace")) {
                            CloseableHttpResponse deleteInstallationResponse = deleteInstallation(cabinet);
                            System.out.println("installationDeleteResponse: " + deleteInstallationResponse.getStatusLine().getStatusCode());

                            postEquipment(cabinet.get("equipment"), "CABINET");
                            String equipmentInstanceUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + cabinet.get("id").toString().replaceAll("\"", "");
                            cabinetsMap.put(cabinet.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);

                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(cabinet.get("facility").toString().replaceAll("\"", ""));
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            postInstallation(cabinet, "CABINET", siteUrl, facilityUrl, equipmentInstanceUrl);
                        } else if (cabinet.get("action") != null && cabinet.get("action").textValue().equals("dismantle")) {
                            deleteInstallation(cabinet);
                        } else {
                            String equipmentInstanceUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + cabinet.get("id").toString().replaceAll("\"", "");
                            cabinetsMap.put(cabinet.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);
                        }
                    }
                }
            }
            System.out.println("============================");
            System.out.println("Cabinets");
            for (Map.Entry<String, String> e : cabinetsMap.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
            if (fillSite.get("powerSources") != null) {
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
                            String powerSourceUrl = baseUri + "/asset-management/api/powerSources/" + powerSource.get("id").toString().replaceAll("\"", "");
                            StringEntity powerSourceInputData = new StringEntity("{\"params\":" + powerSource.get("params").toString() + "}", "UTF-8");
                            System.out.println("[PowerSource:" + powerSourceInputData + "]");
                            HttpPatch facilityHttpPatch = new HttpPatch(new URI(powerSourceUrl));
                            facilityHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                            facilityHttpPatch.setEntity(powerSourceInputData);

                            CloseableHttpClient powerSourceHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse powerSourceResponse = powerSourceHttpClient.execute(facilityHttpPatch);
                            System.out.println("powerSourceResponse: " + powerSourceResponse.getStatusLine().getStatusCode());
                        } else if (powerSource.get("action") != null && powerSource.get("action").textValue().equals("replace")) {
                            String powerDeleteUrl = baseUri + "/asset-management/api/powerSources/" + powerSource.get("id").toString().replaceAll("\"", "");
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
                            String powerDeleteUrl = baseUri + "/asset-management/api/powerSources/" + powerSource.get("id").toString().replaceAll("\"", "");
                            HttpDelete httpDelete = new HttpDelete(new URI(powerDeleteUrl));
                            CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                            deleteInstallationHttpClient.execute(httpDelete);
                        }
                    }
                }
            }
            for (Map.Entry<String, String> s : cabinetsMap.entrySet()) {
                System.out.println(s.getKey() + " " + s.getValue());
            }
            if (fillSite.get("rus") != null) {
                for (JsonNode ru : fillSite.get("rus")) {
                    System.out.println("RU: " + ru.toString());
                    if (ru.get("id") == null) {
                        HttpResponse equipmentResponse = postEquipment(ru.get("equipment"), "RU");

                        System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(ru.get("facility").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        String cabinetId = ru.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) ru.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) ru).replace("params", params);

                        CloseableHttpResponse installationResponse = postInstallation(ru, "RU", siteUrl, facilityUrl, equipmentInstanceUrl);
                        System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());

                        List<String> equipmentUris = Arrays.asList(equipmentInstanceUrl, cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)));
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode connectionNode = mapper.createObjectNode();
                        ObjectNode connectionParams = mapper.createObjectNode();
                        connectionNode.set("params", connectionParams);
                        postConnection(connectionNode, "RU2RBS", equipmentUris);
                    } else {
                        if (ru.get("action") != null && ru.get("action").textValue().equals("modify")) {
                            CloseableHttpResponse patchEquipmentResponse = patchEquipment(ru.get("equipment"));
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

                            HttpResponse equipmentResponse = postEquipment(ru.get("equipment"), "RU");
                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String cabinetId = ru.get("params").get("rbs_number").toString().replaceAll("\"", "");
                            ObjectNode params = (ObjectNode) ru.get("params");
                            params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                            ((ObjectNode) ru).replace("params", params);

                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(ru.get("facility").toString().replaceAll("\"", ""));
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            postInstallation(ru, "RU", siteUrl, facilityUrl, equipmentInstanceUrl);
                        } else if (ru.get("action") != null && ru.get("action").textValue().equals("dismantle")) {
                            deleteInstallation(ru);
                        }
                    }
                }
            }
            if (fillSite.get("dus") != null) {
                for (JsonNode du : fillSite.get("dus")) {
                    if (du.get("id") == null) {
                        HttpResponse equipmentResponse = postEquipment(du.get("equipment"), "DU");

                        System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(du.get("facility").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        String cabinetId = du.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) du.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) du).replace("params", params);

                        CloseableHttpResponse installationResponse = postInstallation(du, "DU", siteUrl, facilityUrl, equipmentInstanceUrl);
                        System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                    } else {
                        if (du.get("action") != null && du.get("action").textValue().equals("modify")) {
                            CloseableHttpResponse patchEquipmentResponse = patchEquipment(du.get("equipment"));
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

                            HttpResponse equipmentResponse = postEquipment(du.get("equipment"), "DU");
                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String cabinetId = du.get("params").get("rbs_number").toString().replaceAll("\"", "");
                            ObjectNode params = (ObjectNode) du.get("params");
                            params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                            ((ObjectNode) du).replace("params", params);

                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(du.get("facility").toString().replaceAll("\"", ""));
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            postInstallation(du, "DU", siteUrl, facilityUrl, equipmentInstanceUrl);
                        } else if (du.get("action") != null && du.get("action").textValue().equals("dismantle")) {
                            deleteInstallation(du);
                        }
                    }
                }
            }
            if (fillSite.get("supplementary") != null) {
                for (JsonNode supplementary : fillSite.get("supplementary")) {
                    if (supplementary.get("id") == null) {
                        HttpResponse equipmentResponse = postEquipment(supplementary.get("equipment"), "SUPPLEMENTARY");

                        System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(supplementary.get("facility").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        CloseableHttpResponse installationResponse = postInstallation(supplementary, "SUPPLEMENTARY", siteUrl, facilityUrl, equipmentInstanceUrl);
                        System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                    } else {
                        if (supplementary.get("action") != null && supplementary.get("action").textValue().equals("modify")) {
                            CloseableHttpResponse patchEquipmentResponse = patchEquipment(supplementary.get("equipment"));
                            System.out.println("equipmentResponse: " + patchEquipmentResponse.getStatusLine().getStatusCode());

                            CloseableHttpResponse patchInstallationResponse = patchInstallation(supplementary);
                            System.out.println("installationResponse: " + patchInstallationResponse.getStatusLine().getStatusCode());
                        } else if (supplementary.get("action") != null && supplementary.get("action").textValue().equals("replace")) {
                            CloseableHttpResponse deleteInstallationResponse = deleteInstallation(supplementary);
                            System.out.println("installationDeleteResponse: " + deleteInstallationResponse.getStatusLine().getStatusCode());

                            HttpResponse equipmentResponse = postEquipment(supplementary.get("equipment"), "SUPPLEMENTARY");
                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(supplementary.get("facility").toString().replaceAll("\"", ""));
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            postInstallation(supplementary, "SUPPLEMENTARY", siteUrl, facilityUrl, equipmentInstanceUrl);
                        } else if (supplementary.get("action") != null && supplementary.get("action").textValue().equals("dismantle")) {
                            deleteInstallation(supplementary);
                        }
                    }
                }
            }
            if (fillSite.get("antennas") != null) {
                for (JsonNode antenna : fillSite.get("antennas")) {
                    if (antenna.get("id") == null) {
                        HttpResponse equipmentResponse = postEquipment(antenna.get("equipment"), "ANTENNA");

                        System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());
                        String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                        String cabinetId = antenna.get("params").get("rbs_number").toString().replaceAll("\"", "");
                        ObjectNode params = (ObjectNode) antenna.get("params");
                        params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                        ((ObjectNode) antenna).replace("params", params);

                        String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(antenna.get("facility").toString().replaceAll("\"", ""));
                        String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                        CloseableHttpResponse installationResponse = postInstallation(antenna, "ANTENNA", siteUrl, facilityUrl, equipmentInstanceUrl);
                        System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                    } else {
                        if (antenna.get("action") != null && antenna.get("action").textValue().equals("modify")) {
                            CloseableHttpResponse patchEquipmentResponse = patchEquipment(antenna.get("equipment"));
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

                            HttpResponse equipmentResponse = postEquipment(antenna.get("equipment"), "ANTENNA");
                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String cabinetId = antenna.get("params").get("rbs_number").toString().replaceAll("\"", "");
                            ObjectNode params = (ObjectNode) antenna.get("params");
                            params.put("rbs_number", cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).substring(cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)).lastIndexOf('/') + 1));
                            ((ObjectNode) antenna).replace("params", params);

                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(antenna.get("facility").toString().replaceAll("\"", ""));
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            postInstallation(antenna, "ANTENNA", siteUrl, facilityUrl, equipmentInstanceUrl);
                        } else if (antenna.get("action") != null && antenna.get("action").textValue().equals("dismantle")) {
                            deleteInstallation(antenna);
                        }
                    }
                }
            }
            Map<String, String> indoorUnits = new HashMap<String, String>();
            Map<String, String> outdoorUnits = new HashMap<String, String>();
            Map<String, String> antennaUnits = new HashMap<String, String>();
            for (JsonNode iu : fillSite.get("ius")) {
                System.out.println(iu.get("id").toString().replaceAll("\"", ""));
                if (iu.get("id").toString().replaceAll("\"", "").startsWith("_NEW")) {
                    HttpResponse equipmentResponse = postEquipment(iu, "IU");
                    log.warning(equipmentResponse.getFirstHeader("Location").getValue());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();
                    indoorUnits.put(iu.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);

                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode installation = mapper.createObjectNode();
                    ObjectNode params = mapper.createObjectNode();
                    installation.set("params", params);
                    postInstallation(installation, "IU", siteUrl, null, equipmentInstanceUrl);
                } else {
                    patchEquipment(iu);
                    String equipmentInstanceUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + iu.get("id").toString().replaceAll("\"", "");
                    indoorUnits.put(iu.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);
                }
            }
            for (JsonNode ou : fillSite.get("ous")) {
                System.out.println(ou.get("id").toString().replaceAll("\"", ""));
                if (ou.get("id").toString().replaceAll("\"", "").startsWith("_NEW")) {
                    HttpResponse equipmentResponse = postEquipment(ou, "OU");
                    log.warning(equipmentResponse.getFirstHeader("Location").getValue());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();
                    outdoorUnits.put(ou.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);

                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode installation = mapper.createObjectNode();
                    ObjectNode params = mapper.createObjectNode();
                    installation.set("params", params);
                    postInstallation(installation, "OU", siteUrl, null, equipmentInstanceUrl);
                } else {
                    patchEquipment(ou);
                    String equipmentInstanceUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + ou.get("id").toString().replaceAll("\"", "");
                    outdoorUnits.put(ou.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);
                }
            }
            for (JsonNode au : fillSite.get("aus")) {
                System.out.println(au.get("id").toString().replaceAll("\"", ""));
                if (au.get("id").toString().replaceAll("\"", "").startsWith("_NEW")) {
                    HttpResponse equipmentResponse = postEquipment(au, "AU");
                    log.warning(equipmentResponse.getFirstHeader("Location").getValue());
                    String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();
                    antennaUnits.put(au.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);

                    String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode installation = mapper.createObjectNode();
                    ObjectNode params = mapper.createObjectNode();
                    installation.set("params", params);
                    postInstallation(installation, "AU", siteUrl, null, equipmentInstanceUrl);
                } else {
                    patchEquipment(au);
                    String equipmentInstanceUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + au.get("id").toString().replaceAll("\"", "");
                    antennaUnits.put(au.get("id").toString().replaceAll("\"", ""), equipmentInstanceUrl);
                }
            }
            for (Map.Entry<String, String> e : indoorUnits.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
            for (Map.Entry<String, String> e : outdoorUnits.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
            for (Map.Entry<String, String> e : antennaUnits.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
            for (Map.Entry<String, String> e : facilityMap.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
            for (Map.Entry<String, String> e : cabinetsMap.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
            if (fillSite.get("transmissions") != null) {
                for (JsonNode transmission : fillSite.get("transmissions")) {
                    if (transmission.get("indoor") != null) {
                        JsonNode nearEndIndoor = transmission.get("indoor").get("nearEnd");
                        JsonNode farEndIndoor = transmission.get("indoor").get("farEnd");

                        log.warning("START INDOOR");
                        if (nearEndIndoor.get("id") == null) {
                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(nearEndIndoor.get("facility").toString().replaceAll("\"", ""));
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + transmission.get("nearEnd").get("site").textValue().replaceAll("\"", "");
                            String equipmentInstanceUrl = indoorUnits.get(nearEndIndoor.get("equipmentId").toString().replaceAll("\"", ""));
                            CloseableHttpResponse postInstallationResponse = postInstallation(nearEndIndoor, "IU", siteUrl, facilityUrl, equipmentInstanceUrl);
                            //String nearEndInstallationInstanceUrl = postInstallationResponse.getFirstHeader("Location").getValue();

                            //farEndIndoor.get("equipment").get("id").t

//                            List<String> equipmentUris = Arrays.asList(equipmentInstanceUrl, cabinetsMap.get(cabinetId.substring(cabinetId.lastIndexOf('/') + 1)));
//                            ObjectMapper mapper = new ObjectMapper();
//                            ObjectNode connectionNode = mapper.createObjectNode();
//                            ObjectNode connectionParams = mapper.createObjectNode();
//                            connectionNode.set("params", connectionParams);
//                            postConnection(connectionNode, "RU2RBS", equipmentUris);
                        } else {
                            CloseableHttpResponse patchInstallationResponse = patchInstallation(nearEndIndoor);
                        }
                    }
                    if (transmission.get("ous") != null) {
                        log.warning("START OUTDOOR");
                        for (JsonNode outdoorUnit : transmission.get("ous")) {
                            JsonNode ou = outdoorUnit.get("nearEnd").get("main_line");
                            if (ou.get("id") == null) {
                                String facilityUrl = facilityMap.get(ou.get("facility").toString().replaceAll("\"", ""));
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + transmission.get("nearEnd").get("site").textValue().replaceAll("\"", "");
                                String equipmentInstanceUrl = outdoorUnits.get(ou.get("equipmentId").toString().replaceAll("\"", ""));
                                log.warning("EQUIPMENT ID: " + ou.get("equipmentId").toString().replaceAll("\"", ""));
                                log.warning("equipmentInstanceUrl: " + equipmentInstanceUrl);
                                postInstallation(ou, "OU", siteUrl, facilityUrl, equipmentInstanceUrl);
                            } else {
                                patchInstallation(ou);
                            }
                        }
                    }
                    if (transmission.get("aus") != null) {
                        log.warning("START ANTENNA");
                        for (JsonNode outdoorUnit : transmission.get("aus")) {
                            JsonNode au = outdoorUnit.get("nearEnd").get("main_line");
                            if (au.get("id") == null) {
                                String facilityUrl = facilityMap.get(au.get("facility").toString().replaceAll("\"", ""));
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + transmission.get("nearEnd").get("site").textValue().replaceAll("\"", "");
                                String equipmentInstanceUrl = antennaUnits.get(au.get("equipmentId").toString().replaceAll("\"", ""));
                                log.warning("EQUIPMENT ID: " + au.get("equipmentId").toString().replaceAll("\"", ""));
                                log.warning("equipmentInstanceUrl: " + equipmentInstanceUrl);
                                postInstallation(au, "AU", siteUrl, facilityUrl, equipmentInstanceUrl);
                            } else {
                                patchInstallation(au);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
