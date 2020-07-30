package kz.kcell.flow.leasing;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("GenerateContractId")
public class GenerateContractId implements JavaDelegate {

    private final String baseUri;

    @Autowired
    public GenerateContractId(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String _CONTRACT_APPROVAL_TYPE = delegateExecution.hasVariableLocal("_CONTRACT_APPROVAL_TYPE") ? delegateExecution.getVariable("_CONTRACT_APPROVAL_TYPE").toString() : null;
        // check FE or CN: _CONTRACT_APPROVAL_TYPE.equals("CN")
        String contractVariableName =  _CONTRACT_APPROVAL_TYPE.equals("CN")  ? "contractInformations" : "contractInformationsFE";

        SpinJsonNode contractInformationsJSON = JSON(delegateExecution.getVariable(contractVariableName));
        SpinList contractInformations = contractInformationsJSON.elements();

        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Integer year = calendar.get(Calendar.YEAR);

        String lastTwo = null;
        if (year != null) {
            lastTwo = year.toString().substring(year.toString().length() - 2);
        }

        String rolloutContractCounter = "ROLLOUT-CONTRACT-" + lastTwo;
        String contractsForSearch = ",";

        for (int j=0; j<contractInformations.size(); j++) {
            SpinJsonNode ci = (SpinJsonNode) contractInformations.get(j);
            if (ci != null && ci.hasProp("ct_acquisitionType") && ci.prop("ct_acquisitionType").value().toString() != "additionalAgreement") {
                String contractCounterId = setContractCounter(rolloutContractCounter);
//                log.info("contractCounterId:" + contractCounterId);
                String powerPrefix = ci.hasProp("ct_contract_type") && ci.prop("ct_contract_type").value().toString().equals("2") ? "EC" : "AS";
//                log.info("powerPrefix: " + powerPrefix);
//                log.info("contractCounterId:" + contractCounterId);

                String contract_id = powerPrefix + '-' + contractCounterId + '-' + lastTwo;
                ci.prop("ct_contractid", contract_id);
//                log.info("contract_id:" + contractCounterId);
                contractsForSearch += contract_id + ",";
            }
        }
        log.info("contractsForSearch: " + contractsForSearch);

        delegateExecution.setVariable("contractsForSearch", contractsForSearch);
        delegateExecution.setVariable(contractVariableName, contractInformationsJSON);

    }

    private String setContractCounter(String CounterName) throws Exception{
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        String locationUrl = baseUri + "/asset-management/rolloutcounter/" + CounterName;
//        StringEntity locationInputData = new StringEntity("");

        HttpPost locationHttpPost = new HttpPost(new URI(locationUrl));
        locationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        locationHttpPost.addHeader("Referer", baseUri);
//        locationHttpPost.setEntity(locationInputData);

        CloseableHttpResponse locationResponse = httpclient.execute(locationHttpPost);
        log.info("locationResponse code: " + locationResponse.getStatusLine().getStatusCode());

        HttpEntity entity = locationResponse.getEntity();
        String contractCounterResponseString = EntityUtils.toString(entity, "UTF-8").replace("\"", "");

//        EntityUtils.consume(locationResponse.getEntity());
//        String contractCounterResponse = EntityUtils.toString(locationResponse.getEntity(), "UTF-8");

        httpclient.close();
        log.info("contractCounterResponse: " + contractCounterResponseString);

        return contractCounterResponseString;
    }
}


