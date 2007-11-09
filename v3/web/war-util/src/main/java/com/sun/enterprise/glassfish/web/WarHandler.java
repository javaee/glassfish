package com.sun.enterprise.glassfish.web;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;
import org.apache.catalina.loader.WebappClassLoader;

import java.io.*;
import java.net.MalformedURLException;

import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;

/**
 * Implementation of the ArchiveHandler for war files.
 *
 * @author Jerome Dochez
 */
@Service
public class WarHandler extends AbstractArchiveHandler implements ArchiveHandler {

    public String getArchiveType() {
        return "war";               
    }

    public boolean handles(ReadableArchive archive) {
        try {
            InputStream is = archive.getEntry("WEB-INF/web.xml");
            if (is!=null) {
                is.close();
                return true;
            }
            return false;
        } catch(IOException e) {
            return false;
        }
    }

    public ClassLoader getClassLoader(ClassLoader parent, ReadableArchive archive) {
        WebappClassLoader cloader = new WebappClassLoader(parent);
        try {
            cloader.addRepository(archive.getURI().toURL().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return cloader;
    }


}
