package kz.kcell.flow.sao;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/sao")
@Log
public class SaoController {

    private final String saoApiUrl;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private Environment environment;

    @Autowired
    public SaoController(@Value("${sao.api.url:http://sao.kcell.kz/apis}") String saoApiUrl) {
        this.saoApiUrl = saoApiUrl;
    }


    @RequestMapping(value = "/apis/FreephoneClientCreateUpdate", method = RequestMethod.POST, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> FreephoneClientCreateUpdate(@RequestBody String saoRequestBody) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }
        // Add check for auth in SAO

        if (isSftp) {
            try {
                System.out.println("saoRequestBody: " + saoRequestBody);
                //String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                JSONObject saoRequestBodyJSON = new JSONObject(saoRequestBody);
                String fkClient = String.valueOf(saoRequestBodyJSON.get("fk_client"));
                StringEntity freephoneClientData = new StringEntity(saoRequestBody, "UTF-8");
                /*
                ArrayList<NameValuePair> postParameters;
                postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("fk_client", fkClient));
                */
                HttpPost freephoneClientPost = new HttpPost(new URI(saoApiUrl+"/FreephoneClientCreateUpdate"));
                List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                params.add(new BasicNameValuePair("fk_client", fkClient));
                freephoneClientPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


                //HttpPost freephoneClientPost = new HttpPost(new URI(saoApiUrl+"/FreephoneClientCreateUpdate?fk_client="+fkClient));

                //freephoneClientPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));



                //freephoneClientPost.setHeader("Authorization", "Basic " + encoding);
                freephoneClientPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                freephoneClientPost.setEntity(freephoneClientData);

                HttpClient freephoneClientHttpClient = HttpClients.createDefault();
                HttpResponse freephoneClientResponse = freephoneClientHttpClient.execute(freephoneClientPost);

                HttpEntity entity = freephoneClientResponse.getEntity();
                String content = EntityUtils.toString(entity);

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            JSONObject saoTestResponseJSON = new JSONObject(saoRequestBody);
            if(saoTestResponseJSON.getInt("fk_client") == 0) {
                saoTestResponseJSON.put("fk_client", 12345);
            }
            return ResponseEntity.ok(saoTestResponseJSON.toString());
            //return ResponseEntity.ok(saoRequestBody);
        }
        return null;
    }

    @RequestMapping(value = "/apis/FreephoneClientUpdate", method = RequestMethod.POST, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> freephoneClientUpdate(@RequestBody String saoRequestBody) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }
        // Add check for auth in SAO

        if (isSftp) {
            try {
                //String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                StringEntity freephoneClientData = new StringEntity(saoRequestBody, ContentType.APPLICATION_JSON);
                HttpPost freephoneClientPost = new HttpPost(new URI(saoApiUrl+"/FreephoneClientUpdate"));
                //freephoneClientPost.setHeader("Authorization", "Basic " + encoding);
                freephoneClientPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                freephoneClientPost.setEntity(freephoneClientData);

                HttpClient freephoneClientHttpClient = HttpClients.createDefault();

                HttpResponse freephoneClientResponse = freephoneClientHttpClient.execute(freephoneClientPost);

                HttpEntity entity = freephoneClientResponse.getEntity();
                String content = EntityUtils.toString(entity);

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            return ResponseEntity.ok(saoRequestBody);
        }
        return null;
    }

    @RequestMapping(value = "/apis/PbxClientUpdate", method = RequestMethod.POST, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> PbxClientUpdate(@RequestBody String saoRequestBody) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }
        // Add check for auth in SAO

        if (isSftp) {
            try {
                //String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                StringEntity PBXClientData = new StringEntity(saoRequestBody, ContentType.APPLICATION_JSON);
                HttpPost PBXClientPost = new HttpPost(new URI(saoApiUrl+"/PbxClientUpdate"));
                //PBXClientPost.setHeader("Authorization", "Basic " + encoding);
                PBXClientPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                PBXClientPost.setEntity(PBXClientData);

                HttpClient PBXClientHttpClient = HttpClients.createDefault();

                HttpResponse PBXClientResponse = PBXClientHttpClient.execute(PBXClientPost);

                HttpEntity entity = PBXClientResponse.getEntity();
                String content = EntityUtils.toString(entity);

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            JSONObject saoTestResponseJSON = new JSONObject(saoRequestBody);
            if(saoTestResponseJSON.getInt("id") == 0) {
                saoTestResponseJSON.put("id", 2234);
                saoTestResponseJSON.put("status", "Created New PBX Client");
            }
            return ResponseEntity.ok(saoTestResponseJSON.toString());
            //return ResponseEntity.ok(saoRequestBody);
        }
        return null;
    }

}
