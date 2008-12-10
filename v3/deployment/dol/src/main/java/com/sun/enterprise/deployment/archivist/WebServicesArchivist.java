package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.WebServicesDeploymentDescriptorFile;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;

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
}
