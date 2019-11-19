package kz.kcell.flow.sharepoint;


import lombok.extern.java.Log;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Service("pbxGetTCFForm")
@Log
public class PBXGetTCFForm implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;
    private final String username;
    private final String pwd;
    private final String productCatalogUrl;
    private final String productCatalogAuth;

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;

    @Autowired
    public PBXGetTCFForm(@Value("${sharepoint.forms.url:https://sp.kcell.kz/forms/_api}") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd,
                         @Value("${product.catalog.url:http://ldb-al-preprod.kcell.kz}") String productCatalogUrl, @Value("${product.catalog.auth:app.camunda.user:Asd123Qwerty!}") String productCatalogAuth) {
        this.baseUri = baseUri;
        this.username = username;
        this.pwd = pwd;
        this.productCatalogUrl = productCatalogUrl;
        this.productCatalogAuth = productCatalogAuth;
    }

    private static String postAuthenticatedResponse(
        final String urlStr, final String domain,
        final String userName, final String password) throws IOException {


        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        String jsonRequestBody = "{}";
        URL urlRequest = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Accept", "application/json;odata=verbose");
        conn.setRequestProperty("Content-Type", "application/json;odata=verbose");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStream os = conn.getOutputStream();
        os.write(jsonRequestBody.getBytes("UTF-8"));
        os.close();

        // read the response
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
        JSONObject jsonObject = new JSONObject(result);


        in.close();
        conn.disconnect();

        return jsonObject.toString();
    }

    private static String postItemsResponse(
        final String urlStr, final String domain,
        final String userName, final String password,
        final String formDigestValueStr, final String requestBodyStr) throws IOException {

        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        System.out.println("postItemsResponse URL: " + urlStr);
        System.out.println("postItemsResponse BODY: " + requestBodyStr);

        URL urlRequest = new URL(urlStr);


        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("X-requestDigest", formDigestValueStr);
        conn.setRequestProperty("Accept", "application/json;odata=verbose");
        conn.setRequestProperty("Content-Type", "application/json;odata=verbose");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStream os = conn.getOutputStream();
        os.write(requestBodyStr.getBytes("UTF-8"));
        os.close();

        // read the response
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
        System.out.println("postItemsResponse RESPONSE: " + result);
        JSONObject jsonObject = new JSONObject(result);

        in.close();
        conn.disconnect();

        return jsonObject.toString();
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
            System.out.println("isSftp:");
            System.out.println(isSftp);
            String[] rejectStatusByTCF = {"Canceled", "Rejected by Department Manager", "Rejected by FD-BRS-Expert", "Rejected by FD-IBS-IT Supervisor", "Rejected by ICTD-SNS-BCOU-BCST Supervisor", "Rejected by ICTD Specialists"};
            //isSftp = true;
            if (isSftp) {

                String processKey = repositoryService.createProcessDefinitionQuery().processDefinitionId(delegateExecution.getProcessDefinitionId()).list().get(0).getKey();
                System.err.println("processKey: " + processKey);

                String tcfFormId = delegateExecution.getVariable("tcfFormId").toString();


                StringBuilder response = new StringBuilder();

                Authenticator.setDefault(new Authenticator() {

                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            "kcell.kz\\" + username, pwd.toCharArray());
                    }
                });

                URL urlRequest = new URL(baseUri + "/Lists/getbytitle('TCF_test')/items(" + tcfFormId + ")");
                System.out.println("URL:" + baseUri + "/Lists/getbytitle('TCF_test')/items(" + tcfFormId + ")");
                HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json;odata=verbose");

                InputStream stream = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(stream));
                String str = "";
                while ((str = in.readLine()) != null) {
                    response.append(str);
                }
                in.close();

                delegateExecution.setVariable("responseBodyTCF", response.toString());

                JSONObject responseSharepointJSON = new JSONObject(response.toString());
                JSONObject tcf = responseSharepointJSON.getJSONObject("d");

                String Status = tcf.get("Status").toString();

                System.out.println("STATUS");
                System.out.println("STATUS");
                System.out.println(Status);
                System.out.println("STATUS");
                System.out.println("STATUS");

                if ("Completed".equals(Status)) {
                    delegateExecution.setVariable("tcfIdReceived", true);
                    delegateExecution.setVariable("rejectedFromTCF", false);
                } else if ("Rejected by ICTD Specialists".equals(Status)) {
                    delegateExecution.setVariable("tcfIdReceived", true);
                    delegateExecution.setVariable("rejectedFromTCF", true);
                } else {
                    delegateExecution.setVariable("tcfIdReceived", false);
                    //delegateExecution.setVariable("rejectedFromTCF", false);
                }
            } else {
                delegateExecution.setVariable("tcfIdReceived", true);
                delegateExecution.setVariable("rejectedFromTCF", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            delegateExecution.setVariable("tcfIdReceived", false);
            delegateExecution.setVariable("rejectedFromTCF", true);
        }
    }
}
