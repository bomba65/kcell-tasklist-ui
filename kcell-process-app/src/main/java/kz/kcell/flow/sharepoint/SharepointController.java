package kz.kcell.flow.sharepoint;

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
import org.apache.xmlbeans.impl.jam.JSourcePosition;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.plugin.variable.SpinValues;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RestController
@Log
@RequestMapping("/sharepoint")
public class SharepointController {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private Environment environment;

    private final String baseUri;
    private final String username;
    private final String pwd;

    @Autowired
    public SharepointController(@Value("${sharepoint.forms.url:https://sp.kcell.kz/forms/_api}") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd) {
        this.baseUri = baseUri;
        this.username = username;
        this.pwd = pwd;
    }

    @RequestMapping(value = "/getbytitle/items/{itemId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTCF(@PathVariable("itemId") String itemId, HttpServletRequest request) throws NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException, ParseException {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
        String result = "error";
        if (isSftp) {
            try {
                String responseText = getAuthenticatedResponse(baseUri + "/Lists/getbytitle('ICTD%20TCF')/items("+ itemId +")", "kcell.kz", this.username, this.pwd);
                //result = responseText;

                ////////////////////
                //String responseString = EntityUtils.toString(entity, "UTF-8");

                JSONObject responseSharepointJSON = new JSONObject(responseText.toString());
                JSONObject tcf = responseSharepointJSON.getJSONObject("d");

                String Id = tcf.get("Id").toString();
                String htmlTable = tcf.get("Requirments").toString();

                Document doc = Jsoup.parse(htmlTable);
                Element table = doc.select("table").get(0);

                ////////////////////// PARSE HTML /////////////////////////
                /////// WITHOUT NESTED TABLE ///////////
                //Element row = table.select("tr").get(3);
                //Element td = row.select("td").get(4);
                //String identifierTCFID = td.text();
                //JSONObject responseJSON = new JSONObject();
                //responseJSON.put("itemId", itemId);
                //responseJSON.put("Id", Id);
                //responseJSON.put("identifierTCFID", identifierTCFID);
                //responseJSON.put("table", table.toString());
                //responseJSON.put("row", row.toString());
                //responseJSON.put("td", td.toString());

                /////// WITH NESTED TABLE ///////////
                Element row = table.select("tr").get(4);
                Element td = row.select("td").get(4);
                Element nestedTable = td.getElementsByTag("table").get(0);
                Element nestedTableRow = nestedTable.select("tr").get(0);
                Element nestedTableRowTd = nestedTableRow.select("td").get(0);
                String identifierTCFID = nestedTableRowTd.text();
                JSONObject responseJSON = new JSONObject();
                responseJSON.put("itemId", itemId);
                responseJSON.put("Id", Id);
                responseJSON.put("identifierTCFID", identifierTCFID);
                responseJSON.put("table", table.toString());
                responseJSON.put("row", row.toString());
                responseJSON.put("td", td.toString());
                responseJSON.put("nestedTable", nestedTable.toString());
                responseJSON.put("nestedTableRow", nestedTableRow.toString());
                responseJSON.put("nestedTableRowTd", nestedTableRowTd.toString());

                ///////////////////////////////////////////////////////////

                return ResponseEntity.ok(responseJSON.toString());
                ////////////////////

            } catch (Exception e) {
                e.printStackTrace();
            }

            //return ResponseEntity.ok(result);

        } else {

            String responseString = "{\n" +
                "    \"d\": {\n" +
                "        \"__metadata\": {\n" +
                "            \"id\": \"Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)\",\n" +
                "            \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)\",\n" +
                "            \"etag\": \"\\\"21\\\"\",\n" +
                "            \"type\": \"SP.Data.TCF_x005f_testListItem \"\n" +
                "        },\n" +
                "        \"FirstUniqueAncestorSecurableObject\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FirstUniqueAncestorSecurableObject\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"RoleAssignments\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/RoleAssignments\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"AttachmentFiles\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/AttachmentFiles\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ContentType\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/ContentType\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesAsHtml\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FieldValuesAsHtml\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesAsText\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FieldValuesAsText\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesForEdit\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FieldValuesForEdit\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"File\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/File\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"Folder\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/Folder\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ParentList\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/ParentList\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FileSystemObjectType\": 0,\n" +
                "        \"Id\": 2914,\n" +
                "        \"ContentTypeId\": \"0x010067B7029E75B96D43BBF9D557A43A4B5C\",\n" +
                "        \"Title\": \"Form #2914\",\n" +
                "        \"InitiatorId\": 3034,\n" +
                "        \"InitiatorDepartment\": \"B2B\",\n" +
                "        \"Subject\": \"B2B short numbers\",\n" +
                "        \"DateDeadline\": \"2018-06-26T00:00:00Z\",\n" +
                "        \"OldTS\": null,\n" +
                "        \"NewTS\": null,\n" +
                "        \"Operator\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.String)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                \"Activ\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"BillingType\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.String)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                \"Orga\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"Service\": \"Products / Tariffs\",\n" +
                "        \"RelationWithThirdParty\": false,\n" +
                "        \"Requirments\": \"<div class=\\\"ExternalClass13053E1C48BA42E3B105B9FE030144BB\\\">\\n<table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width&#58;493px;\\\">\\n\\t<tbody>\\n\\t\\t<tr>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;99px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>ervice Name </strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;64px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Short Number</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td colspan=\\\"3\\\" style=\\\"width&#58;192px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Orga</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;139px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Comments for ICTD</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Counter</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Price per counter</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Orga ID</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;60px;\\\">\\n\\t\\t\\t<p><strong>Create new name and tariff</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td colspan=\\\"5\\\" style=\\\"width&#58;395px;height&#58;60px;\\\">\\n\\t\\t\\t<p>&#160;</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Outgoing SMS Resmi 3775</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>3775</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>7 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>VASSMSO_3775_91616</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Please change&#160; tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\tSMS Concult<br>\\n\\t\\t\\t5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>&#160;</p>\\n\\n\\t\\t\\t<table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width&#58;145px;\\\">\\n\\t\\t\\t\\t<tbody>\\n\\t\\t\\t\\t\\t<tr>\\n\\t\\t\\t\\t\\t\\t<td style=\\\"width&#58;145px;height&#58;19px;\\\">VASSMSO_5190_91693</td>\\n\\t\\t\\t\\t\\t</tr>\\n\\t\\t\\t\\t\\t<tr>\\n\\t\\t\\t\\t\\t\\t<td style=\\\"height&#58;19px;\\\">&#160;</td>\\n\\t\\t\\t\\t\\t</tr>\\n\\t\\t\\t\\t</tbody>\\n\\t\\t\\t</table>\\n\\n\\t\\t\\t<p>&#160;</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Incoming SMS<br>\\n\\t\\t\\tSMS Concult 5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASSMSI_5190_91694</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\tPhilip Morris<br>\\n\\t\\t\\t2121</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>2121</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>TuanTuan_2121_3304</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please change&#160; tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;85px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1355 Karagandinskaya Sluzhba Spaseniya</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>1355</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>VASIVR_1355_91692</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;85px;\\\">\\n\\t\\t\\t<p>Please change tariff from 01.07.2018</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Incoming SMS<br>\\n\\t\\t\\t7111&#160; Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>7111</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Timwe_7111_3621</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\t7111&#160; Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>7111</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Timwe_7111_3620</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 9595 Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>9595</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>VASIVR_9595_91695</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 2777 Bazis A</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>2777</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>VASIVR_2777_90633</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1450 Adal Su</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1450</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>15 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASIVR_1450_9944</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please change tariff. First 10 sec free of charge</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1515 &quot;ITS Security Group&quot;</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1515</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASIVR_1515_91696</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t</tbody>\\n</table>\\n</div>\\n\",\n" +
                "        \"StartDate\": null,\n" +
                "        \"EndDate\": null,\n" +
                "        \"ProductName\": null,\n" +
                "        \"ProductComponents\": null,\n" +
                "        \"BalancesPocketsAccums\": null,\n" +
                "        \"Status\": \"Completed\",\n" +
                "        \"Waiting_x0020_forId\": null,\n" +
                "        \"InterconnectPhonesTesting\": null,\n" +
                "        \"InterconnectDateTestStart\": null,\n" +
                "        \"InterconnectDateTestEnd\": null,\n" +
                "        \"CBOSSPhonesTesting\": null,\n" +
                "        \"CBOSSDateTestStart\": null,\n" +
                "        \"CBOSSDateTestEnd\": null,\n" +
                "        \"ORGAPhonesTesting\": \"77010100756\",\n" +
                "        \"ORGADateTestStart\": \"2018-06-24T00:00:00Z\",\n" +
                "        \"ORGADateTestEnd\": \"2018-06-25T00:00:00Z\",\n" +
                "        \"TICPhonesTesting\": null,\n" +
                "        \"TICDateTestStart\": null,\n" +
                "        \"TICDateTestEnd\": null,\n" +
                "        \"RoamingPhonesTesting\": null,\n" +
                "        \"RoamingDateTestStart\": null,\n" +
                "        \"RoamingDateTestEnd\": null,\n" +
                "        \"Comments\": null,\n" +
                "        \"DepartmentManagerId\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.Int32)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                263\n" +
                "            ]\n" +
                "        },\n" +
                "        \"FD_x002d_BRS_x002d_ExpertId\": 228,\n" +
                "        \"FD_x002d_IBS_x002d_IT_x0020_SupeId\": 2578,\n" +
                "        \"ICTD_x002d_SNS_x002d_BCOU_x002d_Id\": 298,\n" +
                "        \"ICTD_x0020_SpecialistsId\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.Int32)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                222,\n" +
                "                224,\n" +
                "                3291\n" +
                "            ]\n" +
                "        },\n" +
                "        \"DepartmentSpecialistId\": null,\n" +
                "        \"MassApprove\": null,\n" +
                "        \"TCF_CREATE\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"SP.FieldUrlValue\"\n" +
                "            },\n" +
                "            \"Description\": \"Completed\",\n" +
                "            \"Url\": \"https://sp13prod.kcell.kz/forms/_layouts/15/wrkstat.aspx?List=d79e9f26-54d0-4db3-9488-d551236b0005&WorkflowInstanceName=70153b0c-4995-4aa4-a208-20b289e67da5\"\n" +
                "        },\n" +
                "        \"TypeForm\": \"»зменение тарифа на существующий сервис (New service TCF)\",\n" +
                "        \"ServiceNameRUS\": \"Free Phone\",\n" +
                "        \"ServiceNameENG\": \"Free Phone\",\n" +
                "        \"ServiceNameKAZ\": \"Free Phone\",\n" +
                "        \"ID\": 2914,\n" +
                "        \"Modified\": \"2018-06-26T09:45:12Z\",\n" +
                "        \"Created\": \"2018-06-19T09:24:51Z\",\n" +
                "        \"AuthorId\": 3034,\n" +
                "        \"EditorId\": 3034,\n" +
                "        \"OData__UIVersionString\": \"1.0\",\n" +
                "        \"Attachments\": false,\n" +
                "        \"GUID\": \"ad7aa207-76aa-4a0b-ba87-684bf9fb0037\"\n" +
                "    }\n" +
                "}";

            JSONObject responseSharepointJSON = new JSONObject(responseString);
            JSONObject tcf = responseSharepointJSON.getJSONObject("d");

            String Id = tcf.get("Id").toString();
            String htmlTable = tcf.get("Requirments").toString();

            Document doc = Jsoup.parse(htmlTable);
            Element table = doc.select("table").get(0);

            ////////////////////// PARSE HTML /////////////////////////
            /*
            /////// WITHOUT NESTED TABLE ///////////
            Element row = table.select("tr").get(3);
            Element td = row.select("td").get(4);
            String identifierTCFID = td.text();
            JSONObject responseJSON = new JSONObject();
            responseJSON.put("itemId", itemId);
            responseJSON.put("Id", Id);
            responseJSON.put("identifierTCFID", identifierTCFID);
            responseJSON.put("table", table.toString());
            responseJSON.put("row", row.toString());
            responseJSON.put("td", td.toString());
            */

            /////// WITH NESTED TABLE ///////////
            Element row = table.select("tr").get(4);
            Element td = row.select("td").get(4);
            Element nestedTable = td.getElementsByTag("table").get(0);
            Element nestedTableRow = nestedTable.select("tr").get(0);
            Element nestedTableRowTd = nestedTableRow.select("td").get(0);
            String identifierTCFID = nestedTableRowTd.text();
            JSONObject responseJSON = new JSONObject();
            responseJSON.put("itemId", itemId);
            responseJSON.put("Id", Id);
            responseJSON.put("identifierTCFID", identifierTCFID);
            responseJSON.put("table", table.toString());
            responseJSON.put("row", row.toString());
            responseJSON.put("td", td.toString());
            responseJSON.put("nestedTable", nestedTable.toString());
            responseJSON.put("nestedTableRow", nestedTableRow.toString());
            responseJSON.put("nestedTableRowTd", nestedTableRowTd.toString());

            ///////////////////////////////////////////////////////////


            return ResponseEntity.ok(responseJSON.toString());

        }
        return null;
    }

    @RequestMapping(value = "/contextinfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> postNtlmAuth() {
        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
        String result = "error";
        if (isSftp) {
            try {
                String responseText = postAuthenticatedResponse(baseUri + "/contextinfo", "kcell.kz", this.username, this.pwd);
                result = responseText;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok(result);
        } else {
            String responseString = "{\n" +
                "    \"d\": {\n" +
                "        \"GetContextWebInformation\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"SP.ContextWebInformation\"\n" +
                "            },\n" +
                "            \"FormDigestTimeoutSeconds\": 1800,\n" +
                "            \"FormDigestValue\": \"0x922140642AAA754D46D74E9608EB072E93AA387D808026D4BD8DFE1B3FBE5339ECFA8A0FE7786DC24EA6484D475C6F28CAA9BC9DF4D178F9BC25264650EB300A,22 Feb 2019 09:44:01 -0000\",\n" +
                "            \"LibraryVersion\": \"15.0.5085.1000\",\n" +
                "            \"SiteFullUrl\": \"https://sp.kcell.kz/forms\",\n" +
                "            \"SupportedSchemaVersions\": {\n" +
                "                \"__metadata\": {\n" +
                "                    \"type\": \"Collection(Edm.String)\"\n" +
                "                },\n" +
                "                \"results\": [\n" +
                "                    \"14.0.0.0\",\n" +
                "                    \"15.0.0.0\"\n" +
                "                ]\n" +
                "            },\n" +
                "            \"WebFullUrl\": \"https://sp.kcell.kz/forms\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
            return ResponseEntity.ok(responseString);
        }
    }

    @RequestMapping(value = "/items", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> postNtlmItem(@RequestBody String requestBody) {
        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
        String result = "error";
        if (isSftp) {
            try {
                String responseText = postItemsResponse(baseUri + "/Lists/getbytitle('ICTD%20TCF')/items", "kcell.kz", this.username, this.pwd, requestBody);
                result = responseText;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseEntity.ok(result);
        } else {
            String responseString = "{\n" +
                "    \"d\": {\n" +
                "        \"__metadata\": {\n" +
                "            \"id\": \"Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)\",\n" +
                "            \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)\",\n" +
                "            \"etag\": \"\\\"1\\\"\",\n" +
                "            \"type\": \"SP.Data.TCF_x005f_testListItem\"\n" +
                "        },\n" +
                "        \"FirstUniqueAncestorSecurableObject\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/FirstUniqueAncestorSecurableObject\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"RoleAssignments\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/RoleAssignments\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"AttachmentFiles\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/AttachmentFiles\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ContentType\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/ContentType\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesAsHtml\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/FieldValuesAsHtml\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesAsText\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/FieldValuesAsText\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesForEdit\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/FieldValuesForEdit\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"File\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/File\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"Folder\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/Folder\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ParentList\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'2e16f18e-95d2-4d38-bc31-a6be2ceadfe5')/Items(7)/ParentList\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FileSystemObjectType\": 0,\n" +
                "        \"Id\": 7,\n" +
                "        \"ContentTypeId\": \"0x0100638A26D33783534981A5A47693AF4307\",\n" +
                "        \"Title\": null,\n" +
                "        \"InitiatorId\": null,\n" +
                "        \"InitiatorDepartment\": \"B2B\",\n" +
                "        \"Subject\": \"B2B Short Numbers\",\n" +
                "        \"DateDeadline\": \"2019-02-20T18:00:00Z\",\n" +
                "        \"OldTS\": null,\n" +
                "        \"NewTS\": null,\n" +
                "        \"Operator\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.String)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                \"Kcell\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"BillingType\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.String)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                \"CBOSS\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"Service\": \"Products/Tariffs\",\n" +
                "        \"RelationWithThirdParty\": false,\n" +
                "        \"Requirments\": \"<div class=\\\"ExternalClass13053E1C48BA42E3B105B9FE030144BB\\\">\\n<table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width&#58;493px;\\\">\\n\\t<tbody>\\n\\t\\t<tr>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;99px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>ervice Name </strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;64px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Short Number</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td colspan=\\\"3\\\" style=\\\"width&#58;192px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Orga</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;139px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Comments for ICTD</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Counter</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Price per counter</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Orga ID</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;60px;\\\">\\n\\t\\t\\t<p><strong>Create new name and tariff</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td colspan=\\\"5\\\" style=\\\"width&#58;395px;height&#58;60px;\\\">\\n\\t\\t\\t<p>&#160;</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Outgoing SMS Resmi 3775</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>3775</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>7 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>VASSMSO_3775_91616</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Please change&#160; tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\tSMS Concult<br>\\n\\t\\t\\t5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>&#160;</p>\\n\\n\\t\\t\\t<table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width&#58;145px;\\\">\\n\\t\\t\\t\\t<tbody>\\n\\t\\t\\t\\t\\t<tr>\\n\\t\\t\\t\\t\\t\\t<td style=\\\"width&#58;145px;height&#58;19px;\\\">VASSMSO_5190_91693</td>\\n\\t\\t\\t\\t\\t</tr>\\n\\t\\t\\t\\t\\t<tr>\\n\\t\\t\\t\\t\\t\\t<td style=\\\"height&#58;19px;\\\">&#160;</td>\\n\\t\\t\\t\\t\\t</tr>\\n\\t\\t\\t\\t</tbody>\\n\\t\\t\\t</table>\\n\\n\\t\\t\\t<p>&#160;</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Incoming SMS<br>\\n\\t\\t\\tSMS Concult 5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASSMSI_5190_91694</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\tPhilip Morris<br>\\n\\t\\t\\t2121</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>2121</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>TuanTuan_2121_3304</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please change&#160; tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;85px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1355 Karagandinskaya Sluzhba Spaseniya</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>1355</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>VASIVR_1355_91692</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;85px;\\\">\\n\\t\\t\\t<p>Please change tariff from 01.07.2018</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Incoming SMS<br>\\n\\t\\t\\t7111&#160; Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>7111</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Timwe_7111_3621</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\t7111&#160; Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>7111</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Timwe_7111_3620</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 9595 Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>9595</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>VASIVR_9595_91695</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 2777 Bazis A</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>2777</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>VASIVR_2777_90633</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1450 Adal Su</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1450</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>15 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASIVR_1450_9944</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please change tariff. First 10 sec free of charge</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1515 &quot;ITS Security Group&quot;</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1515</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASIVR_1515_91696</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t</tbody>\\n</table>\\n</div>\\n\",\n" +
                "        \"StartDate\": null,\n" +
                "        \"EndDate\": null,\n" +
                "        \"ProductName\": null,\n" +
                "        \"ProductComponents\": null,\n" +
                "        \"BalancesPocketsAccums\": null,\n" +
                "        \"Status\": \"Approved by Department Manager\",\n" +
                "        \"Waiting_x0020_forId\": null,\n" +
                "        \"InterconnectPhonesTesting\": null,\n" +
                "        \"InterconnectDateTestStart\": null,\n" +
                "        \"InterconnectDateTestEnd\": null,\n" +
                "        \"CBOSSPhonesTesting\": null,\n" +
                "        \"CBOSSDateTestStart\": null,\n" +
                "        \"CBOSSDateTestEnd\": null,\n" +
                "        \"ORGAPhonesTesting\": null,\n" +
                "        \"ORGADateTestStart\": null,\n" +
                "        \"ORGADateTestEnd\": null,\n" +
                "        \"TICPhonesTesting\": null,\n" +
                "        \"TICDateTestStart\": null,\n" +
                "        \"TICDateTestEnd\": null,\n" +
                "        \"RoamingPhonesTesting\": null,\n" +
                "        \"RoamingDateTestStart\": null,\n" +
                "        \"RoamingDateTestEnd\": null,\n" +
                "        \"Comments\": null,\n" +
                "        \"DepartmentManagerId\": null,\n" +
                "        \"FD_x002d_BRS_x002d_ExpertId\": null,\n" +
                "        \"FD_x002d_IBS_x002d_IT_x0020_SupeId\": null,\n" +
                "        \"ICTD_x002d_SNS_x002d_BCOU_x002d_Id\": null,\n" +
                "        \"ICTD_x0020_SpecialistsId\": null,\n" +
                "        \"DepartmentSpecialistId\": null,\n" +
                "        \"MassApprove\": null,\n" +
                "        \"TCF_CREATE\": null,\n" +
                "        \"TypeForm\": \"\\u0418\\u0437\\u043c\\u0435\\u043d\\u0435\\u043d\\u0438\\u0435 \\u0442\\u0430\\u0440\\u0438\\u0444\\u0430 \\u043d\\u0430 \\u0441\\u0443\\u0449\\u0435\\u0441\\u0442\\u0432\\u0443\\u044e\\u0449\\u0438\\u0439 \\u0441\\u0435\\u0440\\u0432\\u0438\\u0441 (New service TCF)\",\n" +
                "        \"ServiceNameRUS\": \"Free Phone / Bulk sms\",\n" +
                "        \"ServiceNameENG\": \"Free Phone / Bulk sms\",\n" +
                "        \"ServiceNameKAZ\": \"Free Phone / Bulk sms\",\n" +
                "        \"OData__x0031_23\": null,\n" +
                "        \"ID\": 7,\n" +
                "        \"Modified\": \"2019-02-22T09:45:18Z\",\n" +
                "        \"Created\": \"2019-02-22T09:45:18Z\",\n" +
                "        \"AuthorId\": 4599,\n" +
                "        \"EditorId\": 4599,\n" +
                "        \"OData__UIVersionString\": \"1.0\",\n" +
                "        \"Attachments\": false,\n" +
                "        \"GUID\": \"52948b46-9ab6-4131-82b3-fcd2719d14c5\"\n" +
                "    }\n" +
                "}";
            return ResponseEntity.ok(responseString);
        }
    }

    private static String getAuthenticatedResponse(
        final String urlStr, final String domain,
        final String userName, final String password) throws IOException {

        StringBuilder response = new StringBuilder();

        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        URL urlRequest = new URL(urlStr);
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

        return response.toString();
    }

    private static String postAuthenticatedResponse(
        final String urlStr, final String domain,
        final String userName, final String password) throws IOException {

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
        final String requestBodyStr) throws IOException {

        JSONObject reqObj = new JSONObject(requestBodyStr);
        String formDigetValueStr = reqObj.get("FormDigestValue").toString();
        reqObj.remove("FormDigestValue");
        String jsonRequestBody = reqObj.toString();

        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        log.info("postItemsResponse URL:");
        log.info(urlStr);
        log.info("postItemsResponse BODY:");
        log.info(jsonRequestBody);
        URL urlRequest = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("X-requestDigest", formDigetValueStr);
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
        log.info("postItemsResponse RESPONSE: ");
        log.info(requestBodyStr);
        JSONObject jsonObject = new JSONObject(result);

        in.close();
        conn.disconnect();

        return jsonObject.toString();
    }

    @RequestMapping(value = "/forms/tcf", method = RequestMethod.POST, produces = {"application/json"}, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> PostTCF(@RequestBody String RequestBody) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        /*if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }*/

        if (isSftp) {
            try {

                StringEntity TCFData = new StringEntity(RequestBody, ContentType.APPLICATION_JSON);

                HttpPost httpPostTCF = new HttpPost(new URI(baseUri + "/getbytitle('ICTD%20TCF')"));
                httpPostTCF.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPostTCF.setEntity(TCFData);

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());

                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                HttpResponse response = httpClient.execute(httpPostTCF);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                return ResponseEntity.ok(responseString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            JSONObject testResponseJSON = new JSONObject(RequestBody);
            testResponseJSON.put("id", 1234);

            return ResponseEntity.ok(testResponseJSON.toString());
            //return ResponseEntity.ok(RequestBody);
        }
        return null;
    }
}
