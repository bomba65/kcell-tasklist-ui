package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.delegate.Expression;

import java.util.Arrays;



@Service("getTCFForm")
@Log
public class GetTCFForm implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;
    Expression billingTCF;

    @Autowired
    public GetTCFForm(@Value("${sharepoint.forms.url:https://sp.kcell.kz/forms/_api/Lists}") String baseUri) {
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
        //String billingTCF = this.billingTCF.getValue(delegateExecution).toString();
        String billingTCF = String.valueOf(delegateExecution.getVariableLocal("billingTCF"));
        String tcfFormId = "";

        if (isSftp) {

            if("amdocs".equals(billingTCF)){
                tcfFormId = delegateExecution.getVariable("amdocsTcfFormId").toString();
            }
            if("orga".equals(billingTCF)){
                tcfFormId = delegateExecution.getVariable("orgaTcfFormId").toString();
            }

            HttpGet httpGet = new HttpGet(baseUri + "/getbytitle('ICTD%20TCF')/items("+tcfFormId+")");

            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());

            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");

            delegateExecution.setVariable(billingTCF + "GetResponseBodyTCF", responseString);

            JSONObject responseSharepointJSON = new JSONObject(responseString);
            JSONObject tcf = responseSharepointJSON.getJSONObject("d");
            //String Id = tcf.get("Id").toString();
            String Status = tcf.get("Status").toString();

            if("Completed".equals(Status)){
                String htmlTable = tcf.get("Requirments").toString();
                Document doc = Jsoup.parse(htmlTable);
                Element table = doc.select("table").get(0);

                ////////////////////// PARSE HTML /////////////////////////
                /*
                /////// WITHOUT NESTED TABLE ///////////
                Element row = table.select("tr").get(3);
                Element td = row.select("td").get(4);
                String identifierTCFID = td.text();
                */

                /////// WITH NESTED TABLE ///////////
                Element row = table.select("tr").get(4);
                Element td = row.select("td").get(4);
                Element nestedTable = td.getElementsByTag("table").get(0);
                Element nestedTableRow = nestedTable.select("tr").get(0);
                Element nestedTableRowTd = nestedTableRow.select("td").get(0);
                String identifierTCFID = nestedTableRowTd.text();
                ///////////////////////////////////////////////////////////
                if("amdocs".equals(billingTCF)){
                    delegateExecution.setVariable("identifierAmdocsID", identifierTCFID);
                    delegateExecution.setVariable("amdocsTcfIdReceived", true);
                }

                if("orga".equals(billingTCF)){
                    delegateExecution.setVariable("identifierOrgaID", identifierTCFID);
                    delegateExecution.setVariable("orgaTcfIdReceived", true);
                }
            } else {
                if("amdocs".equals(billingTCF)){
                    delegateExecution.setVariable("amdocsTcfIdReceived", false);
                }
                if("orga".equals(billingTCF)){
                    delegateExecution.setVariable("orgaTcfIdReceived", false);
                }
            }


        } else {

            String responseString = "{\n" +
                "    \"d\": {\n" +
                "        \"__metadata\": {\n" +
                "            \"id\": \"Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)\",\n" +
                "            \"uri\": \"https://sp.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)\",\n" +
                "            \"etag\": \"\\\"21\\\"\",\n" +
                "            \"type\": \"SP.Data.ICTD_x0020_TCFListItem\"\n" +
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

            //String Id = tcf.get("Id").toString();
            String htmlTable = tcf.get("Requirments").toString();

            Document doc = Jsoup.parse(htmlTable);
            Element table = doc.select("table").get(0);

            ////////////////////// PARSE HTML /////////////////////////
            /*
            /////// WITHOUT NESTED TABLE ///////////
            Element row = table.select("tr").get(3);
            Element td = row.select("td").get(4);
            String identifierTCFID = td.text();
            */

            /////// WITH NESTED TABLE ///////////
            Element row = table.select("tr").get(4);
            Element td = row.select("td").get(4);
            Element nestedTable = td.getElementsByTag("table").get(0);
            Element nestedTableRow = nestedTable.select("tr").get(0);
            Element nestedTableRowTd = nestedTableRow.select("td").get(0);
            String identifierTCFID = nestedTableRowTd.text();
            ///////////////////////////////////////////////////////////

            if("amdocs".equals(billingTCF)){
                System.out.println("billingTCF: " + billingTCF);
                delegateExecution.setVariable("identifierAmdocsID", identifierTCFID);
                delegateExecution.setVariable("amdocsTcfIdReceived", true);
            } else {
                delegateExecution.setVariable("amdocsTcfIdReceived", false);
            }

            if("orga".equals(billingTCF)){
                System.out.println("billingTCF: " + billingTCF);
                delegateExecution.setVariable("identifierOrgaID", identifierTCFID);
                delegateExecution.setVariable("orgaTcfIdReceived", true);
            } else {
                delegateExecution.setVariable("orgaTcfIdReceived", false);
            }

        }

    }
}
