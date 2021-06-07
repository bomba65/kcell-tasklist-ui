package kz.kcell.flow.leasing.asset;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("assetCreateContract")
public class CreateContract implements JavaDelegate {

    private Minio minioClient;
    private String baseUri;
    private String assetsUri;

    @Autowired
    DataSource dataSource;

    @Autowired
    public CreateContract(Minio minioClient, @Value("${mail.message.baseurl:http://localhost}") String baseUri, @Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUri) {
        this.minioClient = minioClient;
        this.baseUri = baseUri;
        this.assetsUri = assetsUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String _CONTRACT_APPROVAL_TYPE = delegateExecution.hasVariableLocal("_CONTRACT_APPROVAL_TYPE") ? delegateExecution.getVariable("_CONTRACT_APPROVAL_TYPE").toString() : null;
        SpinJsonNode contractJson = delegateExecution.getVariable("contractInfo") != null ? JSON(delegateExecution.getVariable("contractInfo")) : null;

        if (contractJson != null) {
            log.info("contractJson");
            log.info(contractJson.toString());
            String ct_acquisitionType = contractJson.hasProp("ct_acquisitionType") && contractJson.prop("ct_acquisitionType").value() != null ? contractJson.prop("ct_acquisitionType").value().toString() : null;
            if (ct_acquisitionType.equals("newContract") || ct_acquisitionType.equals("existingContract")) {
                Long old_contract_asset_id = null;

                String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
                String assetCreatedRenterId = delegateExecution.getVariable("assetCreatedRenterId") != null ? delegateExecution.getVariable("assetCreatedRenterId").toString() : null;
                String region = delegateExecution.getVariable("regionCatalogId") != null ? delegateExecution.getVariable("regionCatalogId").toString() : null;

                String ct_access_status_catalogId = contractJson.hasProp("ct_access_status_catalogId") && contractJson.prop("ct_access_status_catalogId").value()!=null ? contractJson.prop("ct_access_status_catalogId").value().toString() : null;
                String ct_currency_type_catalogId = contractJson.hasProp("ct_currency_type_catalogId") && contractJson.prop("ct_currency_type_catalogId").value()!=null ? contractJson.prop("ct_currency_type_catalogId").value().toString() : null;
                String ct_contract_type_catalogId = contractJson.hasProp("ct_contract_type_catalogId") && contractJson.prop("ct_contract_type_catalogId").value()!=null ? contractJson.prop("ct_contract_type_catalogId").value().toString() : null;
                String ct_payment_period_catalogId = contractJson.hasProp("ct_payment_period_catalogId") && contractJson.prop("ct_payment_period_catalogId").value()!=null ? contractJson.prop("ct_payment_period_catalogId").value().toString() : null;
                String ct_executor_catalogId = contractJson.hasProp("ct_executor_catalogId") && contractJson.prop("ct_executor_catalogId").value()!=null ? contractJson.prop("ct_executor_catalogId").value().toString() : null;

                String ct_acceptance_act_date = contractJson.hasProp("ct_acceptance_act_date") ? contractJson.prop("ct_acceptance_act_date").value().toString() : null;
                String ct_contract_start_date = contractJson.hasProp("ct_contract_start_date") ? contractJson.prop("ct_contract_start_date").value().toString() : null;
                String ct_contract_end_date = contractJson.hasProp("ct_contract_end_date") ? contractJson.prop("ct_contract_end_date").value().toString() : null;

                Long ct_contract_sap = contractJson.hasProp("ct_contract_sap") ? contractJson.prop("ct_contract_sap").numberValue().longValue() : 0;
                String contractid = contractJson.hasProp("ct_contractid") ? contractJson.prop("ct_contractid").value().toString() : "";
                Number ct_rent_all = contractJson.hasProp("ct_rent_all") ? contractJson.prop("ct_rent_all").numberValue() : 0;
                Number ct_rent_power = contractJson.hasProp("ct_rent_power") && !contractJson.prop("ct_rent_power").isNull() ? contractJson.prop("ct_rent_power").numberValue() : 0; //notReq
                Boolean ct_checkvat = contractJson.hasProp("ct_checkvat") ? (contractJson.prop("ct_checkvat").value().toString() == "No" ? false : true) : false;
                Boolean ct_ifrs16 = contractJson.hasProp("ct_ifrs16") ? (contractJson.prop("ct_ifrs16").value().toString() == "No" ? false : true) : false;
                Integer ct_vendor_sap = contractJson.hasProp("ct_vendor_sap") ? contractJson.prop("ct_vendor_sap").numberValue().intValue() : 0;
                Boolean ct_autoprolongation = contractJson.hasProp("ct_autoprolongation") ? (contractJson.prop("ct_autoprolongation").value().toString() == "No" ? false : true) : false;

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"); //2020-01-02T18:00:00.000Z
                String nowAsISO = formatter.format(new Date());


                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                if (ct_acquisitionType.equals("existingContract") || ct_acquisitionType.equals("additionalAgreement")) {
                    //get old contract asset id ex. AS-1362-09

                    String ct_contractid_old = contractJson.prop("ct_contractid_old").value().toString();

                    HttpGet httpGet = new HttpGet(new URI(this.assetsUri + "/asset-management/contracts/contractid/" + ct_contractid_old));
                    httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
                    httpGet.addHeader("Referer", baseUri);

                    CloseableHttpResponse getResponse = httpclient.execute(httpGet);

                    HttpEntity entity = getResponse.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    JSONArray jsonResponse = new JSONArray(responseString);
                    if (jsonResponse.get(0) != null) {
                        JSONObject jsonResponseFirst = jsonResponse.getJSONObject(0);
                        old_contract_asset_id = jsonResponseFirst.has("id") ? jsonResponseFirst.getLong("id") : null;
                    }
                }
                if (ct_acquisitionType.equals("existingContract")){

                    //update old contract
                    if (old_contract_asset_id != null) {
                        JSONObject putValue = new JSONObject();

                        JSONObject statusJson = new JSONObject();
                        statusJson.put("catalog_id", 29);
                        statusJson.put("id", 2);

                        putValue.put("contract_status_id", statusJson);
                        HttpPut httpPut = new HttpPut(new URI(this.assetsUri + "/asset-management/contracts/id/" + old_contract_asset_id));
                        //            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
                        httpPut.addHeader("Referer", baseUri);
                        StringEntity inputData = new StringEntity(putValue.toString());
                        httpPut.setEntity(inputData);
                        CloseableHttpResponse putResponse = httpclient.execute(httpPut);

                        if (putResponse.getStatusLine().getStatusCode() < 200 || putResponse.getStatusLine().getStatusCode() >= 300) {
                            throw new RuntimeException("contract updated returns code " + putResponse.getStatusLine().getStatusCode());
                        }
                    }
                }
                JSONObject jsonBody = new JSONObject();

                JSONObject regionJson = new JSONObject();
                regionJson.put("catalog_id", 5);
                regionJson.put("id", region);

                //            access_status_id
                if (ct_access_status_catalogId != null) {
                    JSONObject access_status_id_json = new JSONObject();
                    access_status_id_json.put("catalog_id", 25);
                    access_status_id_json.put("id", ct_access_status_catalogId);
                    jsonBody.put("access_status_id", access_status_id_json);
                }


                //            contract_executor_id
                JSONObject contract_executor_id_json = null;
                if(ct_executor_catalogId!=null){
                    contract_executor_id_json = new JSONObject();
                    contract_executor_id_json.put("catalog_id", 60);
                    contract_executor_id_json.put("id", ct_executor_catalogId);
                }

                //            contract_status_id
                JSONObject contract_status_id_json = new JSONObject();
                contract_status_id_json.put("catalog_id", 29);
                contract_status_id_json.put("id", 3);

                //            contract_type_id
                JSONObject contract_type_id_json = null;
                if(ct_contract_type_catalogId!=null){
                    contract_type_id_json = new JSONObject();
                    contract_type_id_json.put("catalog_id",23);
                    contract_type_id_json.put("id", ct_contract_type_catalogId);
                }
                //            currency_id

                JSONObject currency_id_json = null;
                if(ct_currency_type_catalogId!=null){
                    currency_id_json = new JSONObject();
                    currency_id_json.put("catalog_id",27);
                    currency_id_json.put("id", ct_currency_type_catalogId);
                }

//                //            cost_center_id
//                JSONObject cost_center_id_json = new JSONObject();
//                cost_center_id_json.put("catalog_id",38);
//                cost_center_id_json.put("id", costCenterJson.prop("assetsId").value().toString());

                //            payment_period_id
                JSONObject payment_period_id_json = null;
                if(ct_payment_period_catalogId!=null){
                    payment_period_id_json = new JSONObject();
                    payment_period_id_json.put("catalog_id",26);
                    payment_period_id_json.put("id", ct_payment_period_catalogId);
                }

                jsonBody.put("contractid", contractid);
                jsonBody.put("contract_date", nowAsISO);
                if(contract_executor_id_json!=null){
                    jsonBody.put("contract_executor_id", contract_executor_id_json);
                }

//            jsonBody.put("renter_id", assetCreatedRenterId);
                if(contract_type_id_json!=null){
                    jsonBody.put("contract_type_id", contract_type_id_json);
                }
                jsonBody.put("contract_status_id", contract_status_id_json);
                jsonBody.put("rent_sum", ct_rent_all);
                jsonBody.put("rent_power", ct_rent_power);
                jsonBody.put("need_vat", ct_checkvat);
                jsonBody.put("vat", 0);
                jsonBody.put("contract_sap", ct_contract_sap);
                jsonBody.put("vendor_sap", ct_vendor_sap);

                if(payment_period_id_json!=null){
                    jsonBody.put("payment_period_id", payment_period_id_json);
                }
                if(currency_id_json!=null){
                    jsonBody.put("currency_id", currency_id_json);
                }
                jsonBody.put("contract_start_date", ct_contract_start_date);
                jsonBody.put("contract_end_date", ct_contract_end_date);
                jsonBody.put("acceptance_act_date", ct_acceptance_act_date);
                jsonBody.put("autoprolongation", ct_autoprolongation);
                jsonBody.put("ifrs16", ct_ifrs16);
//                jsonBody.put("cost_center_id", cost_center_id_json);
                jsonBody.put("region_id", regionJson);
                if (ct_acquisitionType.equals("existingContract") && old_contract_asset_id != null ){
                    jsonBody.put("old_contract_id", old_contract_asset_id);
                }

                HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/contracts/"));
//            HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/ncp/"));
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(jsonBody.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                Long assetsCreatedId = jsonResponse.has("id") ?  jsonResponse.getLong("id") : null;
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>jsonBody<<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info(jsonBody.toString());
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info(jsonResponse.toString());
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>jsonResponse<<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("NEW Contract post by id " + contractid + " returns code " + postResponse.getStatusLine().getStatusCode());
                }

                if (assetsCreatedId != null) {
                    String assetsCreatedSiteId = delegateExecution.getVariable("assetsCreatedSiteId") != null ? delegateExecution.getVariable("assetsCreatedSiteId").toString() : null;
                    if (assetsCreatedSiteId != null) {
                        if (_CONTRACT_APPROVAL_TYPE.equals("FE")) {
                            JSONArray farEndInformation = delegateExecution.getVariable("farEndInformation") != null ? new JSONArray(delegateExecution.getVariable("farEndInformation").toString()) : null;
                            if (farEndInformation != null && farEndInformation.length() > 0) {
                                JSONObject priorityFarEnd = farEndInformation.getJSONObject(0);
                                assetsCreatedSiteId = String.valueOf(priorityFarEnd.getLong("farEndSequence"));
                                log.info("CHANGED assetsCreatedSiteId ====" + assetsCreatedSiteId);
                            }
                        }
                        JSONObject siteContractBody = new JSONObject();
                        siteContractBody.put("contract_id", assetsCreatedId);
                        siteContractBody.put("site_id", assetsCreatedSiteId);
                        log.info("SiteContract: ");
                        log.info(siteContractBody.toString());

                        HttpPost httpPostSC = new HttpPost(new URI(this.assetsUri + "/asset-management/sitesContracts/"));
                        httpPostSC.addHeader("Content-Type", "application/json;charset=UTF-8");
                        httpPostSC.addHeader("Referer", baseUri);
                        StringEntity inputDataSC = new StringEntity(siteContractBody.toString());
                        httpPostSC.setEntity(inputDataSC);

                        CloseableHttpResponse postResponseSC = httpclient.execute(httpPostSC);

                        HttpEntity entitySC = postResponseSC.getEntity();
                        String responseStringSC = EntityUtils.toString(entitySC, "UTF-8");
                        JSONObject jsonResponseSC = new JSONObject(responseStringSC);
                        Long siteContractId = jsonResponseSC.has("id") ? jsonResponseSC.getLong("id") : null;

                        log.info("post response code: " + postResponseSC.getStatusLine().getStatusCode());
                        log.info("sitecontract id: " + siteContractId);
                        if (postResponseSC.getStatusLine().getStatusCode() < 200 || postResponseSC.getStatusLine().getStatusCode() >= 300) {
                            throw new RuntimeException("Candidate (Sectors) post returns code " + postResponseSC.getStatusLine().getStatusCode());
                        }
                    }
//                delegateExecution.setVariable("assetsCreatedNcp", assetsCreatedNcp);
                } else {
                    throw new RuntimeException("post by id not parsed assetsCreatedId from response");
                }

            }

            if (ct_acquisitionType.equals("additionalAgreement")) {
                Long old_contract_asset_id = null;

                Number contract_id = contractJson.hasProp("ct_cid") ? contractJson.prop("ct_cid").numberValue() : null;
                String contractid = contractJson.hasProp("ct_contractid") ? contractJson.prop("ct_contractid").value().toString() : "";

                String aa_type_catalog_id = contractJson.hasProp("ct_agreement_type") ? contractJson.prop("ct_agreement_type").value().toString() : "";

                JSONObject aa_type_id = new JSONObject();
                aa_type_id.put("catalog_id", 28);
                aa_type_id.put("id", aa_type_catalog_id);

                String aa_number = contractJson.hasProp("ct_agreement_number") ? contractJson.prop("ct_agreement_number").value().toString() : "";
                String aa_reason = contractJson.hasProp("ct_agreement_reason") ? contractJson.prop("ct_agreement_reason").value().toString() : "";
                String aa_date = contractJson.hasProp("ct_aa_date") ? contractJson.prop("ct_aa_date").value().toString() : "";
                String ct_agreement_executor = contractJson.hasProp("ct_agreement_executor") ? contractJson.prop("ct_agreement_executor").value().toString() : "";

                JSONObject aa_executor_id = new JSONObject();
                aa_executor_id.put("catalog_id", 60);
                aa_executor_id.put("id", ct_agreement_executor);

                String contract_date = contractJson.hasProp("ct_contract_date") ? contractJson.prop("ct_contract_date").value().toString() : "";

                String ct_executor = contractJson.hasProp("ct_executor") ? contractJson.prop("ct_executor").value().toString() : "";

                JSONObject contract_executor_id = new JSONObject();
                contract_executor_id.put("catalog_id", 60);
                contract_executor_id.put("id", ct_executor);

                String ct_contract_type = contractJson.hasProp("ct_contract_type") ? contractJson.prop("ct_contract_type").value().toString() : "";
                JSONObject contract_type_id = new JSONObject();
                contract_type_id.put("catalog_id", 23);
                contract_type_id.put("id", ct_contract_type);

                Number ct_rent_all = contractJson.prop("ct_rent_all").numberValue();
                Number ct_rent_power = contractJson.hasProp("ct_rent_power") && !contractJson.prop("ct_rent_power").isNull() ? contractJson.prop("ct_rent_power").numberValue() : 0; //notReq
                Boolean ct_checkvat = !contractJson.prop("ct_checkvat").value().toString().equals("No");
                Long ct_contract_sap = contractJson.prop("ct_contract_sap").numberValue().longValue();

                String ct_access_status = contractJson.hasProp("ct_access_status") ? contractJson.prop("ct_access_status").value().toString() : "";
                JSONObject access_status_id = new JSONObject();
                access_status_id.put("catalog_id", 25);
                access_status_id.put("id", ct_access_status);

                String ct_payment_period = contractJson.hasProp("ct_payment_period") ? contractJson.prop("ct_payment_period").value().toString() : "";
                JSONObject payment_period_id = new JSONObject();
                payment_period_id.put("catalog_id", 26);
                payment_period_id.put("id", ct_payment_period);


                String ct_currency_type = contractJson.hasProp("ct_currency_type") ? contractJson.prop("ct_currency_type").value().toString() : "";
                JSONObject currency = new JSONObject();
                currency.put("catalog_id", 27);
                currency.put("id", ct_currency_type);

                JSONObject contract_status_id = new JSONObject();
                contract_status_id.put("catalog_id", 29);
                contract_status_id.put("id", 3);

                String ct_acceptance_act_date = contractJson.hasProp("ct_acceptance_act_date") ? contractJson.prop("ct_acceptance_act_date").value().toString() : null;
                String ct_contract_start_date = contractJson.hasProp("ct_contract_start_date") ? contractJson.prop("ct_contract_start_date").value().toString() : null;
                String ct_contract_end_date = contractJson.hasProp("ct_contract_end_date") ? contractJson.prop("ct_contract_end_date").value().toString() : null;

                Boolean ct_autoprolongation = !contractJson.prop("ct_autoprolongation").value().toString().equals("No");
                Boolean ct_ifrs16 = !contractJson.prop("ct_ifrs16").value().toString().equals("No");

                Integer ct_vendor_sap = contractJson.prop("ct_vendor_sap").numberValue().intValue();


                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"); //2020-01-02T18:00:00.000Z
                String nowAsISO = formatter.format(new Date());


                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
                CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

                JSONObject jsonBody = new JSONObject();



//                //            cost_center_id
//                JSONObject cost_center_id_json = new JSONObject();
//                cost_center_id_json.put("catalog_id",38);
//                cost_center_id_json.put("id", costCenterJson.prop("assetsId").value().toString());

                //            payment_period_id

                jsonBody.put("contract_id", old_contract_asset_id);
                jsonBody.put("aa_type_id", aa_type_id);
                jsonBody.put("aa_number", aa_number);
                jsonBody.put("aa_reason", aa_reason);
                jsonBody.put("aa_date", aa_date);
                jsonBody.put("aa_executor_id", aa_executor_id);
                jsonBody.put("contract_date", contract_date);
                jsonBody.put("contract_executor_id", contract_executor_id);
                jsonBody.put("contract_type_id", contract_type_id);
                jsonBody.put("contract_status_id", contract_status_id);
                jsonBody.put("rent_sum", ct_rent_all);
                jsonBody.put("rent_power", ct_rent_power);
//                jsonBody.put("need_vat", ct_checkvat);
                jsonBody.put("vat", 0);
                jsonBody.put("contract_sap", ct_contract_sap);
                jsonBody.put("access_status_id", access_status_id);
                jsonBody.put("payment_period_id", payment_period_id);
                jsonBody.put("currency_id", currency);
                jsonBody.put("contract_start_date", ct_contract_start_date);
                jsonBody.put("contract_end_date", ct_contract_end_date);
                jsonBody.put("acceptance_act_date", ct_acceptance_act_date);
                jsonBody.put("autoprolongation", ct_autoprolongation);
                jsonBody.put("ifrs16", ct_ifrs16);


                HttpPost httpPost = new HttpPost(new URI(this.assetsUri + "/asset-management/contract_aa/"));

                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPost.addHeader("Referer", baseUri);
                StringEntity inputData = new StringEntity(jsonBody.toString());
                httpPost.setEntity(inputData);

                CloseableHttpResponse postResponse = httpclient.execute(httpPost);

                HttpEntity entity = postResponse.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseString);
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>jsonBody<<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info(jsonBody.toString());
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info(jsonResponse.toString());
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>jsonResponse<<<<<<<<<<<<<<<<<<<<<<<<<<");
                log.info("post response code: " + postResponse.getStatusLine().getStatusCode());
                if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("AdditionalAgreement post by id " + contractid + " returns code " + postResponse.getStatusLine().getStatusCode() + "=============" + jsonResponse.toString());
                }
            }
        } else {
            throw new Exception("Error");
        }
    }
}
