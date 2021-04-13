package kz.kcell.flow.leasing.asset;

import com.google.api.client.json.Json;
import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.msgpack.util.json.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.URI;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("assetCreateCandidate")
public class CreateCandidate implements JavaDelegate {

    private Minio minioClient;
    private String baseUri;
    private String assetsUri;

    @Autowired
    DataSource dataSource;

    @Autowired
    public CreateCandidate(Minio minioClient, @Value("${mail.message.baseurl:http://localhost}") String baseUri, @Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUri) {
        this.minioClient = minioClient;
        this.baseUri = baseUri;
        this.assetsUri = assetsUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String createAssetCandidateTable = delegateExecution.getVariable("createAssetCandidateTable") != null ? delegateExecution.getVariable("createAssetCandidateTable").toString() : null;
        log.info("createAssetCandidateTable>>>>>");
        log.info(createAssetCandidateTable);
        log.info("<<<<<createAssetCandidateTable");
        if (createAssetCandidateTable != null) {
            createAssetCandidateTable = createAssetCandidateTable.replaceAll("\"", "");

            String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
            String longitude = delegateExecution.getVariable("longitude") != null ? delegateExecution.getVariable("longitude").toString() : null;
            String latitude = delegateExecution.getVariable("latitude") != null ? delegateExecution.getVariable("latitude").toString() : null;
            String altitude = delegateExecution.getVariable("altitude") != null ? delegateExecution.getVariable("altitude").toString() : null;
            String region = delegateExecution.getVariable("regionCatalogId") != null ? delegateExecution.getVariable("regionCatalogId").toString() : null;

            Long assetsCreatedNcp = delegateExecution.getVariable("assetsCreatedNcp") != null ? (Long) delegateExecution.getVariable("assetsCreatedNcp") : null;
            String assetsCreatedCnAddressId = delegateExecution.getVariable("assetsCreatedCnAddressId") != null ? delegateExecution.getVariable("assetsCreatedCnAddressId").toString() : null;
            String assetsCreatedNeAddressId = delegateExecution.getVariable("assetsCreatedNeAddressId") != null ? delegateExecution.getVariable("assetsCreatedNeAddressId").toString() : null;
            String assetsCreatedNeFacilitieId = delegateExecution.getVariable("assetsCreatedNeFacilitieId") != null ? delegateExecution.getVariable("assetsCreatedNeFacilitieId").toString() : null;
            String assetsCreatedCnFacilitieId = delegateExecution.getVariable("assetsCreatedCnFacilitieId") != null ? delegateExecution.getVariable("assetsCreatedCnFacilitieId").toString() : null;
            String assetsCreatedPowerSourcesId = delegateExecution.getVariable("assetsCreatedPowerSourcesId") != null ? delegateExecution.getVariable("assetsCreatedPowerSourcesId").toString() : null;
            String assetsCreatedCellAntennaId = delegateExecution.getVariable("assetsCreatedCellAntennaId") != null ? delegateExecution.getVariable("assetsCreatedCellAntennaId").toString() : null;

            SpinJsonNode addressJson = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
            SpinJsonNode cellAntennaJson = delegateExecution.getVariable("cellAntenna") != null ? JSON(delegateExecution.getVariable("cellAntenna")) : null;
            SpinJsonNode farEndInformation = delegateExecution.getVariable("farEndInformation") != null ? JSON(delegateExecution.getVariable("farEndInformation")) : null;
            SpinJsonNode candidate = delegateExecution.getVariable("candidate") != null ? JSON(delegateExecution.getVariable("candidate")) : null;
            SpinJsonNode transmissionAntenna = delegateExecution.getVariable("transmissionAntenna") != null ? JSON(delegateExecution.getVariable("transmissionAntenna")) : null;


            String ne_longitude = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("longitude") ? transmissionAntenna.prop("address").prop("longitude").value().toString() : null) : null;
            String ne_latitude = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("latitude") ? transmissionAntenna.prop("address").prop("latitude").value().toString() : null) : null;
            String ne_city_catalogs_id = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("city_catalogs_id") ? transmissionAntenna.prop("address").prop("city_catalogs_id").value().toString() : null) : null;
            String ne_addr_street_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_street") ? transmissionAntenna.prop("address").prop("cn_addr_street").value().toString() : null) : null;
            String ne_addr_building_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_building") ? transmissionAntenna.prop("address").prop("cn_addr_building").value().toString() : null) : null;
            String ne_addr_cadastral_number_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_cadastral_number") ? transmissionAntenna.prop("address").prop("cn_addr_cadastral_number").value().toString() : null) : null;
            String ne_addr_note_name = (transmissionAntenna != null && transmissionAntenna.hasProp("address") && transmissionAntenna.prop("address") != null) ? (transmissionAntenna.prop("address").hasProp("cn_addr_note") ? transmissionAntenna.prop("address").prop("cn_addr_note").value().toString() : null) : null;


