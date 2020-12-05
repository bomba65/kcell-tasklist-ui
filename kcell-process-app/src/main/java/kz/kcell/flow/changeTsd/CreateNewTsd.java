package kz.kcell.flow.changeTsd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import static org.camunda.spin.Spin.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.*;

@Log
@Service("CreateNewTsd")
public class CreateNewTsd implements JavaDelegate {

    @Value("${asset.url:https://asset.test-flow.kcell.kz}")
    private String assetsUri;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        log.info("Add new data into DB");
        log.info(assetsUri);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        SpinJsonNode newTsd = execution.<JsonValue>getVariableTyped("selectedTsd").getValue();

        String tsdId = String.valueOf(newTsd.prop("id"));

        SpinJsonNode capacityObj = !newTsd.hasProp("capacity_id") || newTsd.prop("capacity_id") == null ? null : newTsd.prop("capacity_id");
        if (capacityObj!=null && capacityObj.hasProp("id")) {
            Integer capacityId = capacityObj.prop("id").numberValue().intValue();
            ObjectNode capacity_id = objectMapper.createObjectNode();
            objectNode.set("capacity_id", capacity_id);
            capacity_id.put("catalog_id", 45);
            capacity_id.put("id", capacityId);
        }


        Long timestamp = newTsd.prop("date").numberValue().longValue();
        Date date = new Date(timestamp);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR, 6);
        objectNode.put("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(c.getTime()));

        // fe
        SpinJsonNode farendObj = !newTsd.hasProp("farend_id") || newTsd.prop("farend_id") == null ? null : newTsd.prop("farend_id");
        if (farendObj!=null && farendObj.hasProp("id")){
            Integer farendId = farendObj.prop("id").numberValue().intValue();
            objectNode.put("farend_id", farendId);
        }

        SpinJsonNode feAntennaDiametrObj = !newTsd.hasProp("fe_antenna_diameter_id") || newTsd.prop("fe_antenna_diameter_id") == null ? null : newTsd.prop("fe_antenna_diameter_id");
        if (feAntennaDiametrObj!=null && feAntennaDiametrObj.hasProp("id")) {
            Integer feAntennaDiameterId = feAntennaDiametrObj.prop("id").numberValue().intValue();
            ObjectNode fe_antenna_diameter_id = objectMapper.createObjectNode();
            objectNode.set("fe_antenna_diameter_id", fe_antenna_diameter_id);
            fe_antenna_diameter_id.put("catalog_id", 20);
            fe_antenna_diameter_id.put("id", feAntennaDiameterId);
        }

        SpinJsonNode feAntennaDiametrProtectObj = !newTsd.hasProp("fe_antenna_diameter_protect_id") || newTsd.prop("fe_antenna_diameter_protect_id") == null ? null : newTsd.prop("fe_antenna_diameter_protect_id");
        if (feAntennaDiametrProtectObj!=null && feAntennaDiametrProtectObj.hasProp("id")) {
            Integer feAntennaDiametrProtectId = feAntennaDiametrProtectObj.prop("id").numberValue().intValue();
            ObjectNode fe_antenna_diameter_protect_id = objectMapper.createObjectNode();
            objectNode.set("fe_antenna_diameter_protect_id", fe_antenna_diameter_protect_id);
            fe_antenna_diameter_protect_id.put("catalog_id", 20);
            fe_antenna_diameter_protect_id.put("id", feAntennaDiametrProtectId);
        }

        Double fe_azimuth = !newTsd.hasProp("fe_azimuth") || newTsd.prop("fe_azimuth").isNull() ? null : newTsd.prop("fe_azimuth").isString() ? Double.parseDouble(newTsd.prop("fe_azimuth").stringValue()) : newTsd.prop("fe_azimuth").numberValue().doubleValue();
        objectNode.put("fe_azimuth", fe_azimuth);

        Boolean fe_different_address = !newTsd.hasProp("fe_different_address") || newTsd.prop("fe_different_address").isNull() ? null : newTsd.prop("fe_different_address").boolValue();
        objectNode.put("fe_different_address", fe_different_address);

        SpinJsonNode feFacilityObj = !newTsd.hasProp("fe_facility_id") || newTsd.prop("fe_facility_id") == null ? null : newTsd.prop("fe_facility_id");
        if (feFacilityObj!=null && feFacilityObj.hasProp("id")) {
            Integer feFacilityId = feFacilityObj.prop("id").numberValue().intValue();
            objectNode.put("fe_facility_id", feFacilityId);
        }

        Integer fe_height_susp_antenna = !newTsd.hasProp("fe_height_susp_antenna") || newTsd.prop("fe_height_susp_antenna").isNull() ? null : newTsd.prop("fe_height_susp_antenna").isString() ? Integer.parseInt(newTsd.prop("fe_height_susp_antenna").stringValue()) : newTsd.prop("fe_height_susp_antenna").numberValue().intValue();
        objectNode.put("fe_height_susp_antenna", fe_height_susp_antenna);

        Integer fe_height_susp_antenna_protect = !newTsd.hasProp("fe_height_susp_antenna_protect") || newTsd.prop("fe_height_susp_antenna_protect").isNull() ? null : newTsd.prop("fe_height_susp_antenna_protect").isString() ? Integer.parseInt(newTsd.prop("fe_height_susp_antenna_protect").stringValue()) : newTsd.prop("fe_height_susp_antenna_protect").numberValue().intValue();
        objectNode.put("fe_height_susp_antenna_protect", fe_height_susp_antenna_protect);

        Integer fe_power_levels_rx = !newTsd.hasProp("fe_power_levels_rx") || newTsd.prop("fe_power_levels_rx").isNull() ? null : newTsd.prop("fe_power_levels_rx").isString() ? Integer.parseInt(newTsd.prop("fe_power_levels_rx").stringValue()) : newTsd.prop("fe_power_levels_rx").numberValue().intValue();
        objectNode.put("fe_power_levels_rx", fe_power_levels_rx);

        Integer fe_power_levels_rx_protect = !newTsd.hasProp("fe_power_levels_rx_protect") || newTsd.prop("fe_power_levels_rx_protect").isNull() ? null : newTsd.prop("fe_power_levels_rx_protect").isString() ? Integer.parseInt(newTsd.prop("fe_power_levels_rx_protect").stringValue()) : newTsd.prop("fe_power_levels_rx_protect").numberValue().intValue();
        objectNode.put("fe_power_levels_rx_protect", fe_power_levels_rx_protect);

        Integer fe_power_levels_tx = !newTsd.hasProp("fe_power_levels_tx") || newTsd.prop("fe_power_levels_tx").isNull() ? null : newTsd.prop("fe_power_levels_tx").isString() ? Integer.parseInt(newTsd.prop("fe_power_levels_tx").stringValue()) : newTsd.prop("fe_power_levels_tx").numberValue().intValue();
        objectNode.put("fe_power_levels_tx", fe_power_levels_tx);

        Integer fe_power_levels_tx_protect = !newTsd.hasProp("fe_power_levels_tx_protect") || newTsd.prop("fe_power_levels_tx_protect").isNull() ? null : newTsd.prop("fe_power_levels_tx_protect").isString() ? Integer.parseInt(newTsd.prop("fe_power_levels_tx_protect").stringValue()) : newTsd.prop("fe_power_levels_tx_protect").numberValue().intValue();
        objectNode.put("fe_power_levels_tx_protect", fe_power_levels_tx_protect);

        SpinJsonNode feRauSubbandObj = !newTsd.hasProp("fe_rau_subband_id") || newTsd.prop("fe_rau_subband_id") == null ? null : newTsd.prop("fe_rau_subband_id");
        if (feRauSubbandObj!=null && feRauSubbandObj.hasProp("id")) {
            Integer feRauSubbandId = feRauSubbandObj.prop("id").numberValue().intValue();
            ObjectNode fe_rau_subband_id = objectMapper.createObjectNode();
            objectNode.set("fe_rau_subband_id", fe_rau_subband_id);
            fe_rau_subband_id.put("catalog_id", 46);
            fe_rau_subband_id.put("id", feRauSubbandId);
        }

        SpinJsonNode feRauSubbandProtectObj = !newTsd.hasProp("fe_rau_subband_protect_id") || newTsd.prop("fe_rau_subband_protect_id") == null ? null : newTsd.prop("fe_rau_subband_protect_id");
        if (feRauSubbandProtectObj!=null && feRauSubbandProtectObj.hasProp("id")) {
            Integer feRauSubbandProtectId = feRauSubbandProtectObj.prop("id").numberValue().intValue();
            ObjectNode fe_rau_subband_protect_id = objectMapper.createObjectNode();
            objectNode.set("fe_rau_subband_protect_id", fe_rau_subband_protect_id);
            fe_rau_subband_protect_id.put("catalog_id", 46);
            fe_rau_subband_protect_id.put("id", feRauSubbandProtectId);
        }

        String fe_terminalid = !newTsd.hasProp("fe_terminalid") || newTsd.prop("fe_terminalid").isNull() ? null : newTsd.prop("fe_terminalid").stringValue();
        objectNode.put("fe_terminalid", fe_terminalid);

        Integer fe_txrx_frequencies = !newTsd.hasProp("fe_txrx_frequencies") || newTsd.prop("fe_txrx_frequencies").isNull() ? null : newTsd.prop("fe_txrx_frequencies").isString() ? Integer.parseInt(newTsd.prop("fe_txrx_frequencies").stringValue()) : newTsd.prop("fe_txrx_frequencies").numberValue().intValue();
        objectNode.put("fe_txrx_frequencies", fe_txrx_frequencies);

        Integer fe_txrx_frequencies_protect = !newTsd.hasProp("fe_txrx_frequencies_protect") || newTsd.prop("fe_txrx_frequencies_protect").isNull() ? null : newTsd.prop("fe_txrx_frequencies_protect").isString() ? Integer.parseInt(newTsd.prop("fe_txrx_frequencies_protect").stringValue()) : newTsd.prop("fe_txrx_frequencies_protect").numberValue().intValue();
        objectNode.put("fe_txrx_frequencies_protect", fe_txrx_frequencies_protect);

        SpinJsonNode linkTypeObj = !newTsd.hasProp("link_type_id") || newTsd.prop("link_type_id") == null ? null : newTsd.prop("link_type_id");
        if (linkTypeObj!=null && linkTypeObj.hasProp("id")) {
            Integer linkTypeId = linkTypeObj.prop("id").numberValue().intValue();
            ObjectNode link_type_id = objectMapper.createObjectNode();
            objectNode.set("link_type_id", link_type_id);
            link_type_id.put("catalog_id", 47);
            link_type_id.put("id", linkTypeId);
        }

        //ne
        SpinJsonNode nearendObj = !newTsd.hasProp("nearend_id") || newTsd.prop("nearend_id") == null ? null : newTsd.prop("nearend_id");
        if (nearendObj!=null && nearendObj.hasProp("id")) {
            Integer nearendId = nearendObj.prop("id").numberValue().intValue();
            objectNode.put("nearend_id", nearendId);
        }

        SpinJsonNode neAntennaDiametrObj = !newTsd.hasProp("ne_antenna_diameter_id") || newTsd.prop("ne_antenna_diameter_id") == null ? null : newTsd.prop("ne_antenna_diameter_id");
        if (neAntennaDiametrObj!=null && neAntennaDiametrObj.hasProp("id")) {
            Integer neAntennaDiameterId = neAntennaDiametrObj.prop("id").numberValue().intValue();
            ObjectNode ne_antenna_diameter_id = objectMapper.createObjectNode();
            objectNode.set("ne_antenna_diameter_id", ne_antenna_diameter_id);
            ne_antenna_diameter_id.put("catalog_id", 20);
            ne_antenna_diameter_id.put("id", neAntennaDiameterId);
        }


        SpinJsonNode neAntennaDiametrProtectObj = !newTsd.hasProp("ne_antenna_diameter_protect_id") || newTsd.prop("ne_antenna_diameter_protect_id") == null ? null : newTsd.prop("ne_antenna_diameter_protect_id");
        if (neAntennaDiametrProtectObj!=null && neAntennaDiametrProtectObj.hasProp("id")) {
            Integer neAntennaDiametrProtectId = neAntennaDiametrProtectObj.prop("id").numberValue().intValue();
            ObjectNode ne_antenna_diameter_protect_id = objectMapper.createObjectNode();
            objectNode.set("ne_antenna_diameter_protect_id", ne_antenna_diameter_protect_id);
            ne_antenna_diameter_protect_id.put("catalog_id", 20);
            ne_antenna_diameter_protect_id.put("id", neAntennaDiametrProtectId);
        }


        Double ne_azimuth = !newTsd.hasProp("ne_azimuth") || newTsd.prop("ne_azimuth").isNull() ? null : newTsd.prop("ne_azimuth").isString() ? Double.parseDouble(newTsd.prop("ne_azimuth").stringValue()) : newTsd.prop("ne_azimuth").numberValue().doubleValue();
        objectNode.put("ne_azimuth", ne_azimuth);

        Boolean ne_different_address = !newTsd.hasProp("ne_different_address") || newTsd.prop("ne_different_address").isNull() ? null : newTsd.prop("ne_different_address").boolValue();
        objectNode.put("ne_different_address", ne_different_address);

        SpinJsonNode neFacilityObj = !newTsd.hasProp("ne_facility_id") || newTsd.prop("ne_facility_id") == null ? null : newTsd.prop("ne_facility_id");
        if (neFacilityObj!=null && neFacilityObj.hasProp("id")) {
            Integer neFacilityId = neFacilityObj.prop("id").numberValue().intValue();
            objectNode.put("ne_facility_id", neFacilityId);
        }

        Integer ne_height_susp_antenna = !newTsd.hasProp("ne_height_susp_antenna") || newTsd.prop("ne_height_susp_antenna").isNull() ? null : newTsd.prop("ne_height_susp_antenna").isString() ? Integer.parseInt(newTsd.prop("ne_height_susp_antenna").stringValue()) : newTsd.prop("ne_height_susp_antenna").numberValue().intValue();
        objectNode.put("ne_height_susp_antenna", ne_height_susp_antenna);

        Integer ne_height_susp_antenna_protect = !newTsd.hasProp("ne_height_susp_antenna_protect") || newTsd.prop("ne_height_susp_antenna_protect").isNull() ? null : newTsd.prop("ne_height_susp_antenna_protect").isString() ? Integer.parseInt(newTsd.prop("ne_height_susp_antenna_protect").stringValue()) : newTsd.prop("ne_height_susp_antenna_protect").numberValue().intValue();
        objectNode.put("ne_height_susp_antenna_protect", ne_height_susp_antenna_protect);

        Integer ne_power_levels_rx = !newTsd.hasProp("ne_power_levels_rx") || newTsd.prop("ne_power_levels_rx").isNull() ? null : newTsd.prop("ne_power_levels_rx").isString() ? Integer.parseInt(newTsd.prop("ne_power_levels_rx").stringValue()) : newTsd.prop("ne_power_levels_rx").numberValue().intValue();
        objectNode.put("ne_power_levels_rx", ne_power_levels_rx);

        Integer ne_power_levels_rx_protect = !newTsd.hasProp("ne_power_levels_rx_protect") || newTsd.prop("ne_power_levels_rx_protect").isNull() ? null : newTsd.prop("ne_power_levels_rx_protect").isString() ? Integer.parseInt(newTsd.prop("ne_power_levels_rx_protect").stringValue()) : newTsd.prop("ne_power_levels_rx_protect").numberValue().intValue();
        objectNode.put("ne_power_levels_rx_protect", ne_power_levels_rx_protect);

        Integer ne_power_levels_tx = !newTsd.hasProp("ne_power_levels_tx") || newTsd.prop("ne_power_levels_tx").isNull() ? null : newTsd.prop("ne_power_levels_tx").isString() ? Integer.parseInt(newTsd.prop("ne_power_levels_tx").stringValue()) : newTsd.prop("ne_power_levels_tx").numberValue().intValue();
        objectNode.put("ne_power_levels_tx", ne_power_levels_tx);

        Integer ne_power_levels_tx_protect = !newTsd.hasProp("ne_power_levels_tx_protect") || newTsd.prop("ne_power_levels_tx_protect").isNull() ? null : newTsd.prop("ne_power_levels_tx_protect").isString() ? Integer.parseInt(newTsd.prop("ne_power_levels_tx_protect").stringValue()) : newTsd.prop("ne_power_levels_tx_protect").numberValue().intValue();
        objectNode.put("ne_power_levels_tx_protect", ne_power_levels_tx_protect);

        SpinJsonNode neRauSubbandObj = !newTsd.hasProp("ne_rau_subband_id") || newTsd.prop("ne_rau_subband_id") == null ? null : newTsd.prop("ne_rau_subband_id");
        if (neRauSubbandObj!=null && neRauSubbandObj.hasProp("id")) {
            Integer neRauSubbandId = neRauSubbandObj.prop("id").numberValue().intValue();
            ObjectNode ne_rau_subband_id = objectMapper.createObjectNode();
            objectNode.set("ne_rau_subband_id", ne_rau_subband_id);
            ne_rau_subband_id.put("catalog_id", 46);
            ne_rau_subband_id.put("id", neRauSubbandId);
        }

        SpinJsonNode neRauSubbandProtectObj = !newTsd.hasProp("ne_rau_subband_protect_id") || newTsd.prop("ne_rau_subband_protect_id") == null ? null : newTsd.prop("ne_rau_subband_protect_id");
        if (neRauSubbandProtectObj!=null && neRauSubbandProtectObj.hasProp("id")) {
            Integer neRauSubbandProtectId = neRauSubbandProtectObj.prop("id").numberValue().intValue();
            ObjectNode ne_rau_subband_protect_id = objectMapper.createObjectNode();
            objectNode.set("ne_rau_subband_protect_id", ne_rau_subband_protect_id);
            ne_rau_subband_protect_id.put("catalog_id", 46);
            ne_rau_subband_protect_id.put("id", neRauSubbandProtectId);
        }

        String ne_terminalid = !newTsd.hasProp("ne_terminalid") || newTsd.prop("ne_terminalid").isNull() ? null : newTsd.prop("ne_terminalid").stringValue();
        objectNode.put("ne_terminalid", ne_terminalid);

        Integer ne_txrx_frequencies = !newTsd.hasProp("ne_txrx_frequencies") || newTsd.prop("ne_txrx_frequencies").isNull() ? null : newTsd.prop("ne_txrx_frequencies").isString() ? Integer.parseInt(newTsd.prop("ne_txrx_frequencies").stringValue()) : newTsd.prop("ne_txrx_frequencies").numberValue().intValue();
        objectNode.put("ne_txrx_frequencies", ne_txrx_frequencies);

        Integer ne_txrx_frequencies_protect = !newTsd.hasProp("ne_txrx_frequencies_protect") || newTsd.prop("ne_txrx_frequencies_protect").isNull() ? null : newTsd.prop("ne_txrx_frequencies_protect").isString() ? Integer.parseInt(newTsd.prop("ne_txrx_frequencies_protect").stringValue()) : newTsd.prop("ne_txrx_frequencies_protect").numberValue().intValue();
        objectNode.put("ne_txrx_frequencies_protect", ne_txrx_frequencies_protect);

        Integer nxe1 = !newTsd.hasProp("nxe1") || newTsd.prop("nxe1").isNull() ? null : newTsd.prop("nxe1").isString() ? Integer.parseInt(newTsd.prop("nxe1").stringValue()) : newTsd.prop("nxe1").numberValue().intValue();
        objectNode.put("nxe1", nxe1);

        Double path_distance = !newTsd.hasProp("path_distance") || newTsd.prop("path_distance") == null ? null : newTsd.prop("path_distance").isString() ? Double.parseDouble(newTsd.prop("path_distance").stringValue()) : newTsd.prop("path_distance").numberValue().doubleValue();
        objectNode.put("path_distance", path_distance);

        SpinJsonNode polarizationObj = !newTsd.hasProp("polarization_id") || newTsd.prop("polarization_id") == null ? null : newTsd.prop("polarization_id");
        if (polarizationObj!=null && polarizationObj.hasProp("id")) {
            Integer polarizationId = polarizationObj.prop("id").numberValue().intValue();
            ObjectNode polarization_id = objectMapper.createObjectNode();
            objectNode.set("polarization_id", polarization_id);
            polarization_id.put("catalog_id", 48);
            polarization_id.put("id", polarizationId);
        }

        SpinJsonNode protectionModeObj = !newTsd.hasProp("protection_mode_id") || newTsd.prop("protection_mode_id") == null ? null : newTsd.prop("protection_mode_id");
        if (protectionModeObj!=null && protectionModeObj.hasProp("id")) {
            Integer protectionModeId = protectionModeObj.prop("id").numberValue().intValue();
            ObjectNode protection_mode_id = objectMapper.createObjectNode();
            objectNode.set("protection_mode_id", protection_mode_id);
            protection_mode_id.put("catalog_id", 44);
            protection_mode_id.put("id", protectionModeId);
        }

        objectNode.put("status", "New");

        // collect facilities
        ObjectNode farEndFacility = objectMapper.createObjectNode();

        SpinJsonNode farEndFacilityObj = farendObj.prop("facility_id");
        Integer farEndFacilityId = farEndFacilityObj.prop("id").numberValue().intValue();
        String farEndFacilityLongitude = farEndFacilityObj.prop("longitude").stringValue();
        String farEndFacilityLatitude = farEndFacilityObj.prop("latitude").stringValue();
        Double farEndFacilityAltitude = farEndFacilityObj.prop("altitude").numberValue().doubleValue();
        Integer farEndFacilityConstructionHeight = farEndFacilityObj.prop("construction_height").isString() ? Integer.parseInt(farEndFacilityObj.prop("construction_height").stringValue()) : farEndFacilityObj.prop("construction_height").numberValue().intValue();
        SpinJsonNode farEndFacilityConstructionHeightType = farEndFacilityObj.prop("construction_type_id") == null ? null : farEndFacilityObj.prop("construction_type_id");
        if (farEndFacilityConstructionHeightType.hasProp("id")) {
            Integer farEndFacilityConstructionHeightTypeId = farEndFacilityConstructionHeightType.prop("id").numberValue().intValue();
            ObjectNode farEndConstructionTypeId = objectMapper.createObjectNode();
            farEndFacility.set("construction_type_id", farEndConstructionTypeId);
            farEndConstructionTypeId.put("catalog_id", 14);
            farEndConstructionTypeId.put("id", farEndFacilityConstructionHeightTypeId);
        }
        farEndFacility.put("altitude", farEndFacilityAltitude);
        farEndFacility.put("longitude", farEndFacilityLongitude);
        farEndFacility.put("latitude", farEndFacilityLatitude);
        farEndFacility.put("construction_height", farEndFacilityConstructionHeight);


        ObjectNode nearEndFacility = objectMapper.createObjectNode();

        SpinJsonNode nearEndFacilityObj = nearendObj.prop("facility_id");
        Integer nearEndFacilityId = nearEndFacilityObj.prop("id").numberValue().intValue();
        String nearEndFacilityLongitude = nearEndFacilityObj.prop("longitude").stringValue();
        String nearEndFacilityLatitude = nearEndFacilityObj.prop("latitude").stringValue();
        Double nearEndFacilityAltitude = nearEndFacilityObj.prop("altitude").numberValue().doubleValue();
        Integer nearEndFacilityConstructionHeight = nearEndFacilityObj.prop("construction_height").isString() ? Integer.parseInt(nearEndFacilityObj.prop("construction_height").stringValue()) : nearEndFacilityObj.prop("construction_height").numberValue().intValue();
        SpinJsonNode nearEndFacilityConstructionHeightType = nearEndFacilityObj.prop("construction_type_id") == null ? null : nearEndFacilityObj.prop("construction_type_id");
        if (nearEndFacilityConstructionHeightType.hasProp("id")) {
            Integer nearEndFacilityConstructionHeightTypeId = nearEndFacilityConstructionHeightType.prop("id").numberValue().intValue();
            ObjectNode nearEndConstructionTypeId = objectMapper.createObjectNode();
            nearEndFacility.set("construction_type_id", nearEndConstructionTypeId);
            nearEndConstructionTypeId.put("catalog_id", 14);
            nearEndConstructionTypeId.put("id", nearEndFacilityConstructionHeightTypeId);
        }
        nearEndFacility.put("altitude", nearEndFacilityAltitude);
        nearEndFacility.put("longitude", nearEndFacilityLongitude);
        nearEndFacility.put("latitude", nearEndFacilityLatitude);
        nearEndFacility.put("construction_height", nearEndFacilityConstructionHeight);


            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

            String path = assetsUri + "/asset-management/tsd_mw/id/" + tsdId + "/nearend_id/%7Bnearend_id%7D/farend_id/%7Bfarend_id%7D";
            HttpResponse httpResponse = executePut(path, httpclient, objectNode.toString());
            String response = EntityUtils.toString(httpResponse.getEntity());
            log.info("json:  ----   " + objectNode.toString());
            log.info("response:  ----   " + response);
            if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException("asset.flow.kcell.kz returns code(Add new data into DB) " + httpResponse.getStatusLine().getStatusCode());
            }
            JSONObject json = new JSONObject(response);
            execution.setVariable("newTsdId", json.getString("id"));

            String pathFacilities = assetsUri + "/asset-management/facilities";
            HttpResponse httpResponseFarEndFacilities = executePut(pathFacilities + "/id/" + String.valueOf(farEndFacilityId), httpclient, farEndFacility.toString());
            String responseFarEndFacilities = EntityUtils.toString(httpResponseFarEndFacilities.getEntity());
            log.info("responsePutFarEndFacilities :  ----   " + responseFarEndFacilities);
            if (httpResponseFarEndFacilities.getStatusLine().getStatusCode() < 200 || httpResponseFarEndFacilities.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException("asset.flow.kcell.kz returns code(put far_end facilities) " + httpResponseFarEndFacilities.getStatusLine().getStatusCode());
            }

            HttpResponse httpResponseNearEndFacilities = executePut(pathFacilities + "/id/" + String.valueOf(nearEndFacilityId), httpclient, nearEndFacility.toString());
            String responseNearEndFacilities = EntityUtils.toString(httpResponseNearEndFacilities.getEntity());
            log.info("responsePutNearEndFacilities :  ----   " + responseNearEndFacilities);
            if (httpResponseNearEndFacilities.getStatusLine().getStatusCode() < 200 || httpResponseNearEndFacilities.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException("asset.flow.kcell.kz returns code(put near_end facilities) " + httpResponseNearEndFacilities.getStatusLine().getStatusCode());
            }



    }

    private HttpResponse executePut(String url, HttpClient httpClient, String requestBody) throws Exception {
        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPut.setEntity(entity);
        return httpClient.execute(httpPut);

    }

}
