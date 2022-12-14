package kz.kcell.flow.leasing.asset;

import kz.kcell.Utils;
import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("assetUpdateCandidate")
public class UpdateCandidate implements JavaDelegate {

    private Minio minioClient;
    private String baseUri;
    private String assetsUri;

    @Autowired
    DataSource dataSource;

    @Autowired
    public UpdateCandidate(Minio minioClient, @Value("${mail.message.baseurl:http://localhost}") String baseUri, @Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUri) {
        this.minioClient = minioClient;
        this.baseUri = baseUri;
        this.assetsUri = assetsUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String updateAssetCandidateTable = delegateExecution.getVariable("updateAssetCandidateTable") != null ? delegateExecution.getVariable("updateAssetCandidateTable").toString() : null;
        log.info("updateAssetCandidateTable>>>>>");
        log.info(updateAssetCandidateTable);
        log.info("<<<<<updateAssetCandidateTable");
        if (updateAssetCandidateTable != null) {
            updateAssetCandidateTable = updateAssetCandidateTable.replaceAll("\"", "");

            String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
            String longitude = delegateExecution.getVariable("longitude") != null ? delegateExecution.getVariable("longitude").toString() : null;
            String latitude = delegateExecution.getVariable("latitude") != null ? delegateExecution.getVariable("latitude").toString() : null;
            String altitude = delegateExecution.getVariable("altitude") != null ? delegateExecution.getVariable("altitude").toString() : null;
            String region = delegateExecution.getVariable("regionCatalogId") != null ? delegateExecution.getVariable("regionCatalogId").toString() : null;

            String assetsCreatedCnAddressId = delegateExecution.getVariable("assetsCreatedCnAddressId") != null ? delegateExecution.getVariable("assetsCreatedCnAddressId").toString() : null;
            String assetsCreatedNeAddressId = delegateExecution.getVariable("assetsCreatedNeAddressId") != null ? delegateExecution.getVariable("assetsCreatedNeAddressId").toString() : null;
            String assetsCreatedNeFacilitieId = delegateExecution.getVariable("assetsCreatedNeFacilitieId") != null ? delegateExecution.getVariable("assetsCreatedNeFacilitieId").toString() : null;
            String assetsCreatedCnFacilitieId = delegateExecution.getVariable("assetsCreatedCnFacilitieId") != null ? delegateExecution.getVariable("assetsCreatedCnFacilitieId").toString() : null;
            String assetsCreatedPowerSourcesId = delegateExecution.getVariable("assetsCreatedPowerSourcesId") != null ? delegateExecution.getVariable("assetsCreatedPowerSourcesId").toString() : null;
            String assetsCreatedCellAntennaInfoId = delegateExecution.getVariable("assetsCreatedCellAntennaId") != null ? delegateExecution.getVariable("assetsCreatedCellAntennaId").toString() : null;

            SpinJsonNode addressJson = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
            SpinJsonNode cellAntennaJson = delegateExecution.getVariable("cellAntenna") != null ? JSON(delegateExecution.getVariable("cellAntenna")) : null;
            SpinJsonNode farEndInformation = delegateExecution.getVariable("farEndInformation") != null ? JSON(delegateExecution.getVariable("farEndInformation")) : null;
            SpinJsonNode candidate = delegateExecution.getVariable("candidate") != null ? JSON(delegateExecution.getVariable("candidate")) : null;
            SpinJsonNode transmissionAntenna = delegateExecution.getVariable("transmissionAntenna") != null ? JSON(delegateExecution.getVariable("transmissionAntenna")) : null;

            String ne_longitude = transmissionAntenna != null ? (transmissionAntenna.hasProp("longitude") ? transmissionAntenna.prop("longitude").value().toString() : null) : null;
            String ne_latitude = transmissionAntenna != null ? (transmissionAntenna.hasProp("latitude") ? transmissionAntenna.prop("latitude").value().toString() : null) : null;
            String ne_city_catalogs_id = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("city_catalogs_id") ? transmissionAntenna.prop("address").prop("city_catalogs_id").value().toString() : null) : null;
            String ne_addr_street_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_street") ? transmissionAntenna.prop("address").prop("cn_addr_street").value().toString() : null) : null;
            String ne_addr_building_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_building") ? transmissionAntenna.prop("address").prop("cn_addr_building").value().toString() : null) : null;
            String ne_addr_cadastral_number_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_cadastral_number") ? transmissionAntenna.prop("address").prop("cn_addr_cadastral_number").value().toString() : null) : null;
            String ne_addr_note_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_note") ? transmissionAntenna.prop("address").prop("cn_addr_note").value().toString() : null) : null;


//            SpinJsonNode constructionType = delegateExecution.getVariable("candidate") != null ? (candidate.hasProp("constructionType") ? JSON(candidate.prop("constructionType").value().toString()) : null) : null;
            String cn_constructionType = candidate != null ? (candidate.hasProp("constructionType") && candidate.prop("constructionType").hasProp("catalogsId") ? candidate.prop("constructionType").prop("catalogsId").value().toString() : null) : null;
            String cn_altitude = candidate != null ? (candidate.hasProp("cn_altitude") ? candidate.prop("cn_altitude").value().toString() : null) : null;

            if (cn_altitude != null && cn_altitude.indexOf(".") < 0) {
                cn_altitude = cn_altitude + ".0";
            }

            String construction_height = candidate != null ? (candidate.hasProp("cn_height_constr") ? candidate.prop("cn_height_constr").value().toString() : null) : null;
            String square = candidate != null ? (candidate.hasProp("square") ? candidate.prop("square").value().toString() : null) : null;

            if (updateAssetCandidateTable.equals("cn_adresses")) {

                String cn_addr_city_name = addressJson != null ? (addressJson.hasProp("cn_addr_city") ? addressJson.prop("cn_addr_city").value().toString() : null) : null;
                String cn_city_catalogs_id = addressJson != null ? (addressJson.hasProp("city_catalogs_id") ? addressJson.prop("city_catalogs_id").value().toString() : null) : null;
                String cn_addr_street_name = addressJson != null ? (addressJson.hasProp("cn_addr_street") ? addressJson.prop("cn_addr_street").value().toString() : null) : null;
                String cn_addr_building_name = addressJson != null ? (addressJson.hasProp("cn_addr_building") ? addressJson.prop("cn_addr_building").value().toString() : null) : null;
                String cn_addr_cadastral_number_name = addressJson != null ? (addressJson.hasProp("cn_addr_cadastral_number") ? addressJson.prop("cn_addr_cadastral_number").value().toString() : null) : null;
                String cn_addr_note_name = addressJson != null ? (addressJson.hasProp("cn_addr_note") ? addressJson.prop("cn_addr_note").value().toString() : null) : null;

//                SpinJsonNode projectJson = delegateExecution.getVariable("project") != null ? JSON(delegateExecution.getVariable("project")) : null;


                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject newCandidate = new JSONObject();
                JSONObject value = new JSONObject();


                JSONObject regionJson = new JSONObject();
                regionJson.put("catalog_id", 5);
                regionJson.put("id", region);

                //            initiator_id
                if (cn_city_catalogs_id != null) {
                    JSONObject city_id_json = new JSONObject();
                    city_id_json.put("catalog_id", 32);
                    city_id_json.put("id", cn_city_catalogs_id);
                    value.put("city_id", city_id_json);
                }

                if (cn_addr_street_name != null) {
                    value.put("street", cn_addr_street_name);
                }

                if (cn_addr_building_name != null) {
                    value.put("building", cn_addr_building_name);
                }

                if (cn_addr_cadastral_number_name != null) {
                    value.put("cadastral_number", cn_addr_cadastral_number_name);
                }

                if (cn_addr_note_name != null) {
                    value.put("note", cn_addr_note_name);
                }

                value.put("region_id", regionJson);

                log.info(value.toString());

                HttpPut httpPut = Utils.createHttpPut(
                    assetsUri + "/asset-management/addresses/id/" + assetsCreatedCnAddressId, baseUri, value);

                CloseableHttpResponse response = httpclient.execute(httpPut);

//                HttpEntity entity = response.getEntity();
//                String responseString = EntityUtils.toString(entity, "UTF-8");
//                JSONObject jsonResponse = new JSONObject(responseString);
//                Long assetsCnAddress = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + response.getStatusLine().getStatusCode());
                }

//                if (assetsCnAddress != null) {
//                    delegateExecution.setVariable("assetsCreatedCnAddressId", assetsCnAddress);
//                } else {
//                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
//                }

            }

