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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-management")
@Log
public class AssetsController {

    private final String assetUrl;

    @Autowired
    private IdentityService identityService;

    private AssetsController(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetUrl) {
        this.assetUrl = assetUrl;
    }

    @RequestMapping(value = "/bcs_rnc", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<String> getCatalog() throws Exception {

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpGet httpGet = new HttpGet(assetUrl + "/asset-management/bcs_rnc");
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(assetUrl + " " + httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }

        return ResponseEntity.ok(content);
    }

}
