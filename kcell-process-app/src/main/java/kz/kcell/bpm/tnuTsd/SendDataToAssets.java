package kz.kcell.bpm.tnuTsd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.apache.http.client.HttpClient;

import java.util.Random;

@Log
public class SendDataToAssets implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        log.info("sending data to Assets");
        Integer capacityId = Integer.parseInt(String.valueOf(execution.getVariable("hop_link_capacity")));
        Integer hop_link_type = Integer.parseInt(String.valueOf(execution.getVariable("hop_link_type")));
        Integer fe_rau_subband = Integer.parseInt(String.valueOf(execution.getVariable("fe_rau_subband")));
        Integer fe_protection_rau_subband = execution.getVariable("fe_protection_rau_subband") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_protection_rau_subband")));
        Integer fe_siteId = Integer.parseInt(String.valueOf(execution.getVariable("fe_siteId")));
        Integer fe_diameter = Integer.parseInt(String.valueOf(execution.getVariable("fe_diameter")));
        Integer fe_protection_antenna_diameter = execution.getVariable("fe_protection_antenna_diameter") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_protection_antenna_diameter")));
        Double fe_azimuth = Double.parseDouble(String.valueOf(execution.getVariable("fe_azimuth")));
        Integer fe_suspension_height_antennas = execution.getVariable("fe_suspension_height_antennas") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_suspension_height_antennas")));
        Integer fe_protection_suspension_height = execution.getVariable("fe_protection_suspension_height") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_protection_suspension_height")));
        Integer fe_power_level_rx = execution.getVariable("fe_power_level_rx") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_power_level_rx")));
        Integer fe_protection_power_level_rx = execution.getVariable("fe_protection_power_level_rx") == null || String.valueOf(execution.getVariable("fe_protection_power_level_rx")).equals("") ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_protection_power_level_rx")));
        Integer fe_power_level_tx = execution.getVariable("fe_power_level_tx") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_power_level_tx")));
        Integer fe_protection_power_level_tx = execution.getVariable("fe_protection_power_level_tx") == null || String.valueOf(execution.getVariable("fe_protection_power_level_tx")).equals("") ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_protection_power_level_tx")));
        String fe_terminal_id = execution.getVariable("fe_terminal_id") == null ? null : String.valueOf(execution.getVariable("fe_terminal_id")).equals("") ? null : String.valueOf(execution.getVariable("fe_terminal_id"));
        Integer fe_txrx_frequincies = execution.getVariable("fe_txrx_frequincies") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_txrx_frequincies")));
        Integer fe_protection_txrx_frequincies = execution.getVariable("fe_protection_txrx_frequincies") == null || String.valueOf(execution.getVariable("fe_protection_txrx_frequincies")).equals("") ? null : Integer.parseInt(String.valueOf(execution.getVariable("fe_protection_txrx_frequincies")));


        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        ObjectNode capacity = objectMapper.createObjectNode();
        objectNode.set("capacity_id", capacity);
        capacity.put("catalog_id", 45);
        capacity.put("id", capacityId);

        objectNode.put("farend_id", fe_siteId);

        ObjectNode fe_antenna_diameter_id = objectMapper.createObjectNode();
        objectNode.set("fe_antenna_diameter_id", fe_antenna_diameter_id);
        fe_antenna_diameter_id.put("catalog_id", 20);
        fe_antenna_diameter_id.put("id", fe_diameter);

        if (fe_protection_antenna_diameter != null) {
            ObjectNode fe_antenna_diameter_protect_id = objectMapper.createObjectNode();
            objectNode.set("fe_antenna_diameter_protect_id", fe_antenna_diameter_protect_id);
            fe_antenna_diameter_protect_id.put("catalog_id", 20);
            fe_antenna_diameter_protect_id.put("id", fe_protection_antenna_diameter);
        }


        objectNode.put("fe_azimuth", fe_azimuth);
        objectNode.put("fe_different_address", true);
        objectNode.set("fe_facility_id", null);
        objectNode.put("fe_height_susp_antenna", fe_suspension_height_antennas);
        objectNode.put("fe_height_susp_antenna_protect", fe_protection_suspension_height);
        objectNode.put("fe_power_levels_rx", fe_power_level_rx);
        objectNode.put("fe_power_levels_rx_protect", fe_protection_power_level_rx);
        objectNode.put("fe_power_levels_tx", fe_power_level_tx);
        objectNode.put("fe_power_levels_tx_protect", fe_protection_power_level_tx);

        ObjectNode fe_rau_subband_id = objectMapper.createObjectNode();
        objectNode.set("fe_rau_subband_id", fe_rau_subband_id);
        fe_rau_subband_id.put("catalog_id", 46);
        fe_rau_subband_id.put("id", fe_rau_subband);

        if (fe_protection_rau_subband != null) {
            ObjectNode fe_rau_subband_protect_id = objectMapper.createObjectNode();
            objectNode.set("fe_rau_subband_protect_id", fe_rau_subband_protect_id);
            fe_rau_subband_protect_id.put("catalog_id", 46);
            fe_rau_subband_protect_id.put("id", fe_protection_rau_subband);
        }

        objectNode.put("fe_terminalid", fe_terminal_id);
        objectNode.put("fe_txrx_frequencies", fe_txrx_frequincies);
        objectNode.put("fe_txrx_frequencies_protect", fe_protection_txrx_frequincies);

        ObjectNode link_type_id = objectMapper.createObjectNode();
        objectNode.set("fe_rau_subband_protect_id", link_type_id);
        link_type_id.put("catalog_id", 47);
        link_type_id.put("id", hop_link_type);


        Integer ne_rau_subband = execution.getVariable("ne_rau_subband") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_rau_subband")));
        Integer ne_protection_rau_subband = execution.getVariable("ne_protection_rau_subband") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_protection_rau_subband")));
        Integer ne_siteId = execution.getVariable("ne_siteId") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_siteId")));
        Integer ne_diameter = execution.getVariable("ne_diameter") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_diameter")));
        Integer ne_protection_antenna_diameter = execution.getVariable("ne_protection_antenna_diameter") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_protection_antenna_diameter")));
        Double ne_azimuth = execution.getVariable("ne_azimuth") == null ? null : Double.parseDouble(String.valueOf(execution.getVariable("ne_azimuth")));
        Double path_distance = execution.getVariable("path_distance") == null ? null : Double.parseDouble(String.valueOf(execution.getVariable("path_distance")));
        Integer ne_suspension_height_antennas = execution.getVariable("ne_suspension_height_antennas") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_suspension_height_antennas")));
        Integer ne_protection_suspension_height = execution.getVariable("ne_protection_suspension_height") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_protection_suspension_height")));
        Integer ne_power_level_rx = execution.getVariable("ne_power_level_rx") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_power_level_rx")));
        Integer ne_protection_power_level_rx = execution.getVariable("ne_protection_power_level_rx") == null || String.valueOf(execution.getVariable("ne_protection_power_level_rx")).equals("") ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_protection_power_level_rx")));
        Integer ne_power_level_tx = execution.getVariable("ne_power_level_tx") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_power_level_tx")));
        Integer ne_protection_power_level_tx = execution.getVariable("ne_protection_power_level_tx") == null || String.valueOf(execution.getVariable("ne_protection_power_level_tx")).equals("") ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_protection_power_level_tx")));
        String ne_terminal_id = execution.getVariable("ne_terminal_id") == null ? null : String.valueOf(execution.getVariable("ne_terminal_id")).equals("") ? null : String.valueOf(execution.getVariable("ne_terminal_id"));
        String hop_link_nxe1 = execution.getVariable("hop_link_nxe1") == null ? null : String.valueOf(execution.getVariable("hop_link_nxe1")).equals("") ? null : String.valueOf(execution.getVariable("hop_link_nxe1"));
        Integer ne_txrx_frequincies = execution.getVariable("ne_txrx_frequincies") == null || String.valueOf(execution.getVariable("ne_txrx_frequincies")).equals("") ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_txrx_frequincies")));
        Integer ne_protection_txrx_frequincies = execution.getVariable("ne_protection_txrx_frequincies") == null || String.valueOf(execution.getVariable("ne_protection_txrx_frequincies")).equals("") ? null : Integer.parseInt(String.valueOf(execution.getVariable("ne_protection_txrx_frequincies")));
        Integer hop_polarization = execution.getVariable("hop_polarization") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("hop_polarization")));
        Integer protection_mode = execution.getVariable("protection_mode") == null ? null : Integer.parseInt(String.valueOf(execution.getVariable("protection_mode")));


        objectNode.put("nearend_id", ne_siteId);

        ObjectNode ne_antenna_diameter_id = objectMapper.createObjectNode();
        objectNode.set("ne_antenna_diameter_id", ne_antenna_diameter_id);
        ne_antenna_diameter_id.put("catalog_id", 20);
        ne_antenna_diameter_id.put("id", ne_diameter);

        if (ne_protection_antenna_diameter != null) {
            ObjectNode ne_antenna_diameter_protect_id = objectMapper.createObjectNode();
            objectNode.set("ne_antenna_diameter_protect_id", ne_antenna_diameter_protect_id);
            ne_antenna_diameter_protect_id.put("catalog_id", 20);
            ne_antenna_diameter_protect_id.put("id", ne_protection_antenna_diameter);
        }


        objectNode.put("ne_azimuth", ne_azimuth);
        objectNode.put("ne_different_address", true);
        objectNode.set("ne_facility_id", null);
        objectNode.put("ne_height_susp_antenna", ne_suspension_height_antennas);
        objectNode.put("ne_height_susp_antenna_protect", ne_protection_suspension_height);
        objectNode.put("ne_power_levels_rx", ne_power_level_rx);
        objectNode.put("ne_power_levels_rx_protect", ne_protection_power_level_rx);
        objectNode.put("ne_power_levels_tx", ne_power_level_tx);
        objectNode.put("ne_power_levels_tx_protect", ne_protection_power_level_tx);

        ObjectNode ne_rau_subband_id = objectMapper.createObjectNode();
        objectNode.set("ne_rau_subband_id", ne_rau_subband_id);
        ne_rau_subband_id.put("catalog_id", 46);
        ne_rau_subband_id.put("id", ne_rau_subband);

        if (ne_protection_rau_subband != null) {
            ObjectNode ne_rau_subband_protect_id = objectMapper.createObjectNode();
            objectNode.set("ne_rau_subband_protect_id", ne_rau_subband_protect_id);
            ne_rau_subband_protect_id.put("catalog_id", 46);
            ne_rau_subband_protect_id.put("id", ne_protection_rau_subband);
        }

        objectNode.put("ne_terminalid", ne_terminal_id);
        objectNode.put("ne_txrx_frequencies", ne_txrx_frequincies);
        objectNode.put("fe_txrx_frequencies_protect", ne_protection_txrx_frequincies);

        objectNode.put("nxe1", hop_link_nxe1);
        objectNode.put("path_distance", path_distance);

        ObjectNode polarization_id = objectMapper.createObjectNode();
        objectNode.set("polarization_id", polarization_id);
        polarization_id.put("catalog_id", 48);
        polarization_id.put("id", hop_polarization);

        ObjectNode protection_mode_id = objectMapper.createObjectNode();
        objectNode.set("protection_mode_id", protection_mode_id);
        protection_mode_id.put("catalog_id", 44);
        protection_mode_id.put("id", protection_mode);

        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();
            String path = "https://asset.test-flow.kcell.kz/asset-management/tsd_mw";
            HttpResponse httpResponse = executePost(path, httpclient, objectNode.toString());
            log.info("json :  ----   " + objectNode.toString());
            if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException("asset.flow.kcell.kz returns code " + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            throw new BpmnError("error", e.getMessage());
        }

    }


    private HttpResponse executePost(String url, HttpClient httpClient, String requestBody) throws Exception {
        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setEntity(entity);
        return httpClient.execute(httpPost);
    }
}
