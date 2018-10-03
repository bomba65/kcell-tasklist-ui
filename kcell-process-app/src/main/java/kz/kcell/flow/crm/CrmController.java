package kz.kcell.flow.crm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/crm")
@Log
public class CrmController {

    private final String b2bCRMurl;
    private final String b2bCRMauth;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private Environment environment;

    @Autowired
    public CrmController(@Value("${b2b.crm.url:http://ldb-al.kcell.kz/corp_client_profile/bin/}") String b2bCRMurl, @Value("${b2b.crm.auth:app.camunda.user:Asd123Qwerty!}") String b2bCRMauth) {
        this.b2bCRMurl = b2bCRMurl;
        this.b2bCRMauth = b2bCRMauth;
    }

    @RequestMapping(value = "/client/bin/{clientBIN}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getCRMClientByBin(@PathVariable("clientBIN") String clientBIN, HttpServletRequest request) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if (clientBIN == null || clientBIN.length() != 12 ) {
            log.warning("No bin or incorrect bin specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No bin or incorrect bin specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        if (isSftp) {
            String encoding = Base64.getEncoder().encodeToString((b2bCRMauth).getBytes("UTF-8"));

            CloseableHttpClient httpclient = HttpClients.custom().build();

            HttpGet httpGet = new HttpGet(b2bCRMurl + clientBIN);
            httpGet.setHeader("Authorization", "Basic " + encoding);

            HttpResponse httpResponse = httpclient.execute(httpGet);

            HttpEntity entity = httpResponse.getEntity();
            String content = EntityUtils.toString(entity);

            //JSONObject obj = new JSONObject(content);

            EntityUtils.consume(httpResponse.getEntity());
            httpclient.close();
            return ResponseEntity.ok(content);
        } else {

            ObjectMapper mapper = new ObjectMapper();

            ObjectNode fakeContent = mapper.createObjectNode();
            fakeContent.put("id", 80607);
            fakeContent.put("accountName", "Тестовый Клиент");
            fakeContent.put("accountTyp", "Prospect");
            fakeContent.put("bin", "111222333444");
            //fakeContent.put("cugId", null);
            //fakeContent.put("incugId", null);
            //fakeContent.put("categoryAbc", null);

            ObjectNode kcellRegion = mapper.createObjectNode();
            kcellRegion.put("id", 1);
            kcellRegion.put("name", "Almaty Region");
            //kcellRegion.put("status", null);
            kcellRegion.put("lastUpdateDate", "2017-08-01T17:05:52");
            fakeContent.put("kcellRegion", kcellRegion);

            ObjectNode dicChannel = mapper.createObjectNode();
            dicChannel.put("id", 3);
            dicChannel.put("name", "B2B LA");
            //dicChannel.put("status", null);
            //dicChannel.put("lastUpdateDate", null);
            fakeContent.put("dicChannel", dicChannel);

            ObjectNode salesExecutiveUser = mapper.createObjectNode();
            salesExecutiveUser.put("usersId", 10241);
            salesExecutiveUser.put("username", "RUSLAN.ZHOLDYBAYEV");
            salesExecutiveUser.put("firstName", "Руслан");
            salesExecutiveUser.put("lastName", "Жолдыбаев");
            salesExecutiveUser.put("middleName", "Сагатович");
            fakeContent.put("salesExecutiveUser", salesExecutiveUser);

            ObjectNode city = mapper.createObjectNode();
            city.put("id", 50000);
            city.put("nameRu", "Алматы");
            city.put("nameKz", "Алматы");
            city.put("nameEn", "Almaty");
            fakeContent.put("city", city);

            //fakeContent.put("dicSectorEconomics", null);

            ObjectNode servManagerUser = mapper.createObjectNode();
            servManagerUser.put("usersId", 10241);
            servManagerUser.put("username", "RUSLAN.ZHOLDYBAYEV");
            servManagerUser.put("firstName", "Руслан");
            servManagerUser.put("lastName", "Жолдыбаев");
            servManagerUser.put("middleName", "Сагатович");
            fakeContent.put("servManagerUser", servManagerUser);

            ObjectNode supervisorUser = mapper.createObjectNode();
            supervisorUser.put("usersId", 24132);
            supervisorUser.put("username", "VACANCY");
            supervisorUser.put("firstName", "vacancy");
            supervisorUser.put("lastName", "-");
            //supervisorUser.put("middleName", null);
            fakeContent.put("supervisorUser", supervisorUser);

            ObjectNode regionalHeadUser = mapper.createObjectNode();
            regionalHeadUser.put("usersId", 24132);
            regionalHeadUser.put("username", "VACANCY");
            regionalHeadUser.put("firstName", "vacancy");
            regionalHeadUser.put("lastName", "-");
            //regionalHeadUser.put("middleName", null);
            fakeContent.put("regionalHeadUser", regionalHeadUser);

            return ResponseEntity.ok(fakeContent.toString());
        }

    }
}
