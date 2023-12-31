package kz.kcell.flow.assets.client.config;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import kz.kcell.flow.assets.client.AssetsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
@RequiredArgsConstructor
public class AssetsClientConfig {

    private final String assetUrl;

    @Bean
    AssetsClient assetsClient() {
        return Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .requestInterceptor(template -> template.header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .target(AssetsClient.class, assetUrl + "/asset-management");
    }
}
