package kz.kcell.flow.demand;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.msgpack.util.json.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

@Service("CreateBusinessKey")
@lombok.extern.java.Log

public class CreateBusinessKey implements JavaDelegate {

    private final String baseUri;

    public @interface BusinessKey {
        boolean caseSensitive() default true;
    }

    @Autowired
    public CreateBusinessKey(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Autowired
    HistoryService historyService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Long processInstancesCount = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("Demand").count();
        log.info("" + processInstancesCount);

        String businessKey = "DMAOP/" + processInstancesCount;
        log.info("businessKey: " + businessKey);
        delegateExecution.setVariable("businessKey", businessKey);
    }


}
