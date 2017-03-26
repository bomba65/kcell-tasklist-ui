package kz.kcell.camunda.authentication;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.identity.impl.ldap.LdapIdentityProviderSession;
import org.camunda.bpm.identity.impl.ldap.LdapUserQueryImpl;

public class KcellLdapUserQueryImpl extends LdapUserQueryImpl {
    private LdapIdentityProviderSession session;

    public KcellLdapUserQueryImpl(LdapIdentityProviderSession session) {
        this.session = session;
    }

    protected LdapIdentityProviderSession getLdapIdentityProvider(CommandContext commandContext) {
        return session;
    }

}
