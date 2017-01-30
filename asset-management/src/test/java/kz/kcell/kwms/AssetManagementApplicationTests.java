package kz.kcell.kwms;

import kz.kcell.kwms.model.Facility;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import javax.validation.ValidatorFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AssetManagementApplicationTests {

    @Autowired
    DataSource dataSource;

    @Test
	public void testJdbcGeometry() throws Exception {
        Assert.assertNotNull(dataSource);

        try (
                Connection c = dataSource.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("select st_makepoint(10, 20) as point")
        ) {
            rs.next();
            String point = rs.getString("point");
            Assert.assertEquals("POINT (10 20)", point);
        }
    }

    @Autowired
    EntityManager em;

    @Test
    @Transactional
    public void testJpaGeometry() throws Exception {
        Assert.assertNotNull(em);

        em.createQuery("select f from Facility f order by distance(f.location, point(:x, :y))", Facility.class)
                .setParameter("x", -9.0)
                .setParameter("y", 9.0)
                .getResultList()
                .forEach(System.out::println);
    }

    @Test
    public void testRestApi() throws Exception {
        // TODO: call REST api from outside of application
        // try get, post or other metheods
    }


    @TestConfiguration
    public static class TestAssetManagementHibernateJpaAutoConfiguration extends AssetManagementApplication.AssetManagementHibernateJpaAutoConfiguration {
		public TestAssetManagementHibernateJpaAutoConfiguration(
                DataSource dataSource,
                JpaProperties jpaProperties,
                ObjectProvider<JtaTransactionManager> jtaTransactionManager,
                ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers,
                ValidatorFactory validatorFactory) {
			super(dataSource, jpaProperties, jtaTransactionManager, transactionManagerCustomizers, validatorFactory);
		}


		@Override
		@Bean
		public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factoryBuilder) {
            LocalContainerEntityManagerFactoryBean bean = super.entityManagerFactory(factoryBuilder);
            bean.setMappingResources("mappings.xml");
            return bean;
		}
	}

}
