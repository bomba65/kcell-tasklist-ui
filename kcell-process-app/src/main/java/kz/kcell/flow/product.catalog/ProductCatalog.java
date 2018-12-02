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
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
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
    public ProductCatalog(@Value("${product.catalog.url:http://ldb-al-preprod.kcell.kz}") String productCatalogUrl, @Value("${product.catalog.auth:app.camunda.user:Asd123Qwerty!}") String productCatalogAuth) {
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
                HttpPost shortNumberPost = new HttpPost(new URI(productCatalogUrl+"/vas_short_numbers"));
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
                HttpPut shortNumberPut = new HttpPut(new URI(productCatalogUrl+"/vas_short_numbers/"+shortNumberId));
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

    @RequestMapping(value = "/vas_short_numbers/{shortNumber}/service_type_id/{serviceTypeId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> checkShortNumber(@PathVariable("shortNumber") String shortNumber, @PathVariable("serviceTypeId") String serviceTypeId, HttpServletRequest request) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        if (isSftp) {
            try {
                String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                CloseableHttpClient httpclient = HttpClients.custom().build();

                HttpGet httpGet = new HttpGet(productCatalogUrl + "/vas_short_numbers/" + shortNumber + "/service_type_id/" + serviceTypeId);
                httpGet.setHeader("Authorization", "Basic " + encoding);

                HttpResponse httpResponse = httpclient.execute(httpGet);

                HttpEntity entity = httpResponse.getEntity();
                String content = EntityUtils.toString(entity);

                //JSONObject obj = new JSONObject(content);

                EntityUtils.consume(httpResponse.getEntity());
                httpclient.close();

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
                // or return ResponseEntity.ok(content); with some error
            }

        } else {
            String responseBodyString = "";
            // Freephone
            if ("177".equals(serviceTypeId)) {
                if ("7800".equals(shortNumber)){
                    responseBodyString = "{\n" +
                        "                \"id\" : 5133,\n" +
                        "                \"shortNumber\" : \"7800\",\n" +
                        "                \"brand\" : \"B\",\n" +
                        "                \"businessNameRu\" : \"ivr портал \\\"Настроние\\\"\",\n" +
                        "                \"businessNameEn\" : \"ivr портал \\\"Настроние\\\"\",\n" +
                        "                \"businessNameKz\" : \"ivr портал \\\"Настроние\\\"\",\n" +
                        "                \"descRu\" : \"Развлекательный IVR портал \\\"Настроение\\\". Бронь. Ответственный - Расул Рахимов.\",\n" +
                        "                \"descEn\" : \"Развлекательный IVR портал \\\"Настроение\\\"\",\n" +
                        "                \"descKz\" : \"Развлекательный IVR портал \\\"Настроение\\\"\",\n" +
                        "                \"sapNumber\" : null,\n" +
                        "                \"vasServiceType\" : {\n" +
                        "                \"id\" : 177,\n" +
                        "                    \"name\" : \"IVR\"\n" +
                        "            },\n" +
                        "                \"vasContentProvider\" : {\n" +
                        "                \"id\" : 132,\n" +
                        "                    \"name\" : \"Svyazkom LLP\",\n" +
                        "                    \"rnn\" : null,\n" +
                        "                    \"phone\" : \"7 902 942 0545\",\n" +
                        "                    \"email\" : \"glemag@svyazcom.ru, support@svyazcom.ru\",\n" +
                        "                    \"mobile\" : null,\n" +
                        "                    \"bin\" : null,\n" +
                        "                    \"contactPerson\" : null,\n" +
                        "                    \"agreementData\" : \"01.07.2014\",\n" +
                        "                    \"contentProviderState\" : \"A\",\n" +
                        "                    \"lastDt\" : null\n" +
                        "            },\n" +
                        "                \"shortNumberState\" : \"A\",\n" +
                        "                \"phoneIndex\" : null,\n" +
                        "                \"technicalServiceName\" : null,\n" +
                        "                \"rootService\" : null,\n" +
                        "                \"mainService\" : null,\n" +
                        "                \"admLineRevenue\" : null,\n" +
                        "                \"alphanumerics\" : \"N\",\n" +
                        "                \"tariffPlanComment\" : null,\n" +
                        "                \"bulk\" : \"N\",\n" +
                        "                \"serviceCost\" : null,\n" +
                        "                \"monthlyFee\" : null,\n" +
                        "                \"chargingMoCost\" : null,\n" +
                        "                \"operatorServiceCost\" : 77.0,\n" +
                        "                \"usersId\" : 24964,\n" +
                        "                \"changeUsername\" : null,\n" +
                        "                \"changedDate\" : null,\n" +
                        "                \"lastDt\" : \"2017-05-02T15:55:11\",\n" +
                        "                \"startDate\" : null,\n" +
                        "                \"finishDate\" : null,\n" +
                        "                \"type\" : null,\n" +
                        "                \"vasDirection\" : \"1\",\n" +
                        "                \"subdirection\" : \"2\",\n" +
                        "                \"businessGroup\" : \"1\",\n" +
                        "                \"promotion\" : null,\n" +
                        "                \"free10Sec\" : null,\n" +
                        "                \"bin\" : null,\n" +
                        "                \"connectionType\" : null,\n" +
                        "                \"ctNumber\" : null,\n" +
                        "                \"vasChargingIntervalId\" : 1,\n" +
                        "                \"smsLogicType\" : null,\n" +
                        "                \"smsLogicParameter\" : null,\n" +
                        "                \"smsLogicReset\" : null,\n" +
                        "                \"shortNumberAltel\" : null,\n" +
                        "                \"shortNumberBeeline\" : null,\n" +
                        "                \"shortNumberTele2\" : null,\n" +
                        "                \"msggwAccount\" : null,\n" +
                        "                \"monthlyFeeAltel\" : null,\n" +
                        "                \"monthlyFeeBeeline\" : null,\n" +
                        "                \"monthlyFeeTele2\" : null,\n" +
                        "                \"connectionTypeNew\" : null,\n" +
                        "                \"onetimePayment\" : null,\n" +
                        "                \"onetimePaymentAltel\" : null,\n" +
                        "                \"onetimePaymentBeeline\" : null,\n" +
                        "                \"onetimePaymentTele2\" : null,\n" +
                        "                \"offnetProviderName\" : null,\n" +
                        "                \"offnetProviderBin\" : null,\n" +
                        "                \"costPerSubscriber\" : null,\n" +
                        "                \"revenueSharing\" : null,\n" +
                        "                \"exceptions\" : null,\n" +
                        "                \"accountProvider\" : null,\n" +
                        "                \"contractDetail\" : null,\n" +
                        "                \"accountOnBulkSmsc\" : null,\n" +
                        "                \"moAccount\" : null\n" +
                        "            }";
                } else {
                    responseBodyString = "{\"timestamp\":\"2018-12-01T15:02:34.457\",\"status\":404,\"code\":\"ERR0007\",\"subCode\":\"INTERNAL_ERROR\",\"message\":\"Short number " + shortNumber + " doesn't exist\",\"path\":\"/vas_short_numbers/short_number/" + shortNumber + "/service_type_id/177\",\"requestId\":\"20181201_150234_442_VTp\"}";
                }
                return ResponseEntity.ok(responseBodyString);
            }

            if ("13".equals(serviceTypeId)) {
                if ("7800".equals(shortNumber)){
                    responseBodyString = "{\n" +
                        "                \"id\" : 6762,\n" +
                        "                \"shortNumber\" : \"7800\",\n" +
                        "                \"brand\" : \"B\",\n" +
                        "                \"businessNameRu\" : \"bulk sms\",\n" +
                        "                \"businessNameEn\" : \"bulk sms\",\n" +
                        "                \"businessNameKz\" : \"bulk sms\",\n" +
                        "                \"descRu\" : \"Транспортные услуги\\r\\n\",\n" +
                        "                \"descEn\" : null,\n" +
                        "                \"descKz\" : null,\n" +
                        "                \"sapNumber\" : null,\n" +
                        "                \"vasServiceType\" : {\n" +
                        "                \"id\" : 13,\n" +
                        "                    \"name\" : \"SMS\"\n" +
                        "            },\n" +
                        "                \"vasContentProvider\" : {\n" +
                        "                \"id\" : 499,\n" +
                        "                    \"name\" : \"ТОО \\\"TAXI MIR (ТАКСИ МИР)\\\"\",\n" +
                        "                    \"rnn\" : null,\n" +
                        "                    \"phone\" : null,\n" +
                        "                    \"email\" : null,\n" +
                        "                    \"mobile\" : null,\n" +
                        "                    \"bin\" : null,\n" +
                        "                    \"contactPerson\" : null,\n" +
                        "                    \"agreementData\" : null,\n" +
                        "                    \"contentProviderState\" : \"A\",\n" +
                        "                    \"lastDt\" : null\n" +
                        "            },\n" +
                        "                \"shortNumberState\" : \"A\",\n" +
                        "                \"phoneIndex\" : null,\n" +
                        "                \"technicalServiceName\" : null,\n" +
                        "                \"rootService\" : null,\n" +
                        "                \"mainService\" : null,\n" +
                        "                \"admLineRevenue\" : null,\n" +
                        "                \"alphanumerics\" : \"N\",\n" +
                        "                \"tariffPlanComment\" : null,\n" +
                        "                \"bulk\" : \"N\",\n" +
                        "                \"serviceCost\" : null,\n" +
                        "                \"monthlyFee\" : null,\n" +
                        "                \"chargingMoCost\" : 0.0,\n" +
                        "                \"operatorServiceCost\" : 100.0,\n" +
                        "                \"usersId\" : 10222,\n" +
                        "                \"changeUsername\" : \"olga.kozubova\",\n" +
                        "                \"changedDate\" : \"2017-10-09T16:25:59\",\n" +
                        "                \"lastDt\" : \"2017-10-09T16:25:59\",\n" +
                        "                \"startDate\" : \"2017-07-01T00:00:00\",\n" +
                        "                \"finishDate\" : null,\n" +
                        "                \"type\" : 210,\n" +
                        "                \"vasDirection\" : null,\n" +
                        "                \"subdirection\" : \"1\",\n" +
                        "                \"businessGroup\" : \"1\",\n" +
                        "                \"promotion\" : null,\n" +
                        "                \"free10Sec\" : null,\n" +
                        "                \"bin\" : \"160440011318\",\n" +
                        "                \"connectionType\" : null,\n" +
                        "                \"ctNumber\" : null,\n" +
                        "                \"vasChargingIntervalId\" : 1,\n" +
                        "                \"smsLogicType\" : null,\n" +
                        "                \"smsLogicParameter\" : null,\n" +
                        "                \"smsLogicReset\" : null,\n" +
                        "                \"shortNumberAltel\" : null,\n" +
                        "                \"shortNumberBeeline\" : null,\n" +
                        "                \"shortNumberTele2\" : null,\n" +
                        "                \"msggwAccount\" : null,\n" +
                        "                \"monthlyFeeAltel\" : null,\n" +
                        "                \"monthlyFeeBeeline\" : null,\n" +
                        "                \"monthlyFeeTele2\" : null,\n" +
                        "                \"connectionTypeNew\" : null,\n" +
                        "                \"onetimePayment\" : null,\n" +
                        "                \"onetimePaymentAltel\" : null,\n" +
                        "                \"onetimePaymentBeeline\" : null,\n" +
                        "                \"onetimePaymentTele2\" : null,\n" +
                        "                \"offnetProviderName\" : null,\n" +
                        "                \"offnetProviderBin\" : null,\n" +
                        "                \"costPerSubscriber\" : null,\n" +
                        "                \"revenueSharing\" : null,\n" +
                        "                \"exceptions\" : null,\n" +
                        "                \"accountProvider\" : null,\n" +
                        "                \"contractDetail\" : null,\n" +
                        "                \"accountOnBulkSmsc\" : null,\n" +
                        "                \"moAccount\" : null\n" +
                        "            }";
                } else {
                    responseBodyString = "{\"timestamp\":\"2018-12-01T15:02:10.564\",\"status\":404,\"code\":\"ERR0007\",\"subCode\":\"INTERNAL_ERROR\",\"message\":\"Short number " + shortNumber + " doesn't exist\",\"path\":\"/vas_short_numbers/short_number/" + shortNumber + "/service_type_id/13\",\"requestId\":\"20181201_150210_557_e89\"}";
                }
                return ResponseEntity.ok(responseBodyString);
            }
        }
        return null;
    }

    @RequestMapping(value = "/vas_content_providers/bin/{clientBIN}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> checkClientBIN(@PathVariable("clientBIN") String clientBIN, HttpServletRequest request) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        if (isSftp) {
            try {
                String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                CloseableHttpClient httpclient = HttpClients.custom().build();

                HttpGet httpGet = new HttpGet(productCatalogUrl + "/vas_content_providers/bin/" + clientBIN);
                httpGet.setHeader("Authorization", "Basic " + encoding);

                HttpResponse httpResponse = httpclient.execute(httpGet);

                HttpEntity entity = httpResponse.getEntity();
                String content = EntityUtils.toString(entity);

                //JSONObject obj = new JSONObject(content);

                EntityUtils.consume(httpResponse.getEntity());
                httpclient.close();

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
                // or return ResponseEntity.ok(content); with some error
            }

        } else {
            if ("111222333444".equals(clientBIN)) {
                String responseBodyString = "{\n" +
                    "  \"id\" : 8888,\n" +
                    "  \"name\" : \"Товарищество с ограниченной ответственностью \\\"Top Security KZ\\\"\",\n" +
                    "  \"rnn\" : null,\n" +
                    "  \"phone\" : null,\n" +
                    "  \"email\" : null,\n" +
                    "  \"mobile\" : null,\n" +
                    "  \"bin\" : \"111222333444\",\n" +
                    "  \"contactPerson\" : null,\n" +
                    "  \"agreementData\" : null,\n" +
                    "  \"contentProviderState\" : \"A\",\n" +
                    "  \"lastDt\" : \"2018-10-18T16:15:09\"\n" +
                    "}";
                return ResponseEntity.ok(responseBodyString);
            } else {
                return ResponseEntity.ok("{\"timestamp\":\"2018-12-01T16:12:23.188\",\"status\":404,\"code\":\"ERR0007\",\"subCode\":\"INTERNAL_ERROR\",\"message\":\"Provider with BIN " + clientBIN + " doesn't exist\",\"path\":\"/vas_content_providers/bin/" + clientBIN + "\",\"requestId\":\"20181201_161223_036_jWv\"}");
            }
        }
        return null;
    }

    @RequestMapping(value = "/vas_content_providers", method = RequestMethod.POST, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> createClient(@RequestBody String contentProvider) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        if (isSftp) {
            try {
                String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

                StringEntity contentProviderData = new StringEntity(contentProvider, ContentType.APPLICATION_JSON);
                HttpPost contentProviderPost = new HttpPost(new URI(productCatalogUrl+"/vas_content_providers"));
                contentProviderPost.setHeader("Authorization", "Basic " + encoding);
                contentProviderPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                contentProviderPost.setEntity(contentProviderData);

                HttpClient contentProviderHttpClient = HttpClients.createDefault();

                HttpResponse contentProviderResponse = contentProviderHttpClient.execute(contentProviderPost);

                HttpEntity entity = contentProviderResponse.getEntity();
                String content = EntityUtils.toString(entity);

                /////EntityUtils.consume(content);
                //contentProviderHttpClient.close();

                return ResponseEntity.ok(content);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            JSONObject contentProviderJSON = new JSONObject(contentProvider);
            contentProviderJSON.put("id", 99999);

            return ResponseEntity.ok(contentProviderJSON.toString());
        }
        return null;
    }
}
