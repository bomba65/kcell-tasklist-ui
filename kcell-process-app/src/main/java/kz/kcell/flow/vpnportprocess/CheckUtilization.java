package kz.kcell.flow.vpnportprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.catalogs.client.CatalogsClient;
import kz.kcell.flow.catalogs.client.dto.CatalogListDto;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CheckUtilization implements JavaDelegate {

    private final IpVpnConnectService ipVpnConnectService;
    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper vpnPortProcessMapper;
    private final CatalogsClient catalogsClient;
    private final String appBaseUrl;

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static List<String> serviceTypeList = Arrays.asList("Abis", "IuB", "ABIS", "IUB", "MuB", "MUB");

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Map<String, Double> vpns = ipVpnConnectService.findVpnNumbersThatMeetUtilizationCriteria();
        boolean isEmergency = false;
        List<CatalogListDto> serviceTypeIds = catalogsClient.getCatalog(83).getData().getList();

        execution.setVariable("channel", "VPN");
        execution.setVariable("request_type", "Automodify");

        List<VpnCamVar> automodifyServices = new ArrayList<>();
        for (Map.Entry<String, Double> vpn : vpns.entrySet()) {
            VpnOutputDto vpnOutputDto = vpnPortClient.getVpnByVpnNumber(vpn.getKey());

            long serviceTypeId = vpnOutputDto.getServiceTypeId();
            String serviceType = serviceTypeIds.stream()
                    .filter(serviceTypeIdDto -> serviceTypeIdDto.getId() == serviceTypeId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Service type not found"))
                    .getValue();

            int modifiedServiceCapacity = 0;
            if (serviceTypeList.contains(serviceType)) {
                modifiedServiceCapacity = vpnOutputDto.getServiceCapacity() > 600 ? (int) (vpn.getValue() * 100 / 0.75 / 0.9) : (int) (vpn.getValue() * 100 / 0.6 / 0.9);
            } else {
                modifiedServiceCapacity = vpnOutputDto.getServiceCapacity() > 600 ? (int) (vpn.getValue() * 100 / 0.75) : (int) (vpn.getValue() * 100 / 0.6);
            }

            VpnCamVar modifyVpn = vpnPortProcessMapper.map(vpnOutputDto, modifiedServiceCapacity);
            automodifyServices.add(modifyVpn);

            if (vpn.getValue() > 0.9) {
                isEmergency = true;
            }
        }

        execution.setVariable("priority", isEmergency ? "Emergency" : "Regular");
        execution.setVariable("automodifyServices", SpinValues.jsonValue(objectMapper.writeValueAsString(automodifyServices)).create());
        execution.setProcessBusinessKey("VPN-" + getVpnPortCounter());
    }

    private String getVpnPortCounter() throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpPost httpPost = new HttpPost(appBaseUrl + "/asset-management/vpn_port_counter/vpn");
        httpPost.addHeader("Referer", appBaseUrl);
        HttpResponse httpResponse = httpclient.execute(httpPost);

        HttpEntity entity = httpResponse.getEntity();
        String response =  EntityUtils.toString(entity);
        return response.substring(1, response.length() - 1);
    }
}