//            SpinJsonNode constructionType = delegateExecution.getVariable("candidate") != null ? (candidate.hasProp("constructionType") ? JSON(candidate.prop("constructionType").value().toString()) : null) : null;
            String cn_constructionType = candidate != null ? (candidate.hasProp("constructionType") && candidate.prop("constructionType").hasProp("catalogsId") ? candidate.prop("constructionType").prop("catalogsId").value().toString() : null) : null;
            String cn_altitude = candidate != null ? (candidate.hasProp("cn_altitude") && candidate.prop("cn_altitude") != null && candidate.prop("cn_altitude").value() != null ? candidate.prop("cn_altitude").value().toString() : null) : null;

            if (cn_altitude != null && cn_altitude.indexOf(".") < 0) {
                cn_altitude = cn_altitude + ".0";
            }

            String construction_height = candidate != null ? (candidate.hasProp("cn_height_constr") ? candidate.prop("cn_height_constr").value().toString() : null) : null;
            String square = candidate != null ? (candidate.hasProp("square") ? candidate.prop("square").value().toString() : null) : null;

            if (createAssetCandidateTable.equals("cn_adresses")) {

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

                HttpPost httpPost = new HttpPost(new URI(assetsUri + "/asset-management/adresses/"));
                //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(value.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long assetsCnAddress = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

                if (assetsCnAddress != null) {
                    delegateExecution.setVariable("assetsCreatedCnAddressId", assetsCnAddress);
                } else {
                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
                }

            }

            if (createAssetCandidateTable.equals("ne_adresses")) {

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

                HttpPost httpPost = new HttpPost(new URI(assetsUri + "/asset-management/adresses/"));
                //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(value.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long assetsNeAddress = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

                if (assetsNeAddress != null) {
                    delegateExecution.setVariable("assetsCreatedNeAddressId", assetsNeAddress);
                } else {
                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
                }

            }

            if (createAssetCandidateTable.equals("facilitiesCN")) {


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
                HttpPost httpPost = new HttpPost(new URI(assetsUri + "/asset-management/facilities/"));
                //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);

                StringEntity inputData = new StringEntity(value.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long assetsCnFacilitie = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

                if (assetsCnFacilitie != null) {
                    delegateExecution.setVariable("assetsCreatedCnFacilitieId", assetsCnFacilitie);
                } else {
                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
                }

            }
            if (createAssetCandidateTable.equals("facilitiesNE")) {

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

                HttpPost httpPost = new HttpPost(new URI(assetsUri + "/asset-management/facilities/"));
                //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(value.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long assetsNeFacilitie = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

                if (assetsNeFacilitie != null) {
                    delegateExecution.setVariable("assetsCreatedNeFacilitieId", assetsNeFacilitie);
                } else {
                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
                }
            }

            if (createAssetCandidateTable.equals("powerSources")) {

                SpinJsonNode powerSource = delegateExecution.getVariable("powerSource") != null ? JSON(delegateExecution.getVariable("powerSource")) : null;

                String cableLength = powerSource != null ? (powerSource.hasProp("cableLength") ? powerSource.prop("cableLength").value().toString() : null) : null;
                Boolean provideUs3Phase = powerSource != null ? (powerSource.hasProp("provideUs3Phase") && powerSource.prop("provideUs3Phase").value().toString().equals("Yes") ? true : false) : null;
                Boolean agreeToReceiveMonthlyPaymen = powerSource != null ? (powerSource.hasProp("agreeToReceiveMonthlyPaymen") && powerSource.prop("agreeToReceiveMonthlyPaymen").value().toString().equals("Yes") ? true : false) : null;
                String res_electrical_line04 = powerSource != null ? (powerSource.hasProp("closestPublic04") ? powerSource.prop("closestPublic04").value().toString() : null) : null;
                String res_electrical_line10 = powerSource != null ? (powerSource.hasProp("closestPublic10") ? powerSource.prop("closestPublic10").value().toString() : null) : null;

//                JSONArray cable_laying_type_id_json_array = powerSource != null ? (powerSource.hasProp("cableLayingType") ? new JSONArray(powerSource.prop("cableLayingType") .toString()) : null) : null;
                SpinJsonNode cable_laying_type_id_json_array = powerSource != null ? (powerSource.hasProp("cableLayingType") ? powerSource.prop("cableLayingType") : null) : null;

                SpinList<SpinJsonNode> cable_laying_type_id_json_list = cable_laying_type_id_json_array.elements();

                String cable_laying_type_id = null;
                int index = 0;
                JSONArray cable_laying_type_id_json_ar = new JSONArray();
                for (SpinJsonNode cable_laying_type_id_json_obj : cable_laying_type_id_json_list) {
                    cable_laying_type_id = (cable_laying_type_id_json_obj != null && cable_laying_type_id_json_obj.prop("id") != null) ? cable_laying_type_id_json_obj.prop("id").value().toString() : null;
                    cable_laying_type_id_json_ar.put(cable_laying_type_id);
                    index++;
                }


                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject value = new JSONObject();

                if (cable_laying_type_id_json_ar != null && cable_laying_type_id_json_ar.length() > 0) {
                    JSONObject cable_laying_type_id_json = new JSONObject();
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
                    value.put("res_electrical_line10", res_electrical_line10);
                }

                log.info("body value.toString(): ");
                log.info(value.toString());

                HttpPost httpPost = new HttpPost(new URI(assetsUri + "/asset-management/powerSources/"));
                //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(value.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long powerSourcesId = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

                if (powerSourcesId != null) {
                    delegateExecution.setVariable("assetsCreatedPowerSourcesId", powerSourcesId);
                } else {
                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
                }
            }

            if (createAssetCandidateTable.equals("cellAntenna")) {

                SpinJsonNode cellAntenna = delegateExecution.getVariable("cellAntenna") != null ? JSON(delegateExecution.getVariable("cellAntenna")) : null;

//                String du_type_id = cellAntenna != null ? (cellAntenna.hasProp("cn_du") ? cellAntenna.prop("cn_du").value() : null) : null;

//                SpinJsonNode du_type_id_json_array = cellAntenna != null ? (cellAntenna.hasProp("cn_du") ? JSON(cellAntenna.prop("cn_du").value().toString()) : null) : null;
//                SpinList du_type_id_json_list = du_type_id_json_array.elements();
//                SpinJsonNode du_type_id_json_obj = (SpinJsonNode) du_type_id_json_list.get(0);
//                String du_type_id = (du_type_id_json_obj != null && du_type_id_json_obj.hasProp("id")) ?  du_type_id_json_obj.prop("id").value().toString() : null;

                SpinJsonNode du_type_id_json_array = cellAntenna != null ? (cellAntenna.hasProp("cn_du") ? cellAntenna.prop("cn_du") : null) : null;
                JSONArray duArray = new JSONArray();
                if (du_type_id_json_array != null && du_type_id_json_array.elements().size() > 0) {
                    SpinList<SpinJsonNode> du_type_id_json_list = du_type_id_json_array.elements();
                    for (int i = 0; i < du_type_id_json_list.size(); i++) {
                        SpinJsonNode du_type_id = du_type_id_json_list.get(i);
                        duArray.put(du_type_id.prop("catalogsId").value());
                    }
                }

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject value = new JSONObject();

                if (duArray.length() > 0) {
                    JSONObject du_type_id_json = new JSONObject();
                    du_type_id_json.put("catalog_id", 59);
                    du_type_id_json.put("ids", duArray);
                    value.put("du_types", du_type_id_json);
                }

                log.info("body value.toString(): ");
                log.info(value.toString());

                HttpPost httpPost = new HttpPost(new URI(assetsUri + "/asset-management/cellAntennaInfo/"));
                //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(value.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long cellAntennaId = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }

                if (cellAntennaId != null) {
                    delegateExecution.setVariable("assetsCreatedCellAntennaId", cellAntennaId);
                } else {
                    throw new RuntimeException("Candidate address post by not parsed assetsCreatedCnAddressId from response");
                }
            }
            if (createAssetCandidateTable.equals("site")) {
                String site_name = delegateExecution.getVariable("siteName") != null ? delegateExecution.getVariable("siteName").toString() : null;
                Long createdArtefactId = delegateExecution.getVariable("createdArtefactId") != null ? (Long) delegateExecution.getVariable("createdArtefactId") : null;
                SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject value = new JSONObject();
                if (ncpId != null) {
                    value.put("siteid", ncpId);
                    value.put("ncp_id", Long.valueOf(assetsCreatedNcp));
                }
                value.put("udb_artefact_id", createdArtefactId);
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
                if (assetsCreatedCellAntennaId != null) {
                    value.put("cell_antenna_info_id", assetsCreatedCellAntennaId);
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

                log.info("body value.toString(): ");
                log.info(value.toString());

                HttpPost httpPost = new HttpPost(new URI(assetsUri + "/asset-management/sites/"));
                //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(value.toString());
                httpPost.setEntity(inputData);
//
                CloseableHttpResponse postResponse = httpclient.execute(httpPost);
//
                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long siteId = jsonResponse.has("id") ? jsonResponse.getLong("id") : null;

                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
                }
//
                if (siteId != null) {
                    delegateExecution.setVariable("assetsCreatedSiteId", siteId);
                } else {
                    throw new RuntimeException("Candidate site post by not parsed assetsCreatedCnAddressId from response");
                }
            }
        } else {
            throw new Exception("Error");
        }
    }
}
