/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.StartupContext;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

/**
 * Tag Main to get the manifest file 
 */
public class Main extends com.sun.enterprise.module.bootstrap.Main {

    final static Logger logger = Logger.getAnonymousLogger();
    private final static String DEFAULT_DOMAINS_DIR_PROPNAME = "AS_DEF_DOMAINS_PATH";
    private final static String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";
    
    
    public static void main(final String args[]) {
        (new Main()).run(args);   
    }

    protected void setParentClassLoader(StartupContext context, ModulesRegistry mr) throws BootException {

        ClassLoader cl = this.getClass().getClassLoader();
        mr.setParentClassLoader(cl);
                
        // first we mask JAXB if necessary.
        // mask the JAXB and JAX-WS API in the bootstrap classloader so that
        // we get to load our copies in the modules

        Module shared = mr.makeModuleFor("org.glassfish.external:glassfish-jaxb", null);

        if (shared!=null) {
            List<URL> urls = new ArrayList<URL>();
            for (URI location : shared.getModuleDefinition().getLocations()) {
                try {
                    urls.add(location.toURL());
                } catch (MalformedURLException e) {
                    throw new BootException("Cannot set up masking class loader", e);
                }
            }

            cl = new MaskingClassLoader(
                cl,
                urls.toArray(new URL[urls.size()]),
                "javax.xml.bind.",
                "javax.xml.ws.",
                "com.sun.xml."
            );
            mr.setParentClassLoader(cl);
        }

        // now install the java-ee APIs. this has to be at a very high level in the hierarchy
        final String javaeeModuleName =
                System.getProperty("javax.javaee.module-name", "org.glassfish:javax.javaee"); // TODO(Sahoo): Remove this
        Module parentModule = mr.makeModuleFor(javaeeModuleName, null);
        if(parentModule!=null) {
            cl = parentModule.getClassLoader();
        }

        parseAsEnv(context.getRootDirectory().getParentFile());
        File domainRoot = getDomainRoot(context);
        verifyDomainRoot(domainRoot);


        // do we have a lib ?
        Repository lib = mr.getRepository("lib");
        if (lib!=null) {
            List<Repository> libs = new ArrayList<Repository>();
            libs.add(lib);

            // do we have a domain lib ?
            File domainlib = new File(domainRoot, "lib");
            if (domainlib.exists()) {
                Repository domainLib = new DirectoryBasedRepository("domnainlib", domainlib);
                try {
                    domainLib.initialize();
                    mr.addRepository(domainLib);
                    libs.add(domainLib);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error while initializing domain lib repository", e);
                }

            }
            cl = setupSharedCL(cl, libs);
        }

        // finally
        mr.setParentClassLoader(cl);
        
    }

