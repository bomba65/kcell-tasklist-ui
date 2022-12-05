package kz.kcell.flow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.mail.emailDoSend}")
    private boolean emailDoSend;

    @Value("${app.baseurl}")
    private String appBaseUrl;

    @Value("${asset.url}")
    private String assetUrl;

    @Bean boolean isEmailDoSend() {
        return emailDoSend;
    }

    @Bean String appBaseUrl() {
        return appBaseUrl;
    }

    @Bean String assetUrl() {
        return assetUrl;
    }
}
