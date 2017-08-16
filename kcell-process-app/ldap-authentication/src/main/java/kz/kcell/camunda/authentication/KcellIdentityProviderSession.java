package kz.kcell.camunda.authentication;

import org.camunda.bpm.engine.impl.identity.db.DbIdentityServiceProvider;
import org.camunda.bpm.identity.impl.ldap.LdapConfiguration;
import org.camunda.bpm.identity.impl.ldap.LdapIdentityProviderSession;

import java.util.logging.Logger;

public class KcellIdentityProviderSession extends DbIdentityServiceProvider {

    private final static Logger LOG = Logger.getLogger(KcellIdentityProviderSession.class.getName());

    LdapIdentityProviderSession ldapIdentityProviderSession;


    public KcellIdentityProviderSession(LdapConfiguration ldapConfiguration) {
        ldapIdentityProviderSession = new KcellLdapIdentityProviderSession(ldapConfiguration);
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        return ldapIdentityProviderSession.checkPassword(userId, password)
            || super.checkPassword(userId, password);
    }
}
