package com.sun.enterprise.glassfish.web;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Service;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.LifecycleException;
import org.apache.naming.resources.FileDirContext;

import java.io.*;
import java.net.MalformedURLException;
import java.util.jar.Manifest;
import java.util.jar.JarFile;

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
        return DeploymentUtils.isWebArchive(archive);
    }

    public ClassLoader getClassLoader(ClassLoader parent, ReadableArchive archive) {
        WebappClassLoader cloader = new WebappClassLoader(parent);
        try {
            FileDirContext r = new FileDirContext();
            File base = new File(archive.getURI());
            r.setDocBase(base.getAbsolutePath());
            cloader.setResources(r);
            cloader.addRepository("WEB-INF/classes/", new File(base, "WEB-INF/classes/"));
            File libDir = new File(base, "WEB-INF/lib");
            if (libDir.exists()) {
                for (File file : libDir.listFiles(
                        new FileFilter() {
                            public boolean accept(File pathname) {
                                return pathname.getName().endsWith("jar");
                            }
                        }))
                {
                    cloader.addRepository(file.toURL().toString());

                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            cloader.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
        return cloader;
    }
}
