package kz.kcell.camunda.authentication.plugin;

import kz.kcell.camunda.authentication.KcellIdentityProviderFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.identity.impl.ldap.LdapConfiguration;
import org.camunda.bpm.identity.impl.ldap.util.CertificateHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KcellIdentityProviderPlugin extends LdapConfiguration implements ProcessEnginePlugin {
    protected Logger LOG = Logger.getLogger(KcellIdentityProviderPlugin.class.getName());



    protected boolean acceptUntrustedCertificates = false;

    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

        LOG.log(Level.INFO, "PLUGIN {0} activated on process engine {1}", new String[]{getClass().getSimpleName(), processEngineConfiguration.getProcessEngineName()});

        if(acceptUntrustedCertificates) {
            CertificateHelper.acceptUntrusted();
            LOG.log(Level.WARNING, "Enabling accept of untrusted certificates. Use at own risk.");
        }

        KcellIdentityProviderFactory kcellIdentityProvider = new KcellIdentityProviderFactory();
        kcellIdentityProvider.setLdapConfiguration(this);
        processEngineConfiguration.setIdentityProviderSessionFactory(kcellIdentityProvider);
    }

    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        // nothing to do
    }

    public void postProcessEngineBuild(ProcessEngine processEngine) {
        // nothing to do
    }

    public void setAcceptUntrustedCertificates(boolean acceptUntrustedCertificates) {
        this.acceptUntrustedCertificates = acceptUntrustedCertificates;
    }

    public boolean isAcceptUntrustedCertificates() {
        return acceptUntrustedCertificates;
    }
}
