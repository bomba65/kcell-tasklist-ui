package kz.kcell.flow.sao;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service("freephoneClientSAO")
@Log
public class FreephoneClientCreateUpdate implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;

    @Autowired
    public FreephoneClientCreateUpdate(@Value("${sao.api.url:http://sao.kcell.kz/apis}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
        Boolean saoSuccessfulResponse = true;

        String connectionType = String.valueOf(delegateExecution.getVariable("connectionType"));
        JSONArray identifierJSONArray = new JSONArray(String.valueOf(delegateExecution.getVariable("identifiers")));
        JSONObject identifierJSON = identifierJSONArray.getJSONObject(0);

        JSONArray operatorJSONArray = identifierJSON.getJSONArray("operators");
        String title = String.valueOf(identifierJSON.get("title"));
        String operatorType =  String.valueOf(delegateExecution.getVariable("operatorType"));

        JSONObject saoResponse = new JSONObject();
        JSONObject saoRequest = new JSONObject();
        JSONObject requestBodyJSON = new JSONObject();

        if (isSftp) {
            System.out.println("requestBodyJSON: " + requestBodyJSON.toString() + ", operators: " + operatorJSONArray.toString());

            for(int n = 0; n < operatorJSONArray.length(); n++) {

                JSONObject operatorJSON = operatorJSONArray.getJSONObject(n);

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("report", operatorJSON.get("name").toString()));
                params.add(new BasicNameValuePair("fk_client", "0"));
                params.add(new BasicNameValuePair("client_name", String.valueOf(delegateExecution.getVariable("officialClientCompanyName"))));
                params.add(new BasicNameValuePair("bin", String.valueOf(delegateExecution.getVariable("clientBIN"))));
                params.add(new BasicNameValuePair("technology", "IVR"));
                params.add(new BasicNameValuePair("short_number", title));
                //params.add(new BasicNameValuePair("type_conn", connectionType));

                if("onnet".equals(operatorType)){
                    params.add(new BasicNameValuePair("provider", "Kcell"));
                } else {
                    if("Kcell".equals(operatorJSON.get("name").toString())) {
                        params.add(new BasicNameValuePair("provider", "Kcell"));
                    } else {
                        String provider = String.valueOf(delegateExecution.getVariable("provider"));
                        if("SMS Consult".equals(provider)){
                            provider = "SMS-Consult";
                        }
                        params.add(new BasicNameValuePair("provider", provider));
                    }
                }

                if("SIP_SBC".equals(connectionType)){
                    params.add(new BasicNameValuePair("type_conn", connectionType));
                    params.add(new BasicNameValuePair("ip", String.valueOf(delegateExecution.getVariable("ipNumber"))));
                } else if("transmit".equals(connectionType)){
                    params.add(new BasicNameValuePair("type_conn", "Call forwarding"));
                    params.add(new BasicNameValuePair("forward_num", String.valueOf(delegateExecution.getVariable("transmitNumber"))));
                } else if("E1".equals(connectionType)){
                    params.add(new BasicNameValuePair("type_conn", connectionType));
                    params.add(new BasicNameValuePair("term_point", String.valueOf(delegateExecution.getVariable("terminationPoint"))));
                    params.add(new BasicNameValuePair("hope_point", String.valueOf(delegateExecution.getVariable("hopPoint"))));
                    params.add(new BasicNameValuePair("site_name", String.valueOf(delegateExecution.getVariable("siteName"))));
                    params.add(new BasicNameValuePair("port", String.valueOf(delegateExecution.getVariable("lastMilePort"))));
                    params.add(new BasicNameValuePair("exchange", String.valueOf(delegateExecution.getVariable("EXCHANGE"))));
                    params.add(new BasicNameValuePair("mgw", String.valueOf(delegateExecution.getVariable("MGW"))));
                    params.add(new BasicNameValuePair("rtdma", String.valueOf(delegateExecution.getVariable("RTDMA"))));
                    params.add(new BasicNameValuePair("klm", String.valueOf(delegateExecution.getVariable("channelKLM"))));
                }
                saoRequest.put(operatorJSON.get("name").toString(), params.toString());

                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(new URI(baseUri+"/FreephoneClientCreateUpdate"));

                httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                CloseableHttpResponse response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                JSONObject responseJSON = new JSONObject(responseString);
                saoResponse.put(operatorJSON.get("name").toString(), responseJSON);
                if(responseJSON.has("success")) {
                    Integer responseStatusCode = (Integer) responseJSON.get("success");
                    if(responseStatusCode != 1) {
                        saoSuccessfulResponse = false;
                    }
                } else {
                    saoSuccessfulResponse = false;
                }

                client.close();
            }
            JsonValue saoRequestJsonValue = SpinValues.jsonValue(saoRequest.toString()).create();
            delegateExecution.setVariable("saoRequest", saoRequestJsonValue);

            JsonValue saoResponseJsonValue = SpinValues.jsonValue(saoResponse.toString()).create();
            delegateExecution.setVariable("saoResponse", saoResponseJsonValue);

            delegateExecution.setVariable("saoSuccessfulResponse", saoSuccessfulResponse);

        } else {
            System.out.println("requestBodyJSON: " + requestBodyJSON.toString() + ", operators: " + operatorJSONArray.toString());
            for(int n = 0; n < operatorJSONArray.length(); n++) {
                JSONObject operatorJSON = operatorJSONArray.getJSONObject(n);

                requestBodyJSON.put("report", operatorJSON.get("name").toString());
                saoRequest.put(operatorJSON.get("name").toString(), requestBodyJSON);

                JSONObject responseJSON = new JSONObject();
                responseJSON.put("fk_client", 1001+n);
                saoResponse.put(operatorJSON.get("name").toString(), responseJSON);
            }
            JsonValue saoRequestJsonValue = SpinValues.jsonValue(saoRequest.toString()).create();
            delegateExecution.setVariable("saoRequest", saoRequestJsonValue);

            JsonValue saoResponseJsonValue = SpinValues.jsonValue(saoResponse.toString()).create();
            delegateExecution.setVariable("saoResponse", saoResponseJsonValue);

            delegateExecution.setVariable("saoSuccessfulResponse", saoSuccessfulResponse);

            System.out.println("saoRequest: " + saoRequest.toString());
            System.out.println("saoResponse: " + saoResponse.toString());
            System.out.println("saoSuccessfulResponse: " + saoSuccessfulResponse);
        }
    }
}
