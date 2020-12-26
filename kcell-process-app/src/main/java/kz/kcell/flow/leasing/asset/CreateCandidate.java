package kz.kcell.flow.leasing.asset;

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
import org.camunda.spin.json.SpinJsonNode;
import org.json.JSONObject;
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

            if (createAssetCandidateTable.equals("adresses")) {

//                String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
//                String longitude = delegateExecution.getVariable("longitude") != null ? delegateExecution.getVariable("longitude").toString() : null;
//                String latitude = delegateExecution.getVariable("latitude") != null ? delegateExecution.getVariable("latitude").toString() : null;
                String region = delegateExecution.getVariable("regionCatalogId") != null ? delegateExecution.getVariable("regionCatalogId").toString() : null;

                SpinJsonNode addressJson = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
                SpinJsonNode cellAntennaJson = delegateExecution.getVariable("cellAntenna") != null ? JSON(delegateExecution.getVariable("cellAntenna")) : null;
                SpinJsonNode farEndInformation = delegateExecution.getVariable("farEndInformation") != null ? JSON(delegateExecution.getVariable("farEndInformation")) : null;

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
                if (cn_city_catalogs_id != null ) {
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

                HttpPost httpPost = new HttpPost(new URI("https://asset.test-flow.kcell.kz/asset-management/adresses/"));
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
        } else {
            throw new Exception("Error");
        }
    }
}