            if (updateAssetCandidateTable.equals("ne_adresses")) {

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject newCandidate = new JSONObject();
                JSONObject value = new JSONObject();


                JSONObject regionJson = new JSONObject();
                regionJson.put("catalog_id", 5);
                regionJson.put("id", region);

                //            initiator_id
                if (ne_city_catalogs_id != null) {
                    JSONObject city_id_json = new JSONObject();
                    city_id_json.put("catalog_id", 32);
                    city_id_json.put("id", ne_city_catalogs_id);
                    value.put("city_id", city_id_json);
                }

                if (ne_addr_street_name != null) {
                    value.put("street", ne_addr_street_name);
                }

                if (ne_addr_building_name != null) {
                    value.put("building", ne_addr_building_name);
                }

                if (ne_addr_cadastral_number_name != null) {
                    value.put("cadastral_number", ne_addr_cadastral_number_name);
                }

                if (ne_addr_note_name != null) {
                    value.put("note", ne_addr_note_name);
                }

                value.put("region_id", regionJson);

                log.info(value.toString());

                HttpPut httpPut = Utils.createHttpPut(
                    assetsUri + "/asset-management/addresses/id/" + assetsCreatedNeAddressId, baseUri, value);

                CloseableHttpResponse response = httpclient.execute(httpPut);

//                HttpEntity entity = response.getEntity();
//                String responseString = EntityUtils.toString(entity, "UTF-8");
//                JSONObject jsonResponse = new JSONObject(responseString);
//                Long assetsNeAddress = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + response.getStatusLine().getStatusCode());
                }

//                if (assetsNeAddress != null) {
//                    delegateExecution.setVariable("assetsCreatedNeAddressId", assetsNeAddress);
//                } else {
//                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
//                }

            }

            if (updateAssetCandidateTable.equals("facilitiesCN")) {


                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject newCandidate = new JSONObject();
                JSONObject value = new JSONObject();


                JSONObject regionJson = new JSONObject();
                regionJson.put("catalog_id", 5);
                regionJson.put("id", region);

                if (assetsCreatedCnAddressId != null) {
                    value.put("address_id", assetsCreatedCnAddressId);
                }

                value.put("latitude", latitude);
                value.put("longitude", longitude);
                if (cn_altitude != null) {
                    value.put("altitude", cn_altitude);
                }

                //            initiator_id
                if (cn_constructionType != null) {
                    JSONObject construction_type_id_json = new JSONObject();
                    construction_type_id_json.put("catalog_id", 14);
                    construction_type_id_json.put("id", cn_constructionType);
                    value.put("construction_type_id", construction_type_id_json);
                }

                if (construction_height != null) {
                    value.put("construction_height", construction_height);
                }

                if (square != null) {
                    value.put("square", square);
                }

                log.info("facilitiesCN body value.toString(): ");
                log.info(value.toString());

                log.info("facilitiesCN url: ");
                log.info(assetsUri + "/asset-management/facilities/id/" + assetsCreatedCnFacilitieId);

                log.info("facilitiesCN baseUri: ");
                log.info(baseUri);

                HttpPut httpPut = Utils.createHttpPut(
                    assetsUri + "/asset-management/facilities/id/" + assetsCreatedCnFacilitieId, baseUri, value);

                CloseableHttpResponse response = httpclient.execute(httpPut);

//                HttpEntity entity = response.getEntity();
//                String responseString = EntityUtils.toString(entity, "UTF-8");
//                JSONObject jsonResponse = new JSONObject(responseString);
//                Long assetsCnFacilitie = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + response.getStatusLine().getStatusCode() + "\n body " + EntityUtils.toString(response.getEntity()));
                }

