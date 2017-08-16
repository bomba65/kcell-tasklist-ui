package kz.kcell.camunda.authentication;

import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.identity.impl.ldap.LdapConfiguration;

public class KcellIdentityProviderFactory implements SessionFactory {

    protected LdapConfiguration ldapConfiguration;

    public Class<?> getSessionType() {
        return WritableIdentityProvider.class;
    }

    public Session openSession() {
        return new KcellIdentityProviderSession(ldapConfiguration);
    }

    public LdapConfiguration getLdapConfiguration() {
        return ldapConfiguration;
    }

    public void setLdapConfiguration(LdapConfiguration ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

}
