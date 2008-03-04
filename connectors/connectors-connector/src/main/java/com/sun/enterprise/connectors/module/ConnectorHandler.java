package com.sun.enterprise.connectors.module;

import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.io.InputStream;


@Service
public class ConnectorHandler extends AbstractArchiveHandler implements ArchiveHandler {

    public String getArchiveType() {
        return "rar";
    }

    public boolean handles(ReadableArchive archive) {
        try {
            InputStream is = archive.getEntry("META-INF/ra.xml");
            if (is != null) {
                is.close();
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    //TODO V3 should this be connector-class-loader ? Purpose of this method ? Who uses this classloader ?
    //TODO V3 guess: purpose is for deploymentContext & Sniffer.handles() purpose only 

    public ClassLoader getClassLoader(ClassLoader parent, ReadableArchive archive) {
        //TODO V3 temp
        // return ConnectorClassLoader.getInstance(parent);
        return Thread.currentThread().getContextClassLoader();
    }
}
