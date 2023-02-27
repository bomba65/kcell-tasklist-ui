package kz.kcell.flow.catalogs.client.config;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import kz.kcell.flow.catalogs.client.CatalogsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
public class CatalogsClientConfig {

    private final String catalogsUrl;

    public CatalogsClientConfig(@Value("${catalogs.url:https://catalogs.test-flow.kcell.kz}") String catalogsUrl) {
        this.catalogsUrl = catalogsUrl;
    }
    @Bean
    CatalogsClient catalogsClient() {
        return Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .requestInterceptor(template -> template.header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .target(CatalogsClient.class, catalogsUrl);
    }
}
