package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service("createSMSGWClient")
@Log
public class CreateSMSGWClient implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;
    private final String username;
    private final String pwd;


    @Autowired
    public CreateSMSGWClient(@Value("https://admin-api-hermes-stage.kcell.kz") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd) {
        this.baseUri = baseUri;
        this.username = username;
        this.pwd = pwd;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        System.out.println("isSftp: " + isSftp);

        log.info("basUri: " + baseUri);
        String connectionType = String.valueOf(delegateExecution.getVariable("connectionType"));
        String identifierType = String.valueOf(delegateExecution.getVariable("identifierType"));
        log.info("connectionType: "  + connectionType + " , identifierType: " + identifierType );

        if ("rest".equals(connectionType) && "alfanumeric".equals(identifierType)) {
            log.info("alfanumeric $ rest");

            StringBuilder responseUsersRequest = new StringBuilder();
//            Authenticator.setDefault(new Authenticator() {
//                @Override
//                public PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication("kcell.kz\\" + username, pwd.toCharArray());
//                }
//            });

            URL getUsersRequest = new URL(baseUri + "/users/");
            HttpURLConnection conn = (HttpURLConnection) getUsersRequest.openConnection();

            log.info("connection");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json;odata=verbose");

            log.info("connection");

            InputStream stream = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String str = "";
            while ((str = in.readLine()) != null) {
                responseUsersRequest.append(str);
            }
            in.close();

            log.info("responseGetUsersRequest :  " + responseUsersRequest);
            JSONArray responseSharepointJSON = new JSONArray(responseUsersRequest);
            delegateExecution.setVariable("responseGetUsers", responseUsersRequest.toString());

            String clientBIN = String.valueOf(delegateExecution.getVariable("clientBIN"));

            boolean userAlreadyExists = false;

            for (int i = 0; i < responseSharepointJSON.length(); i++) {
                if (((JSONObject) responseSharepointJSON.get(i)).get("bin").equals(clientBIN)) {
                    userAlreadyExists = true;
                    break;
                }
            }

            if (!userAlreadyExists) {
                String comments = String.valueOf(delegateExecution.getVariable(""));
                String clientCompanyLatName = String.valueOf(delegateExecution.getVariable("clientCompanyLatName"));
                String smsGwUsername = "REST_" + clientCompanyLatName;
                String smsGwPasswd = "123456789";

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("bin", clientBIN));
                params.add(new BasicNameValuePair("comments", comments));
                params.add(new BasicNameValuePair("isActive", "true"));
                params.add(new BasicNameValuePair("isReadOnly", "false"));
                params.add(new BasicNameValuePair("password", smsGwPasswd));
                params.add(new BasicNameValuePair("userId", "0"));
                params.add(new BasicNameValuePair("username", smsGwUsername));

                CloseableHttpClient client = HttpClients.createDefault();
                HttpPut httpPost = new HttpPut(new URI(baseUri+"/users/"));
                httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                CloseableHttpResponse response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                delegateExecution.setVariable("putUserResponse", responseString);
                delegateExecution.setVariable("smsGwUsername", smsGwUsername);
                delegateExecution.setVariable("smsGwPasswd", smsGwPasswd);

            }



        }

    }


}
