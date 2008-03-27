package com.sun.enterprise.deploy.jar;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;

/**
 * ArchiveHandler implementation for jar files
 *
 * @author Jerome Dochez
 */
@Service
public class JarHandler extends AbstractArchiveHandler implements ArchiveHandler {
    public String getArchiveType() {
        return "jar";
    }

    public boolean handles(ReadableArchive archive) {
        // I don't handle war files...
        if (DeploymentUtils.isWebArchive(archive)) {
            return false;
        }
        // but I handle everything else
        return true;
    }

    public ClassLoader getClassLoader(ClassLoader parent, ReadableArchive archive) {
        EJBClassLoader cloader = new EJBClassLoader(parent);
        try {              
            cloader.addURL(archive.getURI().toURL());
        } catch(MalformedURLException e) {
            return null;
        }
        return cloader;
    }
}
