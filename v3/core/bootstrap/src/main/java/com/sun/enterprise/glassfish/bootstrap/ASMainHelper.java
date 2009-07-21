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
import com.sun.enterprise.module.common_impl.ModuleId;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

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

        if (!asenv.exists()) {
            Logger.getAnonymousLogger().fine(asenv.getAbsolutePath() + " not found, ignoring");
            return;
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
     * Determines the root directory of the domain that we'll start and
     * sets the system property called {@link #INSTANCE_ROOT_PROP_NAME}.
     */
    /*package*/ void verifyAndSetDomainRoot(File domainRoot)
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
            throw new RuntimeException("Multiple domains[" + domains.length + "] found under "
                    + domainsDir + " -- you must specify a domain name as -domain <name>");

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

    public static boolean deleteRecursive(File dir) {
        for (File f : dir.listFiles()) {
            if(f.isFile()) {
                f.delete();
            } else {
                deleteRecursive(f);
            }
        }
        return dir.delete();
    }

    static long getLastModified(File directory, long current) {

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

    /**
     * This method is used to copy a given file to another file
     * using the buffer sixe specified
     *
     * @param fin  the source file
     * @param fout the destination file
     */
    static void copyFile(File fin, File fout) throws IOException {

        InputStream inStream = new BufferedInputStream(new FileInputStream(fin));
        FileOutputStream fos = new FileOutputStream(fout);
        copy(inStream, fos, fin.length());
    }

    static void copyWithoutClose(InputStream in, FileOutputStream out, long size) throws IOException {

        ReadableByteChannel inChannel = Channels.newChannel(in);
        FileChannel outChannel = out.getChannel();
        outChannel.transferFrom(inChannel, 0, size);

    }

    static void copy(InputStream in, FileOutputStream out, long size) throws IOException {

        try {
            copyWithoutClose(in, out, size);
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }


    private class PlainJarRepository extends AbstractRepositoryImpl {
        File aFile = null;

        public PlainJarRepository(String name, File aFile) {
            super(name, aFile.toURI());
            this.aFile = aFile;
        }

        protected void loadModuleDefs(
                Map<ModuleId, ModuleDefinition> moduleDefs,
                List<URI> libraries) throws IOException {
            if (aFile.exists()) {
                libraries.add(aFile.toURI());
            }
        }
    }
}
