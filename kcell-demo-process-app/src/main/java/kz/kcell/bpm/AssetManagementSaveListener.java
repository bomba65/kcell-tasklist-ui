package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.Header;
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

public class AssetManagementSaveListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> facilityMap = new HashMap<String, String>();
            List<String> outdoorMap = new ArrayList<String>();
            List<String> indoorMap = new ArrayList<String>();
            List<String> antennasMap = new ArrayList<String>();
            if (delegateTask.getVariableTyped("fillSite") != null && delegateTask.getVariableTyped("fillSite").getValue() != null) {
                JsonNode fillSite = mapper.readTree(delegateTask.getVariableTyped("fillSite").getValue().toString());
                //Facility section
                System.out.println("========== FACILITY =========");
                try {
                    for (JsonNode facility : fillSite.get("facilities")) {
                        String definitionId = null;
                        if (facility.get("definition") != null) {
                            definitionId = facility.get("definition").textValue();
                        }
                        String definitionUrl = "/asset-management/api/facilityDefinitions/";
                        if (definitionId != null && !definitionId.isEmpty()) {
                            definitionUrl += definitionId;
                        }
                        System.out.println(facility.toString());
                        if (facility.get("_facilityId") == null) {
                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances";
                            StringEntity installationInputData = new StringEntity("{\"params\":" + facility.get("object").toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");
                            HttpPost facilityHttpPost = new HttpPost(new URI(facilityUrl));
                            facilityHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            facilityHttpPost.setEntity(installationInputData);
                            HttpClient facilityHttpClient = HttpClients.createDefault();
                            HttpResponse facilityResponse = facilityHttpClient.execute(facilityHttpPost);
                            EntityUtils.consume(facilityResponse.getEntity());

                            HttpPost postFacilityToSite = new HttpPost(new URI("http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "") + "/facilities"));
                            postFacilityToSite.addHeader("Content-Type", "text/uri-list;charset=UTF-8");
                            postFacilityToSite.setEntity(new StringEntity(facilityResponse.getFirstHeader("Location").getValue()));
                            CloseableHttpClient postFacilityToSiteHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse postFacilityToSiteResponse = postFacilityToSiteHttpClient.execute(postFacilityToSite);
                            System.out.println("facilityResponse: " + postFacilityToSiteResponse.getStatusLine().getStatusCode());
                            String facilitynewIdUrl = facilityResponse.getFirstHeader("Location").getValue().substring(facilityResponse.getFirstHeader("Location").getValue().lastIndexOf('/') + 1);
                            facilityMap.put(facility.get("object").get("definition").textValue(), facilitynewIdUrl);
                        } else {
                            facilityMap.put(facility.get("object").get("definition").textValue(), facility.get("_facilityId").textValue().toString().replace("\"", ""));
                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facility.get("_facilityId").toString().replace("\"", "");
                            StringEntity installationInputData = new StringEntity("{\"params\":" + facility.get("object").toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");
                            System.out.println("[FACILITY:" + installationInputData + "]");
                            HttpPatch facilityHttpPatch = new HttpPatch(new URI(facilityUrl));
                            facilityHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                            facilityHttpPatch.setEntity(installationInputData);

                            CloseableHttpClient facilityHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse facilityResponse = facilityHttpClient.execute(facilityHttpPatch);
                            System.out.println("facilityResponse: " + facilityResponse.getStatusLine().getStatusCode());
                        }
                    }
                    for (Map.Entry<String, String> a : facilityMap.entrySet()) {
                        System.out.println(a.getKey() + " " + a.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Indoor units section
                System.out.println("========== Indoor Units =========");
                try {
                    String definitionUrl = "/asset-management/api/equipmentDefinitions/IU";
                    for (JsonNode iu : fillSite.get("transmission").get("indoorUnits")) {
                        String equipmentId = null;
                        if (iu.get("_equipmentId") != null) {
                            equipmentId = iu.get("_equipmentId").textValue();
                        }
                        if (equipmentId != null && !equipmentId.isEmpty()) {
                            String indoorUrl = "http://assets:8080/asset-management/api/equipmentInstance/" + equipmentId;
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + iu.toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");
                            System.out.println("[INDOOR UNIT:" + equipmentInputData + "]");
                            HttpPatch indoorHttpPatch = new HttpPatch(new URI(indoorUrl));
                            indoorHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                            indoorHttpPatch.setEntity(equipmentInputData);

                            CloseableHttpClient indoorHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse indoorResponse = indoorHttpClient.execute(indoorHttpPatch);
                            System.out.println("Indoor Unit : " + indoorResponse.getStatusLine().getStatusCode());
                            indoorMap.add("/asset-management/api/equipmentInstance/" + equipmentId);
                        } else {
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + iu.toString() + ", \"definition\":\"" + definitionUrl + "\"}", "UTF-8");
                            System.out.println("[INDOOR UNIT:" + equipmentInputData + "]");
                            String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            equipmentPost.setEntity(equipmentInputData);
                            HttpClient equipmentHttpClient = HttpClients.createDefault();
                            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                            EntityUtils.consume(equipmentResponse.getEntity());
                            String newEquipmentUrl = equipmentResponse.getFirstHeader("Location").getValue();
                            indoorMap.add(newEquipmentUrl);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Outdoor units section
                System.out.println("========== Outdoor Units =========");
                try {
                    String definitionUrl = "/asset-management/api/equipmentDefinitions/OU";
                    for (JsonNode ou : fillSite.get("transmission").get("outdoorUnits")) {
                        String equipmentId = null;
                        if (ou.get("_equipmentId") != null) {
                            equipmentId = ou.get("_equipmentId").textValue();
                        }
                        if (equipmentId != null && !equipmentId.isEmpty()) {
                            String indoorUrl = "http://assets:8080/asset-management/api/equipmentInstance/" + equipmentId;
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + ou.toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");
                            System.out.println("[OUTDOOR UNIT:" + equipmentInputData + "]");
                            HttpPatch indoorHttpPatch = new HttpPatch(new URI(indoorUrl));
                            indoorHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                            indoorHttpPatch.setEntity(equipmentInputData);

                            CloseableHttpClient indoorHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse indoorResponse = indoorHttpClient.execute(indoorHttpPatch);
                            System.out.println("Indoor Unit : " + indoorResponse.getStatusLine().getStatusCode());
                            outdoorMap.add("/asset-management/api/equipmentInstance/" + equipmentId);
                        } else {
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + ou.toString() + ", \"definition\":\"" + definitionUrl + "\"}", "UTF-8");
                            System.out.println("[OUTDOOR UNIT:" + equipmentInputData + "]");
                            String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            equipmentPost.setEntity(equipmentInputData);
                            HttpClient equipmentHttpClient = HttpClients.createDefault();
                            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                            EntityUtils.consume(equipmentResponse.getEntity());
                            String newEquipmentUrl = equipmentResponse.getFirstHeader("Location").getValue();
                            outdoorMap.add(newEquipmentUrl);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Antenna units section
                System.out.println("========== Antenna Units =========");
                try {
                    String definitionUrl = "/asset-management/api/equipmentDefinitions/AU";
                    for (JsonNode au : fillSite.get("transmission").get("antennas")) {
                        String equipmentId = null;
                        if (au.get("_equipmentId") != null) {
                            equipmentId = au.get("_equipmentId").textValue();
                        }
                        if (equipmentId != null && !equipmentId.isEmpty()) {
                            String antennaUrl = "http://assets:8080/asset-management/api/equipmentInstance/" + equipmentId;
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + au.toString() + ", \"definition\": \"" + definitionUrl + "\"}", "UTF-8");
                            System.out.println("[ANTENNA UNIT:" + equipmentInputData + "]");
                            HttpPatch indoorHttpPatch = new HttpPatch(new URI(antennaUrl));
                            indoorHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                            indoorHttpPatch.setEntity(equipmentInputData);

                            CloseableHttpClient indoorHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse indoorResponse = indoorHttpClient.execute(indoorHttpPatch);
                            System.out.println("Indoor Unit : " + indoorResponse.getStatusLine().getStatusCode());
                            antennasMap.add("/asset-management/api/equipmentInstance/" + equipmentId);
                        } else {
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + au.toString() + ", \"definition\":\"" + definitionUrl + "\"}", "UTF-8");
                            System.out.println("[ANTENNA UNIT:" + equipmentInputData + "]");
                            String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            equipmentPost.setEntity(equipmentInputData);
                            HttpClient equipmentHttpClient = HttpClients.createDefault();
                            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                            EntityUtils.consume(equipmentResponse.getEntity());
                            String newEquipmentUrl = equipmentResponse.getFirstHeader("Location").getValue();
                            antennasMap.add(newEquipmentUrl);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //HOPS units section
                System.out.println("========== HOPS =========");
                try {
                    for (JsonNode hop : fillSite.get("transmissions")) {
                        //Outdoors
                        System.out.println("\t========== Outdoors =========");
                        JsonNode outDoors = hop.get("nearEnd").get("outdoors");
                        for (JsonNode nearEndOutdoor : outDoors) {
                            JsonNode outdoor = nearEndOutdoor.get("nearEnd");
                            String installationId = null;
                            if (outdoor.get("_installationId") != null) {
                                installationId = outdoor.get("_installationId").asText();
                            }
                            ObjectNode node = (ObjectNode) outdoor;
                            node.remove("_installationId");
                            if (installationId != null && !installationId.isEmpty()) {
                                String outdoorUrl = "http://assets:8080/asset-management/api/installationInstance/" + installationId;
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(node.get("location").textValue());
                                String equipmentUrl = outdoorMap.get(Integer.parseInt(outdoor.get("main_line").get("antenna_unit_index").textValue()));

                                StringEntity installationInputData = new StringEntity("{\"params\":" + node.toString() + ", \"equipment\":\"" + equipmentUrl + "\", \"facility\":\"" + facilityUrl + "\"}", "UTF-8");
                                System.out.println("{\"params\":" + node.toString() + ", \"equipment\":\"" + equipmentUrl + "\", \"facility\":\"" + facilityUrl + "\"}");
                                System.out.println("[HOP OUTDOOR UNIT:" + installationInputData + "]");
                                HttpPatch outdoorHttpPatch = new HttpPatch(new URI(outdoorUrl));
                                outdoorHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                outdoorHttpPatch.setEntity(installationInputData);

                                CloseableHttpClient outdoorHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse outdoorResponse = outdoorHttpClient.execute(outdoorHttpPatch);
                                System.out.println("HOP Outdoor Unit : " + outdoorResponse.getStatusLine().getStatusCode());
                            } else {
                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(node.get("location").textValue());
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");
                                String equipmentUrl = outdoorMap.get(Integer.parseInt(outdoor.get("main_line").get("outdoor_unit_index").textValue()));

                                StringEntity installationInputData = new StringEntity("{\"params\":" + node.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/OU\"}", "UTF-8");
                                System.out.println("{\"params\":" + node.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/OU\"}");
                                System.out.println("[HOP OUTDOOR UNIT:" + installationInputData + "]");
                                System.out.println(installationUrl);
                                HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                                installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPost.setEntity(installationInputData);
                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            }
                        }
                        //Antennas
                        System.out.println("\t========== Antennas =========");
                        JsonNode antennas = hop.get("nearEnd").get("antennas");
                        for (JsonNode nearEndAntennas : antennas) {
                            JsonNode antenna = nearEndAntennas.get("nearEnd");
                            String installationId = null;
                            if (antenna.get("_installationId") != null) {
                                installationId = antenna.get("_installationId").asText();
                            }
                            ObjectNode node = (ObjectNode) antenna;
                            node.remove("_installationId");
                            if (installationId != null && !installationId.isEmpty()) {
                                String antennaUrl = "http://assets:8080/asset-management/api/installationInstance/" + installationId;
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(node.get("location").textValue());
                                String equipmentUrl = antennasMap.get(Integer.parseInt(antenna.get("main_line").get("antenna_unit_index").textValue()));
                                StringEntity installationInputData = new StringEntity("{\"params\":" + node.toString() + ", \"equipment\":\"" + equipmentUrl + "\", \"facility\":\"" + facilityUrl + "\"}", "UTF-8");
                                System.out.println("[HOP ANTENNA UNIT:" + installationInputData + "]");
                                HttpPatch outdoorHttpPatch = new HttpPatch(new URI(antennaUrl));
                                outdoorHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                outdoorHttpPatch.setEntity(installationInputData);

                                CloseableHttpClient antennaUrlHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse antennaResponse = antennaUrlHttpClient.execute(outdoorHttpPatch);
                                System.out.println("HOP ANTENNA Unit : " + antennaResponse.getStatusLine().getStatusCode());
                            } else {
                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(node.get("location").textValue());
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");
                                String equipmentUrl = antennasMap.get(Integer.parseInt(antenna.get("main_line").get("antenna_unit_index").textValue()));
                                System.out.println("{\"params\":" + node.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/AU\"}");
                                StringEntity installationInputData = new StringEntity("{\"params\":" + node.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/AU\"}", "UTF-8");
                                System.out.println(installationUrl);
                                HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                                installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPost.setEntity(installationInputData);
                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
/*
                //MicroWave section
                System.out.println("========== MICROWAVE =========");
                try {
                    JsonNode equipment = fillSite.get("microWave").get("equipment");
                    JsonNode installation = fillSite.get("microWave").get("installation");
                    StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}", "UTF-8");
                    String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + fillSite.get("microWave").get("installation").get("id").toString().replace("\"", "");
                    System.out.println(installationUrl);
                    HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                    installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                    installationHttpPatch.setEntity(installationInputData);

                    CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                    CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                    System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());

                    StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}", "UTF-8");
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
                */
                //Cabinets sections
                System.out.println("========== CABINETS =========");
                try {
                    for (JsonNode cabinet : fillSite.get("cabinets")) {
                        JsonNode equipment = cabinet.get("equipment").get("object");
                        JsonNode installation = cabinet.get("installation").get("object");
                        if (cabinet.get("_equipmentId") == null || cabinet.get("_installationId") == null) {
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/CABINET\"}", "UTF-8");
                            String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            equipmentPost.setEntity(equipmentInputData);
                            HttpClient equipmentHttpClient = HttpClients.createDefault();
                            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                            EntityUtils.consume(equipmentResponse.getEntity());

                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/CABINET\"}", "UTF-8");
                            System.out.println(installationUrl);
                            HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                            installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            installationHttpPost.setEntity(installationInputData);

                            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                            System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                        } else {
                            if (cabinet.get("action").textValue().equals("modify")) {
                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + cabinet.get("_equipmentId").toString().replace("\"", "");
                                System.out.println(equipmentUrl);
                                HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
                                equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentHttpPatch.setEntity(equipmentInputData);

                                CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
                                System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());


                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}", "UTF-8");
                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + cabinet.get("_installationId").toString().replace("\"", "");
                                System.out.println(installationUrl);
                                HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                                installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPatch.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            } else if (cabinet.get("action").textValue().equals("replace")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + cabinet.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);

                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/CABINET\"}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                                HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                                equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentPost.setEntity(equipmentInputData);
                                HttpClient equipmentHttpClient = HttpClients.createDefault();
                                HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                                EntityUtils.consume(equipmentResponse.getEntity());

                                String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/CABINET\"}", "UTF-8");
                                System.out.println(installationUrl);
                                HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                                installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPost.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            } else if (cabinet.get("action").textValue().equals("dismantle")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + cabinet.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Power sections
                System.out.println("========== POWER =========");
                try {
                    for (JsonNode powerSource : fillSite.get("powerSources")) {
                        if (powerSource.get("_powerSourceId") == null) {
                            String powerSourceUrl = "http://assets:8080/asset-management/api/powerSources";
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            StringEntity powerSourceInputData = new StringEntity("{\"params\":" + powerSource.get("object").toString() + ", \"site\": \"" + siteUrl + "\"}", "UTF-8");
                            System.out.println("[PowerSource:" + powerSourceInputData + "]");
                            System.out.println("{\"params\":" + powerSource.get("object").toString() + ", \"site\": \"" + siteUrl + "\"}");
                            HttpPost facilityHttpPost = new HttpPost(new URI(powerSourceUrl));
                            facilityHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            facilityHttpPost.setEntity(powerSourceInputData);

                            CloseableHttpClient powerSourceHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse powerSourceResponse = powerSourceHttpClient.execute(facilityHttpPost);
                            System.out.println("powerSourceResponse: " + powerSourceResponse.getStatusLine().getStatusCode());
                        } else {
                            if (powerSource.get("action").textValue().equals("modify")) {
                                String powerSourceUrl = "http://assets:8080/asset-management/api/powerSources/" + powerSource.get("_powerSourceId").toString().replace("\"", "");
                                StringEntity powerSourceInputData = new StringEntity("{\"params\":" + powerSource.get("object").toString() + "}", "UTF-8");
                                System.out.println("[PowerSource:" + powerSourceInputData + "]");
                                HttpPatch facilityHttpPatch = new HttpPatch(new URI(powerSourceUrl));
                                facilityHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                facilityHttpPatch.setEntity(powerSourceInputData);

                                CloseableHttpClient powerSourceHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse powerSourceResponse = powerSourceHttpClient.execute(facilityHttpPatch);
                                System.out.println("powerSourceResponse: " + powerSourceResponse.getStatusLine().getStatusCode());
                            } else if (powerSource.get("action").textValue().equals("replace")) {
                                String powerDeleteUrl = "http://assets:8080/asset-management/api/powerSources/" + powerSource.get("_powerSourceId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(powerDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);

                                String powerSourceUrl = "http://assets:8080/asset-management/api/powerSources";
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                                StringEntity powerSourceInputData = new StringEntity("{\"params\":" + powerSource.get("object").toString() + ", \"site\": \"" + siteUrl + "\"}", "UTF-8");
                                System.out.println("[PowerSource:" + powerSourceInputData + "]");
                                System.out.println("{\"params\":" + powerSource.get("object").toString() + ", \"site\": \"" + siteUrl + "\"}");
                                HttpPost facilityHttpPost = new HttpPost(new URI(powerSourceUrl));
                                facilityHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                facilityHttpPost.setEntity(powerSourceInputData);

                                CloseableHttpClient powerSourceHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse powerSourceResponse = powerSourceHttpClient.execute(facilityHttpPost);
                                System.out.println("powerSourceResponse: " + powerSourceResponse.getStatusLine().getStatusCode());
                            } else if (powerSource.get("action").textValue().equals("dismantle")) {
                                String powerDeleteUrl = "http://assets:8080/asset-management/api/powerSources/" + powerSource.get("_powerSourceId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(powerDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //RUDUs sections
                System.out.println("========== RUDU =========");
                try {
                    for (JsonNode rudu : fillSite.get("rudus")) {
                        JsonNode equipment = rudu.get("equipment").get("object");
                        JsonNode installation = rudu.get("installation").get("object");
                        if (rudu.get("_equipmentId") == null || rudu.get("_installationId") == null) {
                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/RUDU\"}", "UTF-8");
                            String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            equipmentPost.setEntity(equipmentInputData);
                            HttpClient equipmentHttpClient = HttpClients.createDefault();
                            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                            EntityUtils.consume(equipmentResponse.getEntity());

                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/RUDU\"}", "UTF-8");
                            System.out.println(installationUrl);
                            HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                            installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            installationHttpPost.setEntity(installationInputData);

                            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                            System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                        } else {
                            if (rudu.get("action").textValue().equals("modify")) {
                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + rudu.get("_equipmentId").toString().replace("\"", "");
                                System.out.println(equipmentUrl);
                                HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
                                equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentHttpPatch.setEntity(equipmentInputData);

                                CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
                                System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());


                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}", "UTF-8");
                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + rudu.get("_installationId").toString().replace("\"", "");
                                System.out.println(installationUrl);
                                HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                                installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPatch.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            } else if (rudu.get("action").textValue().equals("replace")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + rudu.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);

                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/RUDU\"}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                                HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                                equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentPost.setEntity(equipmentInputData);
                                HttpClient equipmentHttpClient = HttpClients.createDefault();
                                HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                                EntityUtils.consume(equipmentResponse.getEntity());

                                String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/RUDU\"}", "UTF-8");
                                System.out.println(installationUrl);
                                HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                                installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPost.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            } else if (rudu.get("action").textValue().equals("dismantle")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + rudu.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Supplementaryies sections
                System.out.println("========== SUPPLEMENTARY =========");
                try {
                    for (JsonNode supplementary : fillSite.get("supplementary")) {
                        JsonNode equipment = supplementary.get("equipment").get("object");
                        JsonNode installation = supplementary.get("installation").get("object");

                        if (supplementary.get("_equipmentId") == null || supplementary.get("_installationId") == null) {

                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/SUPPLEMENTARY\"}", "UTF-8");
                            String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            equipmentPost.setEntity(equipmentInputData);
                            HttpClient equipmentHttpClient = HttpClients.createDefault();
                            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                            EntityUtils.consume(equipmentResponse.getEntity());

                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/SUPPLEMENTARY\"}", "UTF-8");
                            System.out.println(installationUrl);
                            HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                            installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            installationHttpPost.setEntity(installationInputData);

                            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                            System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                        } else {
                            if (supplementary.get("action").textValue().equals("modify")) {
                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + supplementary.get("_equipmentId").toString().replace("\"", "");
                                System.out.println(equipmentUrl);
                                HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
                                equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentHttpPatch.setEntity(equipmentInputData);

                                CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
                                System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());

                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}", "UTF-8");
                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + supplementary.get("_installationId").toString().replace("\"", "");
                                System.out.println(installationUrl);
                                HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                                installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPatch.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            } else if (supplementary.get("action").equals("replace")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + supplementary.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);

                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/SUPPLEMENTARY\"}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                                HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                                equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentPost.setEntity(equipmentInputData);
                                HttpClient equipmentHttpClient = HttpClients.createDefault();
                                HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                                EntityUtils.consume(equipmentResponse.getEntity());

                                String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/SUPPLEMENTARY\"}", "UTF-8");
                                System.out.println(installationUrl);
                                HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                                installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPost.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            } else if (supplementary.get("action").textValue().equals("dismantle")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + supplementary.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Antennas sections
                System.out.println("========== ANTENNAS =========");
                try {
                    for (JsonNode antanna : fillSite.get("antennas")) {
                        JsonNode equipment = antanna.get("equipment").get("object");
                        JsonNode installation = antanna.get("installation").get("object");

                        if (antanna.get("_equipmentId") == null || antanna.get("_installationId") == null) {

                            StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/ANTENNA\"}", "UTF-8");
                            String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                            HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                            equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            equipmentPost.setEntity(equipmentInputData);
                            HttpClient equipmentHttpClient = HttpClients.createDefault();
                            HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                            EntityUtils.consume(equipmentResponse.getEntity());

                            String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                            String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                            String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                            String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                            StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/ANTENNA\"}", "UTF-8");
                            System.out.println(installationUrl);
                            HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                            installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                            installationHttpPost.setEntity(installationInputData);

                            CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                            CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                            System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                        } else {
                            if (antanna.get("action").textValue().equals("modify")) {
                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + "}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances/" + antanna.get("_equipmentId").toString().replace("\"", "");
                                System.out.println(equipmentUrl);
                                HttpPatch equipmentHttpPatch = new HttpPatch(new URI(equipmentUrl));
                                equipmentHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentHttpPatch.setEntity(equipmentInputData);

                                CloseableHttpClient equipmentHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentHttpPatch);
                                System.out.println("equipmentResponse: " + equipmentResponse.getStatusLine().getStatusCode());

                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + "}", "UTF-8");
                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances/" + antanna.get("_installationId").toString().replace("\"", "");
                                System.out.println(installationUrl);
                                HttpPatch installationHttpPatch = new HttpPatch(new URI(installationUrl));
                                installationHttpPatch.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPatch.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPatch);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());
                            } else if (antanna.get("action").textValue().equals("replace")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + antanna.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);

                                StringEntity equipmentInputData = new StringEntity("{\"params\":" + equipment.toString() + ", \"definition\":\"http://assets:8080/asset-management/api/equipmentDefinitions/ANTENNA\"}", "UTF-8");
                                String equipmentUrl = "http://assets:8080/asset-management/api/equipmentInstances";
                                HttpPost equipmentPost = new HttpPost(new URI(equipmentUrl));
                                equipmentPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                equipmentPost.setEntity(equipmentInputData);
                                HttpClient equipmentHttpClient = HttpClients.createDefault();
                                HttpResponse equipmentResponse = equipmentHttpClient.execute(equipmentPost);
                                EntityUtils.consume(equipmentResponse.getEntity());

                                String equipmentInstanceUrl = equipmentResponse.getFirstHeader("Location").getValue();

                                String installationUrl = "http://assets:8080/asset-management/api/installationInstances";
                                String facilityUrl = "http://assets:8080/asset-management/api/facilityInstances/" + facilityMap.get(installation.get("location").textValue());
                                String siteUrl = "http://assets:8080/asset-management/api/sites/" + fillSite.get("siteId").textValue().replaceAll("\"", "");

                                StringEntity installationInputData = new StringEntity("{\"params\":" + installation.toString() + ", \"site\":\"" + siteUrl + "\", \"facility\":\"" + facilityUrl + "\", \"equipment\":\"" + equipmentInstanceUrl + "\", \"definition\":\"http://assets:8080/asset-management/api/installationDefinitions/ANTENNA\"}", "UTF-8");
                                System.out.println(installationUrl);
                                HttpPost installationHttpPost = new HttpPost(new URI(installationUrl));
                                installationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                                installationHttpPost.setEntity(installationInputData);

                                CloseableHttpClient installationHttpClient = HttpClients.createDefault();
                                CloseableHttpResponse installationResponse = installationHttpClient.execute(installationHttpPost);
                                System.out.println("installationResponse: " + installationResponse.getStatusLine().getStatusCode());

                            } else if (antanna.get("action").textValue().equals("dismantle")) {
                                String installationDeleteUrl = "http://assets:8080/asset-management/api/installationInstances/" + antanna.get("_installationId").toString().replace("\"", "");
                                HttpDelete httpDelete = new HttpDelete(new URI(installationDeleteUrl));
                                CloseableHttpClient deleteInstallationHttpClient = HttpClients.createDefault();
                                deleteInstallationHttpClient.execute(httpDelete);
                            }
                        }
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
