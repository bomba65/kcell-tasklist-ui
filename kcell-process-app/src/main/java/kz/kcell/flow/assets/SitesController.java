package kz.kcell.flow.assets;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

@RestController
@RequestMapping("/sites")
@Log
public class SitesController {

    private final String assetsUrl;
    private final String assetsLeasingUrl;

    @Autowired
    private IdentityService identityService;

    private SitesController(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUrl, @Value("${asset.leasing_url:https://asset.flow.kcell.kz}") String assetsLeasingUrl) {
        this.assetsUrl = assetsUrl;
        this.assetsLeasingUrl = assetsLeasingUrl;
    }

    @RequestMapping(value = "/name/contains/{name}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> siteContainsName(@PathVariable("name") String name, @RequestParam(required = false) Boolean forRollout) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        String baseUrl = assetsUrl;

        if (forRollout != null && forRollout) {
            baseUrl = assetsLeasingUrl;
//            log.info("assets baseUrl for search by name: " + (forRollout ? "assetsLeasingUrl" : "assetsUrl") );
        }

        HttpGet httpGet = new HttpGet(baseUrl + "/asset-management/sites/name/contains/" + name);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(baseUrl + " site contains name = " + name + " returns code " + httpResponse.getStatusLine().getStatusCode());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> siteGetById(@PathVariable("id") Long id) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(assetsUrl + "/asset-management/sites/id/" + id);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(assetsUrl + " site get by id = " + id + " returns code " + httpResponse.getStatusLine().getStatusCode());
        }

        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/siteid/{siteid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> siteGetBySiteid(@PathVariable("siteid") String siteid) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(assetsUrl + "/asset-management/sites/siteid/" + siteid);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(assetsUrl + " site get by siteid = " + siteid + " returns code " + httpResponse.getStatusLine().getStatusCode());
        }

        return ResponseEntity.ok(content);
    }
}
