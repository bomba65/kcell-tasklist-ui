package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.util.Base64;

@Log
@Service("GetTraceNumber")
public class GetTraceNumber implements JavaDelegate {

    private static final String URL_ENDING = "/subscriptions/traceNumber";

    @Value("${atlas.subscribers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        UriBuilder uriBuilder = UriBuilder.fromPath(atlasUrl + URL_ENDING);

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", "Basic " + encoding);
        HttpResponse response = httpclient.execute(httpGet);

        if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.info("GetTraceNumber returns code " + response.getStatusLine().getStatusCode() + "\nMessage: " + EntityUtils.toString(response.getEntity()));
        } else {
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            delegateExecution.setVariable("traceNumber", content);
        }
    }
}