//                if (assetsCnFacilitie != null) {
//                    delegateExecution.setVariable("assetsCreatedCnFacilitieId", assetsCnFacilitie);
//                } else {
//                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
//                }

            }
            if (updateAssetCandidateTable.equals("facilitiesNE")) {

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject newCandidate = new JSONObject();
                JSONObject value = new JSONObject();


                JSONObject regionJson = new JSONObject();
                regionJson.put("catalog_id", 5);
                regionJson.put("id", region);

                if (assetsCreatedCnAddressId != null) {
                    value.put("address_id", assetsCreatedNeAddressId);
                }

                value.put("latitude", ne_latitude);
                value.put("longitude", ne_longitude);
                if (cn_altitude != null) {
                    value.put("altitude", cn_altitude);
                }

                //            initiator_id
                if (cn_constructionType != null) {
                    JSONObject construction_type_id_json = new JSONObject();
                    construction_type_id_json.put("catalog_id", 14);
                    construction_type_id_json.put("id", cn_constructionType);
                    value.put("construction_type_id", construction_type_id_json);
                }

                if (construction_height != null) {
                    value.put("construction_height", construction_height);
                }

                if (square != null) {
                    value.put("square", square);
                }

                HttpPut httpPut = Utils.createHttpPut(
                    assetsUri + "/asset-management/facilities/id/" + assetsCreatedNeFacilitieId, baseUri, value);

                CloseableHttpResponse response = httpclient.execute(httpPut);

//                HttpEntity entity = response.getEntity();
//                String responseString = EntityUtils.toString(entity, "UTF-8");
//                JSONObject jsonResponse = new JSONObject(responseString);
//                Long assetsNeFacilitie = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + response.getStatusLine().getStatusCode());
                }

