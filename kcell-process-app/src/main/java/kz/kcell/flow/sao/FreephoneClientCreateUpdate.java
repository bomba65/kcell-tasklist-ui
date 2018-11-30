package kz.kcell.flow.sao;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;


@Service("freephoneClientSAO")
@Log
public class FreephoneClientCreateUpdate implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;
    //Expression billingTCF;

    @Autowired
    public FreephoneClientCreateUpdate(@Value("${sao.api.url:http://sao.kcell.kz/apis}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        /*
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }
        */
        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
        Boolean saoSuccessfulResponse = true;

        String connectionType = String.valueOf(delegateExecution.getVariable("connectionType"));
        JSONArray identifierJSONArray = new JSONArray(String.valueOf(delegateExecution.getVariable("identifiers")));
        JSONObject identifierJSON = identifierJSONArray.getJSONObject(0);

        JSONArray operatorJSONArray = identifierJSON.getJSONArray("operators");
        //String title = identifierJSON.get("title").toString();

        JSONObject saoResponse = new JSONObject();

        JSONObject saoRequest = new JSONObject();

        JSONObject requestBodyJSON = new JSONObject();

        requestBodyJSON.put("fk_client", 0);
        requestBodyJSON.put("client_name", String.valueOf(delegateExecution.getVariable("officialClientCompanyName")));
        requestBodyJSON.put("bin", String.valueOf(delegateExecution.getVariable("clientBIN")));
        requestBodyJSON.put("provider", String.valueOf(delegateExecution.getVariable("provider")));
        //requestBodyJSON.put("technology", null);
        requestBodyJSON.put("type_conn", connectionType);

        //region: null,
        //voice_platform: null

        if(connectionType.equals("SIP_SBC")){
            requestBodyJSON.put("ip", String.valueOf(delegateExecution.getVariable("ipNumber")));
        } else if(connectionType.equals("transmit")){
            requestBodyJSON.put("forward_num", String.valueOf(delegateExecution.getVariable("transmitNumber")));
        } else if(connectionType.equals("E1")){
            requestBodyJSON.put("term_point", String.valueOf(delegateExecution.getVariable("terminationPoint")));
            requestBodyJSON.put("hope_point", String.valueOf(delegateExecution.getVariable("hopPoint")));
            requestBodyJSON.put("site_name", String.valueOf(delegateExecution.getVariable("siteName")));
            requestBodyJSON.put("port", String.valueOf(delegateExecution.getVariable("lastMilePort")));

            requestBodyJSON.put("exchange", String.valueOf(delegateExecution.getVariable("EXCHANGE")));
            requestBodyJSON.put("mgw", String.valueOf(delegateExecution.getVariable("MGW")));
            requestBodyJSON.put("rtdma", String.valueOf(delegateExecution.getVariable("RTDMA")));
            requestBodyJSON.put("klm", String.valueOf(delegateExecution.getVariable("channelKLM")));

            //$scope.freephoneClientResponse.region = $scope.;
            //$scope.freephoneClientResponse.voice_platform = $scope.;
            //$scope.freephoneClientResponse. = $scope.trNodeIP;
        }

        if (isSftp) {

            for(int n = 0; n < operatorJSONArray.length(); n++) {
                JSONObject operatorJSON = operatorJSONArray.getJSONObject(n);

                requestBodyJSON.put("report", operatorJSON.get("name").toString());
                saoRequest.put(operatorJSON.get("name").toString(), requestBodyJSON);

                // write to process variables response from sao - freephoneClientResponse - must include info about invoking rest api for each operator


                StringEntity freephoneClientData = new StringEntity(requestBodyJSON.toString(), ContentType.APPLICATION_JSON);

                HttpPost freephoneClientPost = new HttpPost(new URI(baseUri+"/FreephoneClientCreateUpdate"));
                freephoneClientPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                freephoneClientPost.setEntity(freephoneClientData);

                HttpClient freephoneClientHttpClient = HttpClients.createDefault();
                HttpResponse freephoneClientResponse = freephoneClientHttpClient.execute(freephoneClientPost);
                HttpEntity entity = freephoneClientResponse.getEntity();

                String responseString = EntityUtils.toString(entity, "UTF-8");

                // What is the format of returned response from POST method?
                JSONObject responseJSON = new JSONObject(responseString);

                if(responseJSON.has("fk_client")) {
                    long fk_client = (long) responseJSON.get("fk_client");
                    if(fk_client > 0) {
                        saoResponse.put(operatorJSON.get("name").toString(), responseJSON);
                    } else {
                        saoSuccessfulResponse = false;
                    }
                }
            }

            JsonValue saoRequestJsonValue = SpinValues.jsonValue(saoRequest.toString()).create();
            delegateExecution.setVariable("saoRequest", saoRequestJsonValue);

            JsonValue saoResponseJsonValue = SpinValues.jsonValue(saoResponse.toString()).create();
            delegateExecution.setVariable("saoResponse", saoResponseJsonValue);

            delegateExecution.setVariable("saoSuccessfulResponse", saoSuccessfulResponse);

            // cast variables to number according to number type fields in confirmLAstMileFinishConstruction.html, fillConnectionInformation.html, createApplicationToConnection.html
            // add specific data to requestBodyJSON according to DP-147

        } else {
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
        }
    }
}
