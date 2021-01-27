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


@RestController
@RequestMapping("/tsd_mw")
@Log
public class TsdMWController {

    private final String assetsUrl;

    @Autowired
    private IdentityService identityService;

    private TsdMWController(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUrl){
        this.assetsUrl = assetsUrl;
    }
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> tsdMWNearendId(@RequestParam(value = "nearend_id", required = false) Long nearend_id, @RequestParam(value = "farend_id", required = false) Long farend_id) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        String req = "";
        if (nearend_id != null) {
            req = "nearend_id=" + nearend_id;
        }
        if (farend_id != null) {
            req = "farend_id=" + farend_id;
        }
        HttpGet httpGet = new HttpGet(assetsUrl + "/asset-management/tsd_mw?" + req);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(assetsUrl + "tsd_mw with " + req + "returns code " + httpResponse.getStatusLine().getStatusCode());
        }



        return ResponseEntity.ok(content);
    }
}
