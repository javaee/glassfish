/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.common_impl.AbstractRepositoryImpl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used by {@link ASMainOSGi}
 * Most of the code is moved from {@link ASMain} to this class to keep ASMain
 * as small as possible.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ASMainHelper {

    private Logger logger;
    private final static String DEFAULT_DOMAINS_DIR_PROPNAME = "AS_DEF_DOMAINS_PATH";
    private final static String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";


    public ASMainHelper(Logger logger) {
        this.logger = logger;
    }

    ClassLoader setupSharedCL(ClassLoader parent, List<URL> classpath,  List<Repository> sharedRepos) {
        List<URI> uris = new ArrayList<URI>();
        for (Repository repo : sharedRepos) {
            uris.addAll(repo.getJarLocations());
        }
        URL[] urls = new URL[uris.size() + classpath.size()];
        int i=0;
        for (URL url : classpath) {
            urls[i++] = url;
        }
        for (URI uri : uris) {
            try {
                urls[i++] = uri.toURL();
            } catch (MalformedURLException e) {
                logger.warning("Error while adding library to shared classpath " + e.getMessage());
            }
        }

        return new ExtensibleClassLoader(urls, parent, sharedRepos);
    }

    /*protected*/ void parseAsEnv(File installRoot) {

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

    void addPaths(File dir, String[] jarPrefixes, List<URL> urls) throws MalformedURLException {
        File[] jars = dir.listFiles();
        if(jars!=null) {
            for( File f : jars) {
                for (String prefix : jarPrefixes) {
                    String name = f.getName();
                    if(name.startsWith(prefix) && name.endsWith(".jar"))
                        urls.add(f.toURI().toURL());
                }
            }
        }
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
    /*package*/ File getDomainRoot(StartupContext context)
    {
        // first see if it is specified directly
        Properties args = context.getArguments();

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
    /*package*/ void verifyDomainRoot(File domainRoot)
    {
        String msg = null;

        if(domainRoot == null)
            msg = "Internal Error: The domain dir is null.";
        else if (!domainRoot.exists())
            msg = "the domain directory does not exist";
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

    private String getParam(Properties map, String name)
    {
        // allow both "-" and "--"
        String val = map.getProperty("-" + name);

        if(val == null)
            val = map.getProperty("--" + name);

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

    /**
     * @return A pseudo repository that contains only jdk/lib/tools.jar, null
     *         if such a jar does not exist.
     */
    Repository getJDKToolsRepo() {
        File jdktools = getJDKToolsJar();
        if (jdktools != null && jdktools.exists()) {
            Repository jdkToolsRepo =
                    new PlainJarRepository("jdktools", jdktools);
            try {
                jdkToolsRepo.initialize();
            } catch(IOException e) {
                logger.log(Level.SEVERE, "Error while initializing jdk tools.jar", e);
            }
            return jdkToolsRepo;
        }
        return null;
    }

    File getJDKToolsJar() {
        File javaHome = new File(System.getProperty("java.home"));
        File jdktools = null;
        if (javaHome.getParent() != null) {
            jdktools = new File(javaHome.getParent(),
                    "lib" + File.separator + "tools.jar");
        }
        return jdktools;
    }

    private class PlainJarRepository extends AbstractRepositoryImpl {
        File aFile = null;

        public PlainJarRepository(String name, File aFile) {
            super(name, aFile.toURI());
            this.aFile = aFile;
        }

        protected void loadModuleDefs(
                Map<String, ModuleDefinition> moduleDefs,
                List<URI> libraries) throws IOException {
            if (aFile.exists()) {
                libraries.add(aFile.toURI());
            }
        }
    }

    void setUpOSGiCache(File glassfishDir, File fwDir, File cacheDir)  {

        // let's find our more recent entry, we'll need it anyway
        long lastModified = getLastModified(new File(glassfishDir, "modules"), 0);
        long settingsLastModified = getLastModified( new File(fwDir, "conf"), 0);

        if (settingsLastModified>lastModified) {
            lastModified = settingsLastModified;
        }

        long recordedLastModified = 0;
        Properties persistedInfo = new Properties();
        File lastModifiedFile = new File(cacheDir.getParentFile(), cacheDir.getName()+".lastmodified");
        if (lastModifiedFile.exists()) {

            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(lastModifiedFile));
                persistedInfo.load(is);
                try {
                    recordedLastModified = Long.parseLong(persistedInfo.getProperty("LastModified"));
                    // check that we have not moved our domain's directory, felix is sensitive to absolute path
                    String location = persistedInfo.getProperty("Location");
                    if (!cacheDir.toURI().toURL().toString().equals(location)) {
                        recordedLastModified=0;
                    }
                } catch (NumberFormatException e) {
                    recordedLastModified = 0;
                }
            } catch(IOException e) {
                logger.info("Cannot read recorded lastModified, felix cache will be flushed");
            } finally {
                if (is!=null) {
                    try {
                        is.close();
                    } catch(IOException e){};
                }
            }
        }


        // if the recordedLastModified is different than our most recent entry,
        // we flush the felix cache, otherwise we reuse it.
        if (recordedLastModified!=lastModified) {
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                // remove this old cache so felix creates a new one.
                logger.info("Removing Felix cache profile dir " + cacheDir+ " left from a previous run");
                boolean deleted = deleteRecursive(cacheDir);
                if (!deleted) {
                    logger.warning("Not able to delete " + cacheDir);
                }
            }
            cacheDir.mkdirs();

            // now record our new LastModified
            ObjectOutputStream os = null;
            try {
                lastModifiedFile.delete();
                if (!lastModifiedFile.createNewFile()) {
                    logger.warning("Cannot create new lastModified file");
                    return;
                }
                os = new ObjectOutputStream(new FileOutputStream(lastModifiedFile));
                persistedInfo.clear();
                persistedInfo.put("LastModified", (new Long(lastModified).toString()));
                persistedInfo.put("Location", cacheDir.toURI().toURL().toString());
                persistedInfo.store(os, null);

            } catch(IOException e) {
                logger.info("Cannot create record of lastModified file");
            } finally {
                if (os!=null) {
                    try {
                        os.close();
                    } catch(IOException e) {}
                }
            }
        }
    }
    

    long getLastModified(File directory, long current) {

        for (File file : directory.listFiles()) {
            long lastModified;
            if (file.isDirectory()) {
                lastModified = getLastModified(file, current);
            } else {
                lastModified = file.lastModified();
            }
            if (lastModified>current) {
                current=lastModified;
            }
        }
        return current;
    }

    boolean deleteRecursive(File dir) {
        for (File f : dir.listFiles()) {
            if(f.isFile()) {
                f.delete();
            } else {
                deleteRecursive(f);
            }
        }
        return dir.delete();
    }    
}
