package kz.kcell.bpm.assignprnumbers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.AssetsClient;
import kz.kcell.flow.assets.dto.JobRequestDto;
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
public class NewPr implements JavaDelegate {
    private final AssetsClient assetsClient;
    private final static Map<String, String> REFERER_HEADER = new SingletonMap<>(REFERER, "http://localhost");
    private final static ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String jrNumber = execution.<StringValue>getVariableTyped("jrNumber").getValue();
        String prNumber = execution.<StringValue>getVariableTyped("prNumber").getValue();
        log.info("jr Number = " + jrNumber + " pr number = " + prNumber);

        JobRequestDto jobRequestDto = assetsClient.createJobRequest(REFERER_HEADER,
            JobRequestDto.builder().jrNumber(jrNumber).build());
        log.info("Job request created: " + mapper.writeValueAsString(jobRequestDto));

        PurchaseRequestDto purchaseRequestDto = assetsClient.createPurchaseRequest(REFERER_HEADER,
            PurchaseRequestDto.builder().prNumber(prNumber).build());
        log.info("Purchase request created: " + mapper.writeValueAsString(purchaseRequestDto));

        JrPrDto jrPrDto = assetsClient.createJrPr(REFERER_HEADER,
            JrPrDto.builder().jrId(jobRequestDto.getId()).prId(purchaseRequestDto.getId()).build());
        log.info("jr_pr created: " + mapper.writeValueAsString(jrPrDto));
    }
}
