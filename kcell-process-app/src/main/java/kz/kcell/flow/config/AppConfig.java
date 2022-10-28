package kz.kcell.flow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.mail.emailDoSend}")
    private boolean emailDoSend;

    @Bean boolean isEmailDoSend() {
        return emailDoSend;
    }

}
