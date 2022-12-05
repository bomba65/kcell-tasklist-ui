package kz.kcell.camunda.authentication;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.identity.db.DbIdentityServiceProvider;
import org.camunda.bpm.engine.impl.identity.db.DbUserQueryImpl;
import org.camunda.bpm.identity.impl.ldap.LdapConfiguration;
import org.camunda.bpm.identity.impl.ldap.LdapIdentityProviderSession;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class KcellIdentityProviderSession extends DbIdentityServiceProvider {

    private final static Logger LOG = Logger.getLogger(KcellIdentityProviderSession.class.getName());

    LdapIdentityProviderSession ldapIdentityProviderSession;
    LdapIdentityProviderSession externalLdapIdentityProviderSession;
    ProcessEngine processEngine;

    public KcellIdentityProviderSession(LdapConfiguration ldapConfiguration, LdapConfiguration externalLdapConfiguration) {
        if(ldapConfiguration!=null){
            ldapIdentityProviderSession = new KcellLdapIdentityProviderSession(ldapConfiguration);
        }
        if(externalLdapConfiguration!=null) {
            externalLdapIdentityProviderSession = new KcellLdapIdentityProviderSession(externalLdapConfiguration);
        }
        processEngine = ProcessEngines.getDefaultProcessEngine();
    }

    private Properties getSqlSessionFactoryProperties(ProcessEngineConfigurationImpl conf) {
        Properties properties = new Properties();
        ProcessEngineConfigurationImpl.initSqlSessionFactoryProperties(properties, conf.getDatabaseTablePrefix(), conf.getDatabaseType());
        return properties;
    }

    private SqlSessionFactory createMyBatisSqlSessionFactory(InputStream config) {

        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        DataSource dataSource = processEngineConfiguration.getDataSource();

        // use this transaction factory if you work in a non transactional
        // environment
        // TransactionFactory transactionFactory = new JdbcTransactionFactory();

        // use this transaction factory if you work in a transactional
        // environment (e.g. called within the engine or using JTA)
        TransactionFactory transactionFactory = new ManagedTransactionFactory();

        Environment environment = new Environment("customTasks", transactionFactory, dataSource);

        XMLConfigBuilder parser = new XMLConfigBuilder(
            new InputStreamReader(config),
            "",
            getSqlSessionFactoryProperties((ProcessEngineConfigurationImpl) processEngineConfiguration));
        Configuration configuration = parser.getConfiguration();
        configuration.setEnvironment(environment);
        configuration = parser.parse();

        configuration.setDefaultStatementTimeout(processEngineConfiguration.getJdbcStatementTimeout());

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        return sqlSessionFactory;
    }

    private String getSingleSatisfiedUser (List<String> userIdList, String userId){

        if (userIdList.isEmpty()){
            return userId;
        } else {
            return userIdList.get(0);
        }
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        InputStream config = this.getClass().getResourceAsStream("/customAuthentication/customMybatisConfiguration.xml");

        SqlSessionFactory sqlSessionFactory = createMyBatisSqlSessionFactory(config);

        SqlSession session = sqlSessionFactory.openSession();
        try {
            List<String> userList = session.selectList("customUserUpperCase.selectUser", userId);
            userId = getSingleSatisfiedUser(userList, userId);

        } finally {
            session.close();
        }

        return (ldapIdentityProviderSession!=null && ldapIdentityProviderSession.checkPassword(userId, password))
            || (externalLdapIdentityProviderSession!=null && externalLdapIdentityProviderSession.checkPassword(userId, password))
            || super.checkPassword(userId, password);
    }

    @Override
    public List<User> findUserByQueryCriteria(DbUserQueryImpl query) {

        if(processEngine.getIdentityService().getCurrentAuthentication()==null){

            InputStream config = this.getClass().getResourceAsStream("/customAuthentication/customMybatisConfiguration.xml");
            SqlSessionFactory sqlSessionFactory = createMyBatisSqlSessionFactory(config);
            SqlSession session = sqlSessionFactory.openSession();

            List<User> users = new ArrayList<>();
            try {
                configureQuery(query, Resources.USER);
                users = session.selectList("customUserUpperCase.selectUserByQueryCriteria", query);
           } finally {
                session.close();
            }
            return users;
        } else {
            return super.findUserByQueryCriteria(query);
        }
    }
}
