package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.WebServicesDeploymentDescriptorFile;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import org.xml.sax.SAXParseException;

/**
 * Extension Archivist for webservices.
 */
@Service
public class WebServicesArchivist extends ExtensionsArchivist {

    public DeploymentDescriptorFile getStandardDDFile(RootDeploymentDescriptor descriptor) {
        return new WebServicesDeploymentDescriptorFile(descriptor);
    }

    public DeploymentDescriptorFile getConfigurationDDFile(RootDeploymentDescriptor descriptor) {
        return null; 
    }

    public XModuleType getModuleType() {
        return XModuleType.WebServices;
    }

    public boolean supportsModuleType(XModuleType moduleType) {
        return (XModuleType.WAR==moduleType || XModuleType.EJB==moduleType
                || XModuleType.EjbInWar==moduleType);
    }

    @Override
    public Object open(Archivist main, ReadableArchive archive, RootDeploymentDescriptor descriptor) throws IOException, SAXParseException {
        BundleDescriptor bundleDescriptor =
            BundleDescriptor.class.cast(super.open(main, archive, descriptor));

        if (bundleDescriptor != null) {
            return bundleDescriptor.getWebServices();
        } else {
            return null;
        }
    }

    public RootDeploymentDescriptor getDefaultDescriptor() {
        return new WebServicesDescriptor();
    }
}
