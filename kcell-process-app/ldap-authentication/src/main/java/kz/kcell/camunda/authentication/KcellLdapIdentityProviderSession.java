package kz.kcell.camunda.authentication;

import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.identity.impl.ldap.LdapConfiguration;
import org.camunda.bpm.identity.impl.ldap.LdapIdentityProviderSession;
import org.camunda.bpm.identity.impl.ldap.LdapUserQueryImpl;

public class KcellLdapIdentityProviderSession extends LdapIdentityProviderSession {
    public KcellLdapIdentityProviderSession(LdapConfiguration ldapConfiguration) {
        super(ldapConfiguration);
    }

    @Override
    public UserQueryImpl createUserQuery(CommandContext commandContext) {
        return new KcellLdapUserQueryImpl(this);
    }


}
