package kz.kcell.flow.catalogs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/catalogs")
@Log
public class CatalogController {

    private final String catalogsUrl;

    @Autowired
    private IdentityService identityService;

    private CatalogController(@Value("${catalogs.url:https://catalogs.test-flow.kcell.kz}") String catalogsUrl) {
        this.catalogsUrl = catalogsUrl;
    }

    @RequestMapping(value = "/api/get/{processId}/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalog(@PathVariable("processId") String processId, @PathVariable("id") Long id) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(catalogsUrl + "/camunda/catalogs/api/get/" + processId + "/" + id);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/get/{processId}/name/{name}", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalog(@PathVariable("processId") String processId, @PathVariable("name") String name) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(catalogsUrl + "/camunda/catalogs/api/get/" + processId + "/name/" + name);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/get/id/{id}", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalog(@PathVariable("id") Long id) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(catalogsUrl + "/camunda/catalogs/api/get/id/" + id);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/get/id/{id}/parentId/{parentId}", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalog(@PathVariable("id") Long id, @PathVariable("parentId") Long parentId, HttpServletRequest request) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(catalogsUrl + "/camunda/catalogs/api/get/id/" + id + "/parentId/" + parentId);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/getnames/{processId}", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalogsNames(@PathVariable("processId") String processId, HttpServletRequest request) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(catalogsUrl + "/camunda/catalogs/api/getnames/" + processId);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/create", method = RequestMethod.POST, consumes = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> createCatalog(@RequestBody String stringBody) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httpPost = new HttpPost(catalogsUrl + "/camunda/catalogs/api/create");
        StringEntity stringEntity = new StringEntity(stringBody);
        httpPost.setEntity(stringEntity);
        HttpResponse httpResponse = httpclient.execute(httpPost);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/getfull/{processId}", method = RequestMethod.POST, consumes = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalogWithValues(@PathVariable("processId") String processId) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httpPost = new HttpPost(catalogsUrl + "/camunda/catalogs/api/getfull/" + processId);
        HttpResponse httpResponse = httpclient.execute(httpPost);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/getfull/catalogids", method = RequestMethod.POST, consumes = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalogByIdsWithValues(@RequestBody List<Long> catalogids) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httpPost = new HttpPost(catalogsUrl + "/camunda/catalogs/api/getfull/catalogids");
        StringEntity stringEntity = new StringEntity(new ObjectMapper().writeValueAsString(catalogids), ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        HttpResponse httpResponse = httpclient.execute(httpPost);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/get/rolloutcatalogids", method = RequestMethod.POST, consumes = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getLeasingCatalogByIdsWithValues(@RequestBody List<Long> catalogids) throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httpPost = new HttpPost(catalogsUrl + "/camunda/catalogs/api/get/rolloutcatalogids");
        StringEntity stringEntity = new StringEntity(new ObjectMapper().writeValueAsString(catalogids), ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse httpResponse = httpclient.execute(httpPost);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(catalogsUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }
}
