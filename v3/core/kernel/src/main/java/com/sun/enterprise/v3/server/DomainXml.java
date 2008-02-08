package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.glassfish.api.Absolutized;
import org.glassfish.config.support.GlassFishDocument;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locates and parses the portion of <tt>domain.xml</tt> that we care.
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 */
@Service
public class DomainXml implements Populator {

    @Inject
    StartupContext context;

    @Inject
    Logger logger;

    @Inject
    Habitat habitat;

    @Inject
    XMLInputFactory xif;

    /**
     * Root of GlassFish installation.
     */
    @Absolutized
    File glassFishRoot;

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
        glassFishRoot = bootstrapDirectory.getParentFile();
        if (!glassFishRoot.exists()) {
            throw new BootError("No such path exists " + glassFishRoot);
        }

        parseAsEnv(glassFishRoot);

        // which domain are we starting ?
        File domainRoot = getDomainRoot();

        V3Environment env = new V3Environment(domainRoot.getPath(), context);
        habitat.add(new ExistingSingletonInhabitant(V3Environment.class, env));
        File domainXml = new File(env.getConfigDirPath(), V3Environment.kConfigXMLFileName);

        parseDomainXml(parser, domainXml);
    }

    /**
     * Parses <tt>domain.xml</tt>
     */
    protected void parseDomainXml(ConfigParser parser, final File domainXml) {
        try {
            // TODO: in reality we need to get the server name from somewhere
            final String serverName = "server";

            DomainXmlReader xsr = new DomainXmlReader(domainXml, serverName);
            parser.parse(xsr, new GlassFishDocument(habitat));
            xsr.close();
            if(!xsr.foundConfig)
                throw new BootError("No <config> seen for name="+xsr.configName);
        } catch (XMLStreamException e) {
            throw new BootError("Failed to parse "+domainXml,e);
        }
    }

    protected void parseAsEnv(File installRootFile) {
        Properties asenvProps = new Properties();
        asenvProps.putAll(System.getProperties());
        asenvProps.put("com.sun.aas.installRoot", glassFishRoot.getPath());

        // let's read the asenv.conf
        File configDir = new File(installRootFile, "config");
        File asenv = getAsEnvConf(configDir);

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
            throw new BootError("Error opening asenv.conf : ", ioe);
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
    }

    /**
     * Figures out the asenv.conf file to load.
     */
    protected File getAsEnvConf(File configDir) {
        String osName = System.getProperty("os.name");
        if (osName.indexOf("Windows") == -1) {
            return new File(configDir, "asenv.conf");
        } else {
            return new File(configDir, "asenv.bat");
        }
    }

    /**
     * Determines the root directory of the domain that we'll start.
     */
    @Absolutized
    protected File getDomainRoot() {
        File domainRoot = new File(System.getProperty("AS_DEF_DOMAINS_PATH"));
        String domainName = context.getArguments().get("-domain");
        if (domainName == null) {
            File[] domainFiles = domainRoot.listFiles();
            if (domainFiles==null) {
                throw new BootError("No such directory exists: "+domainName);
            }
            if (domainFiles.length == 0) {
                throw new BootError("No domain found at " + domainRoot);
            }
            int i = 0;
            while (domainName == null) {
                if (domainFiles[i].isDirectory()) {
                    domainName = domainFiles[i].getName();
                }
                i++;
                if (i > domainFiles.length) {
                    throw new BootError("No domain found at " + domainRoot);
                }
            }
        }

        domainRoot = new File(domainRoot, domainName);
        if (!domainRoot.exists()) {
            throw new BootError("Domain " + domainName + " does not exist at " + domainRoot);
        }

        System.setProperty("com.sun.aas.instanceRoot", domainRoot.getPath() );
        return domainRoot;
    }


    /**
     * {@link XMLStreamReader} that skips irrelvant &lt;config> elements that we shouldn't see.
     */
    private class DomainXmlReader extends XMLStreamReaderFilter {
        /**
         * We need to figure out the configuration name from the server name.
         * Once we find that out, it'll be set here.
         */
        private String configName;
        private final File domainXml;
        private final String serverName;

        /**
         * If we find a matching config, set to true. Used for error detection in case
         * we don't see any config for us.
         */
        private boolean foundConfig;

        /**
         * Because {@link XMLStreamReader} doesn't close the underlying stream,
         * we need to do it by ourselves. So much for the "easy to use" API.
         */
        private FileInputStream stream;

        public DomainXmlReader(File domainXml, String serverName) throws XMLStreamException {
            try {
                stream = new FileInputStream(domainXml);
            } catch (FileNotFoundException e) {
                throw new XMLStreamException(e);
            }
            setParent(xif.createXMLStreamReader(domainXml.toURI().toString(), stream));
            this.domainXml = domainXml;
            this.serverName = serverName;
        }

        public void close() throws XMLStreamException {
            super.close();
            try {
                stream.close();
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }

        boolean filterOut() throws XMLStreamException {
            checkConfigRef(getParent());

            if(getLocalName().equals("config")) {
                if(configName==null) {
                    // we've hit <config> element before we've seen <server>,
                    // so we still don't know which config element to look for.
                    // For us to make this work, we need to parse the file twice
                    parse2ndTime();
                    assert configName!=null;
                }

                // if <config name="..."> didn't match what we are looking for, filter it out
                if(configName.equals(getAttributeValue(null, "name"))) {
                    foundConfig = true;
                    return false;
                }
                return true;
            }

            // we'll read everything else
            return false;
        }

        private void parse2ndTime() throws XMLStreamException {
            logger.info("Forced to parse "+ domainXml +" twice because we didn't see <server> before <config>");
            try {
                InputStream stream = new FileInputStream(domainXml);
                XMLStreamReader xsr = xif.createXMLStreamReader(domainXml.toURI().toString(),stream);
                while(configName==null) {
                    switch(xsr.next()) {
                    case START_ELEMENT:
                        checkConfigRef(xsr);
                        break;
                    case END_DOCUMENT:
                        break;
                    }
                }
                xsr.close();
                stream.close();
                if(configName==null)
                    throw new BootError(domainXml +" contains no <server> element that matches "+ serverName);
            } catch (IOException e) {
                throw new XMLStreamException("Failed to parse "+domainXml,e);
            }
        }

        private void checkConfigRef(XMLStreamReader xsr) {
            String ln = xsr.getLocalName();

            if(configName==null && ln.equals("server")) {
                // is this our <server> element?
                if(serverName.equals(xsr.getAttributeValue(null, "name"))) {
                    configName = xsr.getAttributeValue(null,"config-ref");
                    if(configName==null)
                        throw new BootError("<server> element is missing @config-ref at "+formatLocation(xsr));
                }
            }
        }

        /**
         * Convenience method to return a human-readable location of the parser.
         */
        private String formatLocation(XMLStreamReader xsr) {
            return "line "+xsr.getLocation().getLineNumber()+" at "+xsr.getLocation().getSystemId();
        }
    }
}
