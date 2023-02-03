package kz.kcell.flow.assets.client.config;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import kz.kcell.flow.assets.client.VpnPortClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
@RequiredArgsConstructor
public class VpnPortClientConfig {

    private final String assetUrl;

    @Bean
    VpnPortClient vpnPortClient() {
        return Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .target(VpnPortClient.class, assetUrl + "/asset-management");
    }
}
