package kz.kcell.kwms;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.sql.DataSource;
import javax.validation.ValidatorFactory;
import java.util.Map;

@SpringBootApplication
@Configuration
public class DirectoryManagementApplication {
	public static void main(String[] args) {
        SpringApplication.run(DirectoryManagementApplication.class, args);
	}

	@Bean
	public LocalValidatorFactoryBean localValidatorFactoryBean() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public JsonSchemaFactory jsonSchemaFactory() {
		return JsonSchemaFactory.byDefault();
	}


	@Component
	public static class DirectoryManagementRepositoryRestConfigurer extends RepositoryRestConfigurerAdapter {
		@Override
		public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
			objectMapper.registerModule(new JtsModule());
			/**
			 * TODO
			 * Waiting for https://github.com/FasterXML/jackson-databind/issues/1371 in 2.9
			 * This helps to get rid of custom lombok.config
			 *
			 * objectMapper.disable(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES);
			 */
		}
	}

	@Configuration
	public static class DirectoryManagementHibernateJpaAutoConfiguration extends HibernateJpaAutoConfiguration {
		ValidatorFactory validatorFactory;

		public DirectoryManagementHibernateJpaAutoConfiguration(
				DataSource dataSource,
				JpaProperties jpaProperties,
				ObjectProvider<JtaTransactionManager> jtaTransactionManager,
				ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers,
				ValidatorFactory validatorFactory) {
			super(dataSource, jpaProperties, jtaTransactionManager, transactionManagerCustomizers);
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected void customizeVendorProperties(Map<String, Object> vendorProperties) {
			super.customizeVendorProperties(vendorProperties);
			vendorProperties.put("javax.persistence.validation.factory", validatorFactory);
		}
	}
}
