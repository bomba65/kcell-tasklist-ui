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
@Service("assetCreateNCP")
public class CreateNCP implements JavaDelegate {

    private Minio minioClient;
    private String baseUri;
    private String assetsUri;

    @Autowired
    DataSource dataSource;

    @Autowired
    public CreateNCP(Minio minioClient, @Value("${mail.message.baseurl:http://localhost}") String baseUri, @Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUri) {
        this.minioClient = minioClient;
        this.baseUri = baseUri;
        this.assetsUri = assetsUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
//        String createAssetCandidateTable = delegateExecution.getVariable("createAssetCandidateTable") != null ? delegateExecution.getVariable("createAssetCandidateTable").toString() : null;
//
//        log.info("createAssetCandidateTable>>>>>");
//        log.info(createAssetCandidateTable);
//        log.info("<<<<<createAssetCandidateTable");
//        if (createAssetCandidateTable != null) {
//            createAssetCandidateTable = createAssetCandidateTable.replaceAll("\"", "");

            String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
            String longitude = delegateExecution.getVariable("longitude") != null ? delegateExecution.getVariable("longitude").toString() : null;
            String latitude = delegateExecution.getVariable("latitude") != null ? delegateExecution.getVariable("latitude").toString() : null;
            String region = delegateExecution.getVariable("regionCatalogId") != null ? delegateExecution.getVariable("regionCatalogId").toString() : null;

            SpinJsonNode initiatorJson = delegateExecution.getVariable("initiator") != null ? JSON(delegateExecution.getVariable("initiator")) : null;
            SpinJsonNode reasonJson = delegateExecution.getVariable("reason") != null ? JSON(delegateExecution.getVariable("reason")) : null;
            SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;
            SpinJsonNode projectJson = delegateExecution.getVariable("project") != null ? JSON(delegateExecution.getVariable("project")) : null;


            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();

            JSONObject newNCP = new JSONObject();
            JSONObject value = new JSONObject();


            JSONObject regionJson = new JSONObject();
            regionJson.put("catalog_id", 5);
            regionJson.put("id", region);

            //            initiator_id
            JSONObject initiator_id_json = new JSONObject();
            initiator_id_json.put("catalog_id", 7);
            initiator_id_json.put("id", initiatorJson.prop("catalogsId").value().toString());

            //            project_id
            if(projectJson.hasProp("assetId") && projectJson.prop("assetId").value()!=null){
                JSONObject project_id_json = new JSONObject();
                project_id_json.put("catalog_id", 8);
                project_id_json.put("id", projectJson.prop("assetId").value().toString());

                value.put("project_id", project_id_json);
            }

            //            reason_id
            JSONObject reason_id_json = new JSONObject();
            reason_id_json.put("catalog_id", 9);
            reason_id_json.put("id", reasonJson.prop("assetsId").value().toString());

            //            site_type_id
            JSONObject site_type_id_json = new JSONObject();
            site_type_id_json.put("catalog_id",2);
            site_type_id_json.put("id", siteTypeJson.prop("assetsId").value().toString());

            value.put("ncp_number", ncpId);
            value.put("region_id", regionJson);
            value.put("latitude", "N " + latitude.replace(".", ","));
            value.put("longitude", "E " + longitude.replace(".", ","));
            value.put("initiator_id", initiator_id_json);
            value.put("reason_id", reason_id_json);
            value.put("site_type_id", site_type_id_json);


            HttpPost httpPost = new HttpPost(new URI("https://asset.test-flow.kcell.kz/asset-management/ncp/"));
//            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.addHeader("Referer", baseUri);
            StringEntity inputData = new StringEntity(value.toString());
            httpPost.setEntity(inputData);

            CloseableHttpResponse postResponse = httpclient.execute(httpPost);

            HttpEntity entity = postResponse.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject jsonResponse = new JSONObject(responseString);
            Long assetsCreatedNcp = jsonResponse.has("id") ?  jsonResponse.getLong("id") : null;

            if (assetsCreatedNcp != null) {
                delegateExecution.setVariable("assetsCreatedNcp", assetsCreatedNcp);
            } else {
                throw new RuntimeException("NCP post by id " + ncpId + "not parsed assetsCreatedNcpId from response");
            }

            log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
            if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException("NCP post by id " + ncpId + " returns code " + postResponse.getStatusLine().getStatusCode());
            }
//        } else {
//            throw new Exception("Error");
//        }
    }
}
