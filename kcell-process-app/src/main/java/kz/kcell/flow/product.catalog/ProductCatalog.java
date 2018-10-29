package kz.kcell.flow.product.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;

@RestController
@RequestMapping("/product-catalog")
@Log
public class ProductCatalog {

    private final String productCatalogUrl;
    private final String productCatalogAuth;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private Environment environment;

    @Autowired
    public ProductCatalog(@Value("${product.catalog.preprod.url:http://ldb-al-preprod.kcell.kz/vas_short_numbers}") String productCatalogUrl, @Value("${product.catalog.auth:app.camunda.user:Asd123Qwerty!}") String productCatalogAuth) {
        this.productCatalogUrl = productCatalogUrl;
        this.productCatalogAuth = productCatalogAuth;
    }

    @RequestMapping(value = "/vas_short_numbers", method = RequestMethod.POST, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> setShortNumber(@RequestBody String shortNumber/*, HttpServletRequest request*/) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        if (isSftp) {
            try {
                String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                StringEntity shortNumberData = new StringEntity(shortNumber, ContentType.APPLICATION_JSON);
                HttpPost shortNumberPost = new HttpPost(new URI(productCatalogUrl));
                shortNumberPost.setHeader("Authorization", "Basic " + encoding);
                shortNumberPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                shortNumberPost.setEntity(shortNumberData);

                HttpClient shortNumberHttpClient = HttpClients.createDefault();

                HttpResponse shortNumberResponse = shortNumberHttpClient.execute(shortNumberPost);

                HttpEntity entity = shortNumberResponse.getEntity();
                String content = EntityUtils.toString(entity);

                /////EntityUtils.consume(content);
                //shortNumberHttpClient.close();

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            JSONObject shortNumberJSON = new JSONObject(shortNumber);
            shortNumberJSON.put("id", 12345);

            return ResponseEntity.ok(shortNumberJSON.toString());
        }
        return null;
    }

    @RequestMapping(value = "/vas_short_numbers/{shortNumberId}", method = RequestMethod.PUT, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> updateShortNumber(@PathVariable("shortNumberId") String shortNumberId, @RequestBody String shortNumber) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        if (isSftp) {
            try {
                String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                StringEntity shortNumberData = new StringEntity(shortNumber, ContentType.APPLICATION_JSON);
                HttpPut shortNumberPut = new HttpPut(new URI(productCatalogUrl+"/"+shortNumberId));
                shortNumberPut.setHeader("Authorization", "Basic " + encoding);
                shortNumberPut.addHeader("Content-Type", "application/json;charset=UTF-8");

                shortNumberPut.setEntity(shortNumberData);

                HttpClient shortNumberHttpClient = HttpClients.createDefault();

                HttpResponse shortNumberResponse = shortNumberHttpClient.execute(shortNumberPut);

                HttpEntity entity = shortNumberResponse.getEntity();
                String content = EntityUtils.toString(entity);

                /////EntityUtils.consume(content);
                //shortNumberHttpClient.close();

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            return ResponseEntity.ok(shortNumber);
        }
        return null;
    }
}
