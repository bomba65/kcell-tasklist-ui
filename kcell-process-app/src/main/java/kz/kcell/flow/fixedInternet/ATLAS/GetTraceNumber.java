package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.util.Base64;

@Slf4j
@Service("GetTraceNumber")
public class GetTraceNumber implements JavaDelegate {

    private static final String URL_ENDING = "/subscriptions/traceNumber";

    @Value("${atlas.subscribers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        UriBuilder uriBuilder = UriBuilder.fromPath(atlasUrl + URL_ENDING);

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", "Basic " + encoding);
        HttpResponse response = httpClientWithoutSSL.execute(httpGet);

        if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.error("GetTraceNumber, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode() + "\n" +
                "Error message: " + EntityUtils.toString(response.getEntity()));
        } else {
            log.info("GetTraceNumber, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            delegateExecution.setVariable("traceNumber", content);
        }
    }
}
