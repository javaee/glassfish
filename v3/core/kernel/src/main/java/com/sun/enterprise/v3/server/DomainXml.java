package com.sun.enterprise.v3.server;

import com.sun.enterprise.config.serverbeans.GlassFishDocument;
import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identify where our configuration lives.
 *
 * @author Jerome Dochez
 */
@Service
public class DomainXml implements Populator {

    @Inject
    StartupContext context;

    @Inject
    Logger logger;

    @Inject
    Habitat habitat;


    public void run(ConfigParser parser) {

        if (context == null) {
            System.err.println("Startup context not provided, cannot continue");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Startup class : " + this.getClass().getName());
        }

        // our bootstrap directory is <appserver>/lib, need to calculate
        // our real root installation from there.
        File bootstrapDirectory = context.getRootDirectory();
        String root = bootstrapDirectory.getParentFile().getAbsolutePath();

        Properties asenvProps = new Properties();
        asenvProps.putAll(System.getProperties());
        asenvProps.put("com.sun.aas.installRoot", root);


        File installRootFile = new File(root);
        if (!installRootFile.exists()) {
            logger.severe("Invalid root installation " + installRootFile.getAbsolutePath());
            return;
        }

        // let's read the asenv.conf
        File configDir = new File(installRootFile, "config");
        File asenv;
        String osName = System.getProperty("os.name");
        if (osName.indexOf("Windows") == -1) {
            asenv = new File(configDir, "asenv.conf");
        } else {
            asenv = new File(configDir, "asenv.bat");
        }

        LineNumberReader lnReader = null;
        try {
            lnReader = new LineNumberReader(new FileReader(asenv));
            String line = lnReader.readLine();
            // most of the asenv.conf values have surrounding "", remove them
            // and on Windows, they start with SET XXX=YYY
            Pattern p = Pattern.compile("[Ss]?[Ee]?[Tt]? *([^=]*)=\"?([^\"]*)\"?");
            while (line != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    File f = new File(m.group(2));
                    if (!f.isAbsolute()) {
                        f = new File(configDir, m.group(2));
                        if (f.exists()) {
                            asenvProps.put(m.group(1), f.getAbsolutePath());
                        } else {
                            asenvProps.put(m.group(1), m.group(2));
                        }
                    } else {
                        asenvProps.put(m.group(1), m.group(2));
                    }
                }
                line = lnReader.readLine();
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error opening asenv.conf : ", ioe);
            return;
        } finally {
            try {
                if (lnReader != null)
                    lnReader.close();
            } catch (IOException ioe) {
                // ignore
            }
        }

        // install the new system properties
        System.setProperties(asenvProps);

        // which domain are we starting ?
        File domainRoot = new File(System.getProperty("AS_DEF_DOMAINS_PATH"));
        String domainName = context.getArguments().get("-domain");
        if (domainName == null) {
            File[] domainFiles = domainRoot.listFiles();
            if (domainFiles.length == 0) {
                logger.severe("No domain found at " + domainRoot.getAbsolutePath());
                return;
            }
            int i = 0;
            while (domainName == null) {
                if (domainFiles[i].isDirectory()) {
                    domainName = domainFiles[i].getName();
                }
                i++;
                if (i > domainFiles.length) {
                    logger.severe("No domain found at " + domainRoot.getAbsolutePath());
                    return;
                }
            }
        }

        domainRoot = new File(domainRoot, domainName);
        if (!domainRoot.exists()) {
            logger.severe("Domain " + domainName + " does not exist at " + domainRoot);
            return;
        }
        V3Environment env = new V3Environment(domainRoot.getAbsolutePath(), context);
        habitat.add(new ExistingSingletonInhabitant(V3Environment.class, env));
        File domainXml = new File(env.getConfigDirPath(), V3Environment.kConfigXMLFileName);
        System.setProperty("com.sun.aas.instanceRoot", domainRoot.getAbsoluteFile().getAbsolutePath() );        

        try {
            parser.parse(domainXml.toURL(), new GlassFishDocument(habitat));
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }


    }

}
