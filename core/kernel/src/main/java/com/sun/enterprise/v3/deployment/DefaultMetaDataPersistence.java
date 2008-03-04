package com.sun.enterprise.v3.deployment;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import com.sun.enterprise.v3.contract.ApplicationMetaDataPersistence;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.logging.LogDomains;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Default persistence scheme that use the file system
 */
@Service
public class DefaultMetaDataPersistence implements ApplicationMetaDataPersistence {

    @Inject
    V3Environment env;

    public void save(String name, Properties moduleProps) {

        File generatedAppRoot = new File(env.getApplicationStubPath(), name);

        OutputStream os=null;
        File propFile = null;
        try {
            if (!generatedAppRoot.exists()) {
                generatedAppRoot.mkdirs();
            }

            propFile = new File(generatedAppRoot, "glassfish.props");
            os = new BufferedOutputStream(new FileOutputStream(propFile));
            moduleProps.store(os, "GlassFish container properties");
        } catch(IOException ioe) {
            LogDomains.getLogger(LogDomains.DPL_LOGGER).severe("IOException while saving module properties : " + ioe.getMessage());

            try {
                if (os!=null) {
                    os.close();
                }
                if (propFile.exists()) {
                    propFile.delete();
                }
                if (generatedAppRoot.exists()) {
                    generatedAppRoot.delete();
                }
            } catch(IOException e) {
                // ignore
            }
        } finally {
            try {
                if (os!=null) {
                 os.close();
                }
            } catch(IOException ioe) {
               // ignre
            }
        }
    }

    public Properties load(String appName) {

        File appRoot = new File(env.getApplicationStubPath(), appName);
        // do we have our glassfish.props ?
        File propertiesFile = new File(appRoot, "glassfish.props");

        if (propertiesFile.exists()) {
            // bingo
            Properties props = new Properties();
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(propertiesFile));
                props.load(is);
            } catch(IOException ioe) {
                LogDomains.getLogger(LogDomains.DPL_LOGGER).log(Level.SEVERE,
                        "Cannot load appserver meta information about " + appRoot.getName(), ioe);
                return null;
            } finally {
                try {
                    if (is!=null) {
                        is.close();
                    }
                } catch(IOException e) {
                    // ignore
                }
            }
            return props;
        }
        return null;
    }
}
