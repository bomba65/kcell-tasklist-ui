package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service("ConfirmReservation")
public class ConfirmReservation implements JavaDelegate {

    private class Reservation {
        public String variableName;
        public boolean isRecurring;

        public Reservation(String variableName, boolean isRecurring) {
            this.variableName = variableName;
            this.isRecurring = isRecurring;
        }
    }

    private static final String URL_ENDING = "/subscriptions/reserves/confirm";

    @Value("${atlas.subscribers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        List<Reservation> reservations = Arrays.asList(
            new Reservation("subscriptionReserveId_5100", false),
            new Reservation("subscriptionReserveId_5101", false),
            new Reservation("subscriptionReserveId_5102", true),
            new Reservation("subscriptionReserveId_5103", true),
            new Reservation("subscriptionReserveId_5104", true));

        String subscriberId = (String) delegateExecution.getVariable("subscriberId");

        for (Reservation reservation : reservations) {
            String reserveId = (String) delegateExecution.getVariable(reservation.variableName);
            if (reserveId != null) {
                URIBuilder uriBuilder = new URIBuilder(atlasUrl + URL_ENDING);

                uriBuilder.setParameter("subscriberId", subscriberId);
                uriBuilder.setParameter("reservationId", reserveId);
                uriBuilder.setParameter("subscriptionType", reservation.isRecurring ? "RECURRING" : "ONETIME");
                uriBuilder.setParameter("productType", reservation.isRecurring ? "PACKS" : "PRICE_ITEMS");

                String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

                HttpPost httpPost = new HttpPost(uriBuilder.build());
                httpPost.setHeader("Authorization", "Basic " + encoding);
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                CloseableHttpResponse response = httpClientWithoutSSL.execute(httpPost);

                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                    log.error("ConfirmReserve, query " + uriBuilder + " for variable " + reservation.variableName + " returns code " + response.getStatusLine().getStatusCode() + "\n" +
                        "Error message: " + EntityUtils.toString(response.getEntity()));
                    delegateExecution.setVariable("unsuccessful", true);
                } else {
                    log.info("ConfirmReserve, query " + uriBuilder + " for variable " + reservation.variableName + " returns code " + response.getStatusLine().getStatusCode());
                }

                response.close();
            }
        }

    }
}
