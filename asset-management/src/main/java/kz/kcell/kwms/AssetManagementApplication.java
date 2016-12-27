package kz.kcell.kwms;

import kz.kcell.kwms.model.Facility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class AssetManagementApplication {
	public static void main(String[] args) {
        SpringApplication.run(AssetManagementApplication.class, args);
	}
}
