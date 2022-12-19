package kz.kcell.bpm.assignprnumbers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.AssetsClient;
import kz.kcell.flow.assets.dto.JrPrDto;
import kz.kcell.flow.assets.dto.PurchaseRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.SingletonMap;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.http.HttpHeaders.REFERER;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExistingPr implements JavaDelegate {
    private final AssetsClient assetsClient;
    private final static Map<String, String> REFERER_HEADER = new SingletonMap<>(REFERER, "http://localhost");
    private final static ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String jrPrId = execution.<StringValue>getVariableTyped("jrPrId").getValue();
        String prNumber = execution.<StringValue>getVariableTyped("prNumber").getValue();
        log.info("Pr number = " + prNumber + " jr_pr id = " + jrPrId);

        PurchaseRequestDto purchaseRequestDto = assetsClient.createPurchaseRequest(REFERER_HEADER,
            PurchaseRequestDto.builder().prNumber(prNumber).build());
        log.info("Purchase request created: " + mapper.writeValueAsString(purchaseRequestDto));

        JrPrDto jrPrDto = assetsClient.putJrPr(REFERER_HEADER, Long.parseLong(jrPrId),
            JrPrDto.builder().prId(purchaseRequestDto.getId()).build());
        log.info("jr_pr updated: " + mapper.writeValueAsString(jrPrDto));
    }
}
