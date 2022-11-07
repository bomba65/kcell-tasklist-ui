package kz.kcell.flow.assets;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobrequestcounter")
@Log
public class JobRequestCounterController {

    private final String assetsUrl;

    private JobRequestCounterController(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUrl) {
        this.assetsUrl = assetsUrl;
    }

    @RequestMapping(value = "/{counterId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> getNextCounter(@RequestHeader(value = HttpHeaders.REFERER, required = false) String referer,
                                                 @PathVariable("counterId") String counterId) throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        String baseUrl = assetsUrl;

        HttpPost httpPost = new HttpPost(baseUrl + "/asset-management/jobrequestcounter/" + counterId);
        // To bypass nginx http_referer rule for /asset-management/jobrequestcounter/
        httpPost.addHeader(HttpHeaders.REFERER, referer);
        HttpResponse httpResponse = httpclient.execute(httpPost);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException(baseUrl + " next counter with id = " + counterId + " returns code " + httpResponse.getStatusLine().getStatusCode());
        }

        return ResponseEntity.ok(content);
    }
}