    /**
     * Gets the shared repository and add all subdirectories as Repository
     *
     * @param root installation root
     * @param bootstrapJar
     *      The file from which manifest entries are loaded. Used for error reporting
     * @param mf main module manifest
     * @param mr modules registry
     * @throws BootException
     */
    @Override
    protected void createRepository(File root, File bootstrapJar, Manifest mf, ModulesRegistry mr) throws BootException {

        super.createRepository(root, bootstrapJar, mf, mr);
        Repository repo = mr.getRepository("shared");
        File repoLocation = new File(repo.getLocation());
        for (File file : repoLocation.listFiles(
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                }))
        {
            try {
                Repository newRepo = new DirectoryBasedRepository(file.getName(), file);
                newRepo.initialize();
                mr.addRepository(newRepo);
            } catch(FileNotFoundException e) {
                
            } catch(IOException e) {
                logger.log(Level.SEVERE, "Cannot initialize repository at " + file.getAbsolutePath(), e);
            }
        }
    }

    private ClassLoader setupSharedCL(ClassLoader parent, List<Repository> sharedRepos) {


        List<URI> uris = new ArrayList<URI>();
        for (Repository repo : sharedRepos) {
            uris.addAll(repo.getJarLocations());
        }
        URL[] urls = new URL[uris.size()];
        int i=0;
        for (URI uri : uris) {
            try {
                urls[i++] = uri.toURL();
            } catch (MalformedURLException e) {
                logger.warning("Error while adding library to shared classpath " + e.getMessage());
            }
        }

        return new ExtensibleClassLoader(urls, parent, sharedRepos);
    }

    private class ExtensibleClassLoader extends URLClassLoader
        implements RepositoryChangeListener {

        public ExtensibleClassLoader(URL[] urls, ClassLoader parent, List<Repository> repos) {
            super(urls, parent);
            for (Repository repo : repos) {
                repo.addListener(this);
            }
        }

        public void jarAdded(URI uri) {
            try {
                super.addURL(uri.toURL());
                logger.info("Added " + uri + " to shared classpath, no need to restart appserver");
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Cannot add new added library to shared classpath", e);
            }

        }
        public void jarRemoved(URI uri) {
        }

        public void moduleAdded(ModuleDefinition moduleDefinition) {
        }

        public void moduleRemoved(ModuleDefinition moduleDefinition) {
        }
    }

    private void parseAsEnv(File installRoot) {

        Properties asenvProps = new Properties();
        asenvProps.putAll(System.getProperties());
        asenvProps.put("com.sun.aas.installRoot", installRoot.getPath());

        // let's read the asenv.conf
        File configDir = new File(installRoot, "config");
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
            throw new RuntimeException("Error opening asenv.conf : ", ioe);
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
    private File getAsEnvConf(File configDir) {
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
    private File getDomainRoot(StartupContext context)
    {
        // first see if it is specified directly
        Map<String,String> args = context.getArguments();

        String domainDir = getParam(args, "domaindir");

        if(ok(domainDir))
            return new File(domainDir);

        // now see if they specified the domain name -- we will look in the
        // default domains-dir

        File defDomainsRoot = getDefaultDomainsDir();
        String domainName = getParam(args, "domain");

        if(ok(domainName))
            return new File(defDomainsRoot, domainName);

        // OK -- they specified nothing.  Get the one-and-only domain in the
        // domains-dir
        return getDefaultDomain(defDomainsRoot);
    }

    /**
     * Determines the root directory of the domain that we'll start.
     */
    private void verifyDomainRoot(File domainRoot)
    {
        String msg = null;

        if(domainRoot == null)
            msg = "Internal Error: The domain dir is null.";
        else if(!domainRoot.isDirectory())
            msg = "the domain directory is not a directory.";
        else if(!domainRoot.canWrite())
            msg = "the domain directory is not writable.";
        else if(!new File(domainRoot, "config").isDirectory())
            msg = "the domain directory is corrupt - there is no config subdirectory.";

        if(msg != null)
            throw new RuntimeException(msg);

        domainRoot = absolutize(domainRoot);
        System.setProperty(INSTANCE_ROOT_PROP_NAME, domainRoot.getPath() );
    }

    private File getDefaultDomainsDir()
    {
        // note: 99% error detection!

        String dirname = System.getProperty(DEFAULT_DOMAINS_DIR_PROPNAME);

        if(!ok(dirname))
            throw new RuntimeException(DEFAULT_DOMAINS_DIR_PROPNAME + " is not set.");

        File domainsDir = absolutize(new File(dirname));

        if(!domainsDir.isDirectory())
            throw new RuntimeException(DEFAULT_DOMAINS_DIR_PROPNAME +
                    "[" + dirname + "]" +
                    " is specifying a file that is NOT a directory.");

        return domainsDir;
    }


    private File getDefaultDomain(File domainsDir)
    {
        File[] domains = domainsDir.listFiles(new FileFilter()
            {
                public boolean accept(File f) { return f.isDirectory(); }
            });

        // By default we will start an unspecified domain iff it is the only
        // domain in the default domains dir

        if(domains == null || domains.length == 0)
            throw new RuntimeException("no domain directories found under " + domainsDir);

        if(domains.length > 1)
            throw new RuntimeException("More than one domain found under "
                    + domainsDir + " -- you must specify one domain.");

        return domains[0];
    }


    private boolean ok(String s)
    {
        return s != null && s.length() > 0;
    }

    private String getParam(Map<String,String> map, String name)
    {
        // allow both "-" and "--"
        String val = map.get("-" + name);

        if(val == null)
            val = map.get("--" + name);

        return val;
    }

    private File absolutize(File f)
    {
        try
        {
            return f.getCanonicalFile();
        }
        catch(Exception e)
        {
            return f.getAbsoluteFile();
        }
    }    

}
