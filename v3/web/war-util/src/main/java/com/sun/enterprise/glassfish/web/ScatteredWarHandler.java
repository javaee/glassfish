package com.sun.enterprise.glassfish.web;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.embedded.ScatteredArchive;
import org.glassfish.web.loader.WebappClassLoader;
import org.apache.naming.resources.FileDirContext;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

/**
 * @author Jerome Dochez
 */
@Service
public class ScatteredWarHandler  extends WarHandler implements ArchiveHandler {

    private static final Logger logger = LogDomains.getLogger(ScatteredWarHandler.class, LogDomains.DPL_LOGGER);
    
    @Override
    public String getArchiveType() {
        return "scattered-archive";
    }

    @Override
    public boolean handles(ReadableArchive archive) {
        return (archive instanceof ScatteredArchive &&
                ((ScatteredArchive) archive).type()==ScatteredArchive.Builder.type.war);
    }

    @Override
    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        ScatteredArchive archive = (ScatteredArchive) context.getSource();
        WebappClassLoader cloader = new WebappClassLoader(parent);
        try {
            FileDirContext r = new FileDirContext();
            File base = archive.getResourcesDir();
            r.setDocBase(base.getAbsolutePath());
            File sunWeb = archive.getFile("WEB-INF/sun-web.xml");
            SunWebXmlParser sunWebXmlParser = null;
            if (sunWeb!=null && sunWeb.exists()) {
                sunWebXmlParser = new SunWebXmlParser(sunWeb.getParentFile().getParent());
            }

            cloader.setResources(r);
            for (URL url : archive.getClassPath()) {
                cloader.addRepository(url.toExternalForm());
            }
            if (context.getScratchDir("jsp") != null) {
                cloader.setWorkDir(context.getScratchDir("jsp"));
            }

            if (sunWebXmlParser!=null) {
                configureLoaderAttributes(cloader, sunWebXmlParser, base);
                configureLoaderProperties(cloader, sunWebXmlParser, base);
            }
        } catch(XMLStreamException xse) {
            logger.log(Level.SEVERE, xse.getMessage());
            logger.log(Level.FINE, xse.getMessage(), xse);
        } catch(FileNotFoundException fnfe) {
            logger.log(Level.SEVERE, fnfe.getMessage());
            logger.log(Level.FINE, fnfe.getMessage(), fnfe);
        }
        try {
            cloader.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cloader;
    }
}