//                if (assetsNeFacilitie != null) {
//                    delegateExecution.setVariable("assetsCreatedNeFacilitieId", assetsNeFacilitie);
//                } else {
//                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
//                }
            }

            if (updateAssetCandidateTable.equals("files")) {
//                SpinJsonNode powerSource = delegateExecution.getVariable("powerSource") != null ? JSON(delegateExecution.getVariable("powerSource")) : null;
//
//                String cableLength = powerSource != null ? (powerSource.hasProp("cableLength") ? powerSource.prop("cableLength").value().toString() : null) : null;
//                Boolean provideUs3Phase = powerSource != null ? (powerSource.hasProp("provideUs3Phase") && powerSource.prop("provideUs3Phase").value().toString().equals("Yes") ? true : false) : null;
//                Boolean agreeToReceiveMonthlyPaymen = powerSource != null ? (powerSource.hasProp("agreeToReceiveMonthlyPaymen") && powerSource.prop("agreeToReceiveMonthlyPaymen").value().toString().equals("Yes") ? true : false) : null;
//                String res_electrical_line04 = powerSource != null ? (powerSource.hasProp("closestPublic04") ? powerSource.prop("closestPublic04").value().toString() : null) : null;
//                String res_electrical_line10 = powerSource != null ? (powerSource.hasProp("closestPublic10") ? powerSource.prop("closestPublic10").value().toString() : null) : null;
//
////                JSONArray cable_laying_type_id_json_array = powerSource != null ? (powerSource.hasProp("cableLayingType") ? new JSONArray(powerSource.prop("cableLayingType") .toString()) : null) : null;
//                SpinJsonNode cable_laying_type_id_json_array = powerSource != null ? (powerSource.hasProp("cableLayingType") ? powerSource.prop("cableLayingType") : null) : null;
//
//                SpinList<SpinJsonNode> cable_laying_type_id_json_list = cable_laying_type_id_json_array.elements();
//
//                SpinJsonNode cable_laying_type_id_json_obj = cable_laying_type_id_json_list.get(0);
//
//                String cable_laying_type_id = (cable_laying_type_id_json_obj != null && cable_laying_type_id_json_obj.prop("id") != null ) ?  cable_laying_type_id_json_obj.prop("id").value().toString() : null;
//
//                SSLContextBuilder builder = new SSLContextBuilder();
//                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
//                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
//                    builder.build());
//                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
//                    sslsf).build();
//
//                JSONObject value = new JSONObject();
//
//                if (cable_laying_type_id != null) {
//                    JSONObject cable_laying_type_id_json = new JSONObject();
//                    JSONArray cable_laying_type_id_json_ar =  new JSONArray();
//                    cable_laying_type_id_json_ar.put(cable_laying_type_id);
//                    cable_laying_type_id_json.put("catalog_id", 18);
//                    cable_laying_type_id_json.put("ids", cable_laying_type_id_json_ar);
//                    value.put("cable_laying_type_id", cable_laying_type_id_json);
//                }
//                if (provideUs3Phase != null) {
//                    value.put("power_three_phases", provideUs3Phase);
//                }
//
//                if (cableLength != null) {
//                    value.put("cable_length_connection_point", cableLength);
//                }
//
//                if (agreeToReceiveMonthlyPaymen != null) {
//                    value.put("receive_monthly_payment", agreeToReceiveMonthlyPaymen);
//                }
//
//                if (res_electrical_line04 != null) {
//                    value.put("power_three_phases", provideUs3Phase);
//                }
//
//                if (res_electrical_line10 != null) {
//                    value.put("construction_height", construction_height);
//                }
//
//                log.info("body value.toString(): ");
//                log.info(value.toString());
//
//                HttpPut httpPost = LeasingAssetUtils.createHttpPost(
//                "https://asset.test-flow.kcell.kz/asset-management/powerSources/", baseUri, value);
//
//                CloseableHttpResponse response = httpclient.execute(httpPost);
//
//                HttpEntity entity = response.getEntity();
//                String responseString = EntityUtils.toString(entity, "UTF-8");
//                JSONObject jsonResponse = new JSONObject(responseString);
//                Long powerSourcesId = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;
//
//                log.info("post response code: " + response.getStatusLine().getStatusCode());
//                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
//                    throw new RuntimeException("Candidate post returns code " + response.getStatusLine().getStatusCode());
//                }
//
//                if (powerSourcesId != null) {
//                    delegateExecution.setVariable("assetsCreatedPowerSourcesId", powerSourcesId);
//                } else {
//                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
//                }
            }
            if (updateAssetCandidateTable.equals("site")) {
                String site_name = delegateExecution.getVariable("siteName") != null ? delegateExecution.getVariable("siteName").toString() : null;
                SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;
                Long assetsCreatedSiteId = delegateExecution.getVariable("assetsCreatedSiteId") != null ? Long.valueOf(delegateExecution.getVariable("assetsCreatedSiteId").toString()) : null;

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject value = new JSONObject();
                value.put("id", assetsCreatedSiteId);
                if (ncpId != null) {
                    value.put("siteid", ncpId);
                }
                if (site_name != null) {
                    value.put("site_name", site_name);
                }
                if (siteTypeJson != null) {
                    JSONObject site_type_id_json = new JSONObject();
                    site_type_id_json.put("catalog_id", 2);
                    site_type_id_json.put("id", siteTypeJson.prop("assetsId").value().toString());
                    value.put("site_type_id", site_type_id_json);
                }

                JSONObject site_status_id_json = new JSONObject();
                site_status_id_json.put("catalog_id", 3);
                site_status_id_json.put("id", 8);
                value.put("site_status_id", site_status_id_json);

                if (candidate != null && candidate.hasProp("transmissionTypeAmCatalogsId")) {
                    JSONObject transmission_type_id_json = new JSONObject();
                    transmission_type_id_json.put("catalog_id", 4);
                    transmission_type_id_json.put("id", candidate.prop("transmissionTypeAmCatalogsId").value().toString());
                    value.put("transmission_type_id", transmission_type_id_json);
                }
                if (assetsCreatedCellAntennaInfoId != null) {
                    value.put("cell_antenna_info_id", assetsCreatedCellAntennaInfoId);
                }
                if (assetsCreatedPowerSourcesId != null) {
                    value.put("power_source_id", assetsCreatedPowerSourcesId);
                }
                if (assetsCreatedNeFacilitieId != null) {
                    value.put("facility_id", assetsCreatedNeFacilitieId);
                }
                if (assetsCreatedCnFacilitieId != null) {
                    value.put("facility_main_id", assetsCreatedCnFacilitieId);
                }
                if (site_name != null) {
                    value.put("site_name", site_name);
                }

                log.info("site body value.toString(): ");
                log.info(value.toString());

                log.info("site url: ");
                log.info(assetsUri + "/asset-management/sites/id/" + assetsCreatedSiteId);

                log.info("site baseUri: ");
                log.info(baseUri);

                HttpPut httpPut = Utils.createHttpPut(
                    assetsUri + "/asset-management/sites/id/" + assetsCreatedSiteId, baseUri, value);

                CloseableHttpResponse postResponse = httpclient.execute(httpPut);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);

                log.info("put response code: " + postResponse.getStatusLine().getStatusCode());

                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode()  + "\n body " + responseString);
                }
            }
            if (updateAssetCandidateTable.equals("powerSources")) {

                SpinJsonNode powerSource = delegateExecution.getVariable("powerSource") != null ? JSON(delegateExecution.getVariable("powerSource")) : null;

                String cableLength = powerSource != null ? (powerSource.hasProp("cableLength") ? powerSource.prop("cableLength").value().toString() : null) : null;
                Boolean provideUs3Phase = powerSource != null ? (powerSource.hasProp("provideUs3Phase") && powerSource.prop("provideUs3Phase").value().toString().equals("Yes") ? true : false) : null;
                Boolean agreeToReceiveMonthlyPaymen = powerSource != null ? (powerSource.hasProp("agreeToReceiveMonthlyPaymen") && powerSource.prop("agreeToReceiveMonthlyPaymen").value().toString().equals("Yes") ? true : false) : null;
                String res_electrical_line04 = powerSource != null ? (powerSource.hasProp("closestPublic04") ? powerSource.prop("closestPublic04").value().toString() : null) : null;
                String res_electrical_line10 = powerSource != null ? (powerSource.hasProp("closestPublic10") ? powerSource.prop("closestPublic10").value().toString() : null) : null;

//                JSONArray cable_laying_type_id_json_array = powerSource != null ? (powerSource.hasProp("cableLayingType") ? new JSONArray(powerSource.prop("cableLayingType") .toString()) : null) : null;
                SpinJsonNode cable_laying_type_id_json_array = powerSource != null ? (powerSource.hasProp("cableLayingType") ? powerSource.prop("cableLayingType") : null) : null;

                SpinList<SpinJsonNode> cable_laying_type_id_json_list = cable_laying_type_id_json_array.elements();

                SpinJsonNode cable_laying_type_id_json_obj = cable_laying_type_id_json_list.get(0);

                String cable_laying_type_id = (cable_laying_type_id_json_obj != null && cable_laying_type_id_json_obj.prop("id") != null) ? cable_laying_type_id_json_obj.prop("id").value().toString() : null;

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject value = new JSONObject();

                if (cable_laying_type_id != null) {
                    JSONObject cable_laying_type_id_json = new JSONObject();
                    JSONArray cable_laying_type_id_json_ar = new JSONArray();
                    cable_laying_type_id_json_ar.put(cable_laying_type_id);
                    cable_laying_type_id_json.put("catalog_id", 18);
                    cable_laying_type_id_json.put("ids", cable_laying_type_id_json_ar);
                    value.put("cable_laying_type_id", cable_laying_type_id_json);
                }
                if (provideUs3Phase != null) {
                    value.put("power_three_phases", provideUs3Phase);
                }

                if (cableLength != null) {
                    value.put("cable_length_connection_point", cableLength);
                }

                if (agreeToReceiveMonthlyPaymen != null) {
                    value.put("receive_monthly_payment", agreeToReceiveMonthlyPaymen);
                }

                if (res_electrical_line04 != null) {
                    value.put("res_electrical_line04", res_electrical_line04);
                }

                if (res_electrical_line10 != null) {
                    value.put("res_electrical_line10", res_electrical_line04);
                }

                log.info("body value.toString(): ");
                log.info(value.toString());

                HttpPut httpPut = Utils.createHttpPut(
                    assetsUri + "/asset-management/powerSources/id/" + assetsCreatedPowerSourcesId, baseUri, value);

                CloseableHttpResponse postResponse = httpclient.execute(httpPut);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                log.info("powerSources -PUT response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }
            }

            if (updateAssetCandidateTable.equals("cellAntennaInfo")) {

                SpinJsonNode cellAntenna = delegateExecution.getVariable("cellAntenna") != null ? JSON(delegateExecution.getVariable("cellAntenna")) : null;

//                String du_type_id = cellAntenna != null ? (cellAntenna.hasProp("cn_du") ? cellAntenna.prop("cn_du").value() : null) : null;

//                SpinJsonNode du_type_id_json_array = cellAntenna != null ? (cellAntenna.hasProp("cn_du") ? JSON(cellAntenna.prop("cn_du").value().toString()) : null) : null;
//                SpinList du_type_id_json_list = du_type_id_json_array.elements();
//                SpinJsonNode du_type_id_json_obj = (SpinJsonNode) du_type_id_json_list.get(0);
//                String du_type_id = (du_type_id_json_obj != null && du_type_id_json_obj.hasProp("id")) ?  du_type_id_json_obj.prop("id").value().toString() : null;

                SpinJsonNode du_type_id_json_array = cellAntenna != null ? (cellAntenna.hasProp("cn_du") ? cellAntenna.prop("cn_du") : null) : null;

                String du_unit_string = null;
                if (du_type_id_json_array != null && du_type_id_json_array.elements().size() > 0) {
                    SpinList<SpinJsonNode> du_type_id_json_list = du_type_id_json_array.elements();

                    SpinJsonNode du_type_id = du_type_id_json_list.get(0);
                    du_unit_string = du_type_id.prop("catalogsId").value().toString();
                }

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject value = new JSONObject();

                if (du_unit_string != null) {
                    JSONObject du_type_id_json = new JSONObject();
                    du_type_id_json.put("catalog_id", 59);
                    du_type_id_json.put("id", du_unit_string);
                    value.put("du_type_id", du_type_id_json);
                }

                log.info("body value.toString(): ");
                log.info(value.toString());

                HttpPut httpPut = Utils.createHttpPut(
                    assetsUri + "/asset-management/cellAntennaInfo/id/" + assetsCreatedCellAntennaInfoId, baseUri, value);

                CloseableHttpResponse postResponse = httpclient.execute(httpPut);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                log.info("cellAntennaInfo-PUT response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

            }
            if (updateAssetCandidateTable.equals("rbs")) {

                Long rbsLocationId = null;
                Long bscId = null;
                if (candidate.hasProp("rbsLocation") && candidate.prop("rbsLocation").hasProp("id") && candidate.prop("rbsLocation").prop("id").isNumber()) {
                    rbsLocationId = candidate.prop("rbsLocation").prop("id").numberValue().longValue();
                }
                if (candidate.hasProp("bsc") && candidate.prop("bsc").hasProp("assetsid") && candidate.prop("bsc").prop("assetsid").isNumber()) {
                    bscId = candidate.prop("bsc").prop("assetsid").numberValue().longValue();
                }

                String site_name = delegateExecution.getVariable("siteName") != null ? delegateExecution.getVariable("siteName").toString() : null;
                Long assetsCreatedSiteId = delegateExecution.getVariable("assetsCreatedSiteId") != null ? Long.valueOf(delegateExecution.getVariable("assetsCreatedSiteId").toString()) : null;

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                JSONObject value = new JSONObject();
                value.put("rbs_name", site_name);
                value.put("bsc_rnc_id", bscId);
                value.put("site_id", assetsCreatedSiteId);

                if (rbsLocationId != null) {
                    JSONObject rbsLocationIdValue = new JSONObject();
                    rbsLocationIdValue.put("catalog_id", 13);
                    rbsLocationIdValue.put("id", rbsLocationId);
                    value.put("rbs_location_id", rbsLocationIdValue);
                }

                log.info("body value.toString(): ");
                log.info(value.toString());

                HttpPost httpPost = Utils.createHttpPost(assetsUri + "/asset-management/rbs", baseUri, value);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long rbsCreatedId = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;
                delegateExecution.setVariable("rbsCreatedId", rbsCreatedId);
                log.info("cellAntennaInfo-PUT response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

            }

            if (updateAssetCandidateTable.equals("rbsCabinet")) {

                Long rbsCabinetTypeId = delegateExecution.getVariable("plannedCabinetType") != null ? (delegateExecution.getVariable("plannedCabinetType").toString().equals("Indoor") ? 1L : 2L) : null;
                Long rbsCabinetModelId = null;
                if (candidate.hasProp("bsc") && candidate.prop("bsc").hasProp("id") && candidate.prop("bsc").prop("id").isNumber()) {
                    rbsCabinetTypeId = candidate.prop("bsc").prop("id").numberValue().longValue();
                }

                Long rbsCreatedId = delegateExecution.getVariable("rbsCreatedId") != null ? Long.valueOf(delegateExecution.getVariable("rbsCreatedId").toString()) : null;

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                JSONObject value = new JSONObject();
                value.put("rbs_id", rbsCreatedId);

                if (rbsCabinetModelId != null) {
                    JSONObject rbsCabinetModelIdValue = new JSONObject();
                    rbsCabinetModelIdValue.put("catalog_id", 11);
                    rbsCabinetModelIdValue.put("id", rbsCabinetModelId);
                    value.put("rbs_cabinet_model_id", rbsCabinetModelIdValue);
                }

                if (rbsCabinetTypeId != null) {
                    JSONObject rbsCabinetTypeIdValue = new JSONObject();
                    rbsCabinetTypeIdValue.put("catalog_id", 12);
                    rbsCabinetTypeIdValue.put("id", rbsCabinetTypeId);
                    value.put("rbs_cabinet_type_id", rbsCabinetTypeIdValue);
                }

                log.info("body value.toString(): ");
                log.info(value.toString());

                HttpPost httpPost = Utils.createHttpPost(
                    assetsUri + "/asset-management/rbsCabinets", baseUri, value);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                JSONObject jsonResponse = new JSONObject(responseString);
                Long rbsCabinetCreatedId = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;
                delegateExecution.setVariable("rbsCabinetCreatedId", rbsCabinetCreatedId);

                log.info("cellAntennaInfo-PUT response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }
            }
            if (updateAssetCandidateTable.equals("sectors")) {
                JSONObject cellAntenna = new JSONObject(delegateExecution.getVariable("cellAntenna").toString());
                JSONArray sectors = new JSONArray(cellAntenna.getJSONArray("sectors").toString());
                Long assetsCreatedSiteId = (Long) delegateExecution.getVariable("assetsCreatedSiteId");
                char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
                Boolean different_address = cellAntenna.getJSONObject("address").has("ca_diff_address") && cellAntenna.getJSONObject("address").getBoolean("ca_diff_address");

                for (int i = 0; i < sectors.length(); i++) {
                    JSONObject sector = sectors.getJSONObject(i);
                    Long sectorId = sector.has("assetsCreatedSectorId") ? sector.getLong("assetsCreatedSectorId") : null;
                    String sector_name = "Sector " + (i + 1) + " - Cell " + Character.toUpperCase(alphabet[i]);
                    Long site_id = assetsCreatedSiteId;
                    Long facility_id = Long.valueOf(assetsCreatedCnFacilitieId);
                    Boolean gsm_900 = sector.has("cn_gsm900") && sector.getString("cn_gsm900").contains("Yes");
                    Boolean dcs_1800 = sector.has("cn_dcs1800") && sector.getString("cn_dcs1800").contains("Yes");
                    Boolean wcdma_2100 = sector.has("cn_wcdma_2100") && sector.getString("cn_wcdma_2100").contains("Yes");
                    Boolean umts_900 = sector.has("cn_umts_900") && sector.getString("cn_umts_900").contains("Yes");
                    Boolean lte800 = sector.has("cn_lte800") && sector.getString("cn_lte800").contains("Yes");
                    Boolean ret_lte800 = sector.has("cn_ret_lte800") && sector.getString("cn_ret_lte800").contains("Yes");
                    Boolean lte1800 = sector.has("cn_lte1800") && sector.getString("cn_lte1800").contains("Yes");
                    Boolean ret_lte1800 = sector.has("cn_ret_lte1800") && sector.getString("cn_ret_lte1800").contains("Yes");
                    Boolean lte2100 = sector.has("cn_lte2100") && sector.getString("cn_lte2100").contains("Yes");
                    Boolean ret_lte2100 = sector.has("cn_ret_lte2100") && sector.getString("cn_ret_lte2100").contains("Yes");
                    Boolean duplex_filter_900_1800 = sector.has("cn_lte2100") && sector.getString("cn_duplex_gsm").contains("Yes");
                    Boolean diversity_900_1800 = sector.has("cn_lte2100") && sector.getString("cn_diversity").contains("Yes");
                    Boolean power_splitter_900_1800 = sector.has("cn_lte2100") && sector.getString("cn_power_splitter").contains("Yes");
                    Boolean hcu_900_1800 = sector.has("cn_lte2100") && sector.getString("cn_hcu").contains("Yes");
                    Boolean asc = sector.has("cn_lte2100") && sector.getString("cn_lte2100").contains("Yes");
                    Boolean ret = sector.has("cn_lte2100") && sector.getString("cn_ret").contains("Yes");
                    Boolean tma_900_1800_wcdma = sector.has("cn_lte2100") && sector.getString("cn_tma_gsm").contains("Yes");
                    Boolean tcc_900_1800 = sector.has("cn_lte2100") && sector.getString("cn_tcc").contains("Yes");
                    Boolean extended_range_900_1800 = sector.has("cn_lte2100") && sector.getString("cn_gsm_range").contains("Yes");

                    SSLContextBuilder builder = new SSLContextBuilder();
                    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        builder.build());
                    CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                        sslsf).build();

                    JSONObject value = new JSONObject();

                    if (sector_name != null) {
                        value.put("sector_name", sector_name);
                    }
                    if (site_id != null) {
                        value.put("site_id", site_id);
                    }
                    value.put("different_address", different_address);
                    if (facility_id != null) {
                        value.put("facility_id", facility_id);
                    }
                    if (gsm_900 != null) {
                        value.put("gsm_900", gsm_900);
                    }
                    if (dcs_1800 != null) {
                        value.put("dcs_1800", dcs_1800);
                    }
                    if (wcdma_2100 != null) {
                        value.put("wcdma_2100", wcdma_2100);
                    }
                    if (umts_900 != null) {
                        value.put("umts_900", umts_900);
                    }
                    if (lte800 != null) {
                        value.put("lte800", lte800);
                    }
                    if (ret_lte800 != null) {
                        value.put("ret_lte800", ret_lte800);
                    }
                    if (lte1800 != null) {
                        value.put("lte1800", lte1800);
                    }
                    if (ret_lte1800 != null) {
                        value.put("ret_lte1800", ret_lte1800);
                    }
                    if (lte2100 != null) {
                        value.put("lte2100", lte2100);
                    }
                    if (ret_lte2100 != null) {
                        value.put("ret_lte2100", ret_lte2100);
                    }
                    if (duplex_filter_900_1800 != null) {
                        value.put("duplex_filter_900_1800", duplex_filter_900_1800);
                    }
                    if (diversity_900_1800 != null) {
                        value.put("diversity_900_1800", diversity_900_1800);
                    }
                    if (power_splitter_900_1800 != null) {
                        value.put("power_splitter_900_1800", power_splitter_900_1800);
                    }
                    if (hcu_900_1800 != null) {
                        value.put("hcu_900_1800", hcu_900_1800);
                    }
                    if (asc != null) {
                        value.put("asc", asc);
                    }
                    if (ret != null) {
                        value.put("ret", ret);
                    }
                    if (tma_900_1800_wcdma != null) {
                        value.put("tma_900_1800_wcdma", tma_900_1800_wcdma);
                    }
                    if (tcc_900_1800 != null) {
                        value.put("tcc_900_1800", tcc_900_1800);
                    }
                    if (extended_range_900_1800 != null) {
                        value.put("extended_range_900_1800", extended_range_900_1800);
                    }

                    if (sectorId != null) {
                        HttpPut httpPut = Utils.createHttpPut(
                            assetsUri + "/asset-management/sectors/id/" + sectorId, baseUri, value);

                        CloseableHttpResponse postResponse = httpclient.execute(httpPut);

                        HttpEntity entity = postResponse.getEntity();
                        String responseString = EntityUtils.toString(entity, "UTF-8");
                        log.info("put response code: " + postResponse.getStatusLine().getStatusCode());
                        log.info("sector id: " + sectorId);
                        if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                            throw new RuntimeException("Candidate (Sectors) post returns code " + postResponse.getStatusLine().getStatusCode());
                        }
                    } else {
                        HttpPost httpPost = Utils.createHttpPost(
                            assetsUri + "/asset-management/sectors/", baseUri, value);

                        CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                        HttpEntity entity = postResponse.getEntity();
                        String responseString = EntityUtils.toString(entity, "UTF-8");
                        JSONObject jsonResponse = new JSONObject(responseString);
                        sectorId = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                        log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                        log.info("sector id: " + sectorId);
                        if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                            throw new RuntimeException("Candidate (Sectors) post returns code " + postResponse.getStatusLine().getStatusCode());
                        }

                        if (sectorId != null) {
                            cellAntenna.getJSONArray("sectors").getJSONObject(i).put("assetsCreatedSectorId", sectorId);
                            delegateExecution.setVariable("cellAntenna", SpinValues.jsonValue(cellAntenna.toString()));
                        } else {
                            throw new RuntimeException("Candidate site post by not parsed assetsCreatedCnAddressId from response");
                        }
                    }
                }
            }

            if (updateAssetCandidateTable.equals("antenna")) {
                JSONObject cellAntenna = new JSONObject(delegateExecution.getVariable("cellAntenna").toString());
                JSONArray sectors = new JSONArray(cellAntenna.getJSONArray("sectors").toString());
                Long assetsCreatedSiteId = (Long) delegateExecution.getVariable("assetsCreatedSiteId");
                for (int i = 0; i < sectors.length(); i++) {
                    JSONObject sector = sectors.getJSONObject(i);
                    JSONArray antennas = sector.getJSONArray("antennas");
                    Long sectorId = sector.has("assetsCreatedSectorId") ? sector.getLong("assetsCreatedSectorId") : null;
                    if (sectorId != null) {
                        for (int j = 0; j < antennas.length(); j++) {
                            JSONObject antenna = antennas.getJSONObject(j);
                            Long antennaId = antenna.has("assetsCreatedAntennaId") ? antenna.getLong("assetsCreatedAntennaId") : null;
                            SSLContextBuilder builder = new SSLContextBuilder();
                            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                                builder.build());
                            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                                sslsf).build();

                            Long azimuth = antenna.getLong("azimuth");
                            Long suspension_height_antenna = antenna.getLong("suspensionHeight");
                            JSONObject valueAntenna = new JSONObject();
                            valueAntenna.put("sector_id", sectorId);
                            if (azimuth != null) {
                                valueAntenna.put("azimuth", azimuth);
                            }
                            if (suspension_height_antenna != null) {
                                valueAntenna.put("suspension_height_antennas", suspension_height_antenna);
                            }

                            if(antenna.has("catalog_value_id")){
                                Long antenna_model_catalog_id = antenna.getLong("catalog_value_id");
                                JSONObject antennaModel = new JSONObject();
                                antennaModel.put("catalog_id", 19);
                                antennaModel.put("id", antenna_model_catalog_id);
                                valueAntenna.put("antenna_model_id", antennaModel);
                            }

                            if(antenna.has("cn_antenna_loc_catalog_value_id")){
                                Long cn_antenna_loc_catalog_value_id = antenna.getLong("cn_antenna_loc_catalog_value_id");
                                JSONObject antennaLocation = new JSONObject();
                                antennaLocation.put("catalog_id", 64);
                                antennaLocation.put("id", cn_antenna_loc_catalog_value_id);
                                valueAntenna.put("antenna_location_id", antennaLocation);
                            }

                            if (antennaId != null) {
                                HttpPut httpPutAntenna = Utils.createHttpPut(
                                    assetsUri + "/asset-management/cellAntennas/id/" + antennaId, baseUri, valueAntenna);

                                CloseableHttpResponse putResponseAntenna = httpclient.execute(httpPutAntenna);

                                HttpEntity entityAntenna = putResponseAntenna.getEntity();
                                String responseStringAntenna = EntityUtils.toString(entityAntenna, "UTF-8");
                                JSONObject jsonResponseAntenna = new JSONObject(responseStringAntenna);

                                log.info("put response code: " + putResponseAntenna.getStatusLine().getStatusCode());
                                log.info("antenna id: " + antennaId);
                                if (putResponseAntenna.getStatusLine().getStatusCode() < 200 || putResponseAntenna.getStatusLine().getStatusCode() >= 300) {
                                    throw new RuntimeException("Candidate (Antenna)  put returns code " + putResponseAntenna.getStatusLine().getStatusCode());
                                }
                            } else {

                                HttpPost httpPostAntenna = Utils.createHttpPost(
                                    assetsUri + "/asset-management/cellAntennas/", baseUri, valueAntenna);

                                CloseableHttpResponse postResponseAntenna = httpclient.execute(httpPostAntenna);

                                HttpEntity entityAntenna = postResponseAntenna.getEntity();
                                String responseStringAntenna = EntityUtils.toString(entityAntenna, "UTF-8");
                                JSONObject jsonResponseAntenna = new JSONObject(responseStringAntenna);
                                antennaId = jsonResponseAntenna.has("id") ? jsonResponseAntenna.getLong("id") : null;

                                log.info("post response code: " + postResponseAntenna.getStatusLine().getStatusCode());
                                log.info("antenna id: " + antennaId);
                                if (postResponseAntenna.getStatusLine().getStatusCode() < 200 || postResponseAntenna.getStatusLine().getStatusCode() >= 300) {
                                    throw new RuntimeException("Candidate (Antenna)  post returns code " + postResponseAntenna.getStatusLine().getStatusCode());
                                }

                                if (antennaId != null) {
                                    cellAntenna.getJSONArray("sectors").getJSONObject(i).getJSONArray("antennas").getJSONObject(j).put("assetsCreatedAntennaId", antennaId);
                                    delegateExecution.setVariable("cellAntenna", SpinValues.jsonValue(cellAntenna.toString()));
                                } else {
                                    throw new RuntimeException("Candidate site post by not parsed assetsCreatedCnAddressId from response");
                                }
                            }

                        }
                    } else {
                        throw new RuntimeException("sectorId is null");
                    }
                }
            }


        } else {
            throw new Exception("Error");
        }
    }
}
