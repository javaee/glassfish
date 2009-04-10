/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */
package org.glassfish.embed;

import  com.sun.enterprise.util.ObjectAnalyzer;
import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.*;
import java.net.MalformedURLException;
import static org.glassfish.embed.util.ServerConstants.*;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.net.URL;
import org.glassfish.embed.util.EmbeddedUtils;

/**
 * A class that is responsible for encapsulating all information having to do with
 * the external filesystem.
 * <ul>
 * <li>It maintains references to directories that GlassFish requires.
 * <li>It sets up special files that GlassFish needs.
 * <li>It is smart about initialization and state.  For instance, if you call a
 * getter before initialization -- an {@link EmbeddedException} will be thrown.
 *
 * @author Byron Nevins
 */

public final class EmbeddedFileSystem {

    /**
     *
     */
    EmbeddedFileSystem() {
    }

    // ****************************************************
    // *************    public setters
    // ****************************************************


    /**
     * Set the root directory of the Embedded GlassFish file system.
     * The directory specified must exist.
     *
     * @param f install root directory
     * @throws EmbeddedException
     */
    public void setInstallRoot(File f) throws EmbeddedException {
        mustNotBeInitialized("setInstallRoot");
        installRoot = SmartFile.sanitize(f);

        if (!EmbeddedUtils.mkdirsIfNotExist(f)) {
            throw new EmbeddedException("bad_install_root", f);
        }
    }

    /**
     * Set the directory for the server instance
     * The directory specified must exist.
     *
     * @param f instance root directory
     * @throws EmbeddedException
     */
    public void setInstanceRoot(File f) throws EmbeddedException {
        mustNotBeInitialized("setInstanceRoot");
        instanceRoot = SmartFile.sanitize(f);

        if (!EmbeddedUtils.mkdirsIfNotExist(f)) {
            throw new EmbeddedException("bad_instance_root", f);
        }
    }

    /**
     * Use the specified file as the target domain.xml.
     * The target domain.xml is the output of the in-memory domain.xml
     *
     * @param f file that the server writes out to as domain.xml
     * @throws EmbeddedException
     */
    public void setDomainXmlTarget(File f) throws EmbeddedException {
        mustNotBeInitialized("setDomainXmlTarget");
        domainXmlTarget = SmartFile.sanitize(f);
    }

    /**
     * Use the specified URL as the source domain.xml.
     * The source domain.xml is the input to the in-memory domain.xml
     *
     * @param url URL that is read in by the server as domain.xml
     * @throws EmbeddedException
     */
    public void setDomainXmlSource(URL url) throws EmbeddedException {
        mustNotBeInitialized("setDomainXmlSource(URL)");
        domainXmlSource = url;
    }

    /**
     * Use the specified file as the source domain.xml.
     * The source domain.xml is the input to the in-memory domain.xml.
     *
     * @param f file that is read in by the server as domain.xml
     * @throws EmbeddedException
     */
    public void setDomainXmlSource(File f) throws EmbeddedException {
        mustNotBeInitialized("setDomainXmlSource(File)");
        setDomainXmlTarget(f);

        try {
            domainXmlSource = f.toURI().toURL();
        } catch (Exception e) {
            throw new EmbeddedException("bad_file", f, e);
        }
    }
    /**
     * Set the docroot directory of the Embedded GlassFish file system.
     * The default is instance-dir/docroot
     *
     * The directory must exist or it must be possible to create it.
     *
     * @param docRoot  the desired docroot directory
     * @throws EmbeddedException
     */
    public void setDocRootDir(File docRoot) throws EmbeddedException {
        mustNotBeInitialized("setDocRootDir");
        docRootDir = SmartFile.sanitize(docRoot);

        if (!EmbeddedUtils.mkdirsIfNotExist(docRootDir)) {
            throw new EmbeddedException("bad_docroot", docRootDir);
        }
    }

    /**
     * Specifies whether to delete the Embedded file system after stopping Embedded
     * GlassFish process.  If set to <code>true</code> the installation directory
     * and all directories and files under it will be deleted upon exit.
     * If set to <code>false</code>, the files are not deleted.
     *
     * The default depends on whether the installation or instance directory is set:
     *
     * If the installation or instance directory is set, the default is false.
     * If the installation or instance direcotry is not set, the default is true.
     *
     * @param b true - deletes the install root upon exit
     *          false - keeps the install root upon exit
     * @throws EmbeddedException
     */
    public void setAutoDelete(boolean b) throws EmbeddedException {
        mustNotBeInitialized("setAutoDelete");
        autoDelete = b;
        autoDeleteWasExplicitlySet = true;
    }

    /**
     * Specify the file to be used for logging
     *
     * @param f file for logging
     */
    public void setLogFile(File f) {
        // f can be null -- it is pointless but OK
        logFile = f;
    }
    
    // ****************************************************
    // *************    public getters
    // ****************************************************

    /**
     * Returns the root directory of the Embedded GlassFish file system.
     *
     * @return install root directory
     * @throws EmbeddedException if instance has not been initialized
     */
    public File getInstallRoot() throws EmbeddedException {
        mustBeInitialized("getInstallRoot");
        return EmbeddedUtils.cloneAndVerifyFile(installRoot);
    }

    /**
     * Returns the root directory of the server instance
     *
     * @return instance root directory
     * @throws EmbeddedException if instance has not been initialized
     */
    public File getInstanceRoot() throws EmbeddedException {
        mustBeInitialized("getInstanceRoot");
        return EmbeddedUtils.cloneAndVerifyFile(instanceRoot);
    }

    /**
     * Returns the file that the server writes out to as domain.xml
     *
     * @return target domain.xml file
     * @throws EmbeddedException if instance has not been initialized
     */
    public File getTargetDomainXml() throws EmbeddedException {
        mustBeInitialized("getTargetDomainXml");
        return domainXmlTarget;
    }

    /**
     * Returns the URL that the server reads in as domain.xml
     *
     * @return source domain.xml URL
     * @throws EmbeddedException if instance has not been initialized
     */
    public URL getSourceDomainXml() throws EmbeddedException {
        mustBeInitialized("getSourceDomainXml");
        return domainXmlSource;
    }

    /**
     * Returns the log file
     *
     * @return logfile
     * @throws EmbeddedException if instance has not been initialized
     */
    public File getLogFile() throws EmbeddedException {
        mustBeInitialized("getLogFile");
        return EmbeddedUtils.cloneFile(logFile);
    }

    /**
     * Returns the applications directory of the Embedded GlassFish file system
     *
     * @return applications directory
     * @throws EmbeddedException if instance has not been initialized
     */
    public File getApplicationsDir() throws EmbeddedException {
        return EmbeddedUtils.cloneAndVerifyFile(appsDir);
    }

    /**
     * Returns the docroot directory of the Embedded GlassFish file system
     *
     * @return docroot directory
     * @throws EmbeddedException if instance has not been initialized
     */
    public File getDocRootDir() throws EmbeddedException {
        mustBeInitialized("getDocRootDir");
        return EmbeddedUtils.cloneAndVerifyFile(docRootDir);
    }

    /**
     * Sets the default web.xml to url.  The default url is located inside the
     * Embedded GlassFish jars.
     * @param url
     */
    public void setDefaultWebXml(URL url) {
        this.defaultWebXml = url;
    }

    /**
     * The default web.xml is inside the Embedded GlassFish jar.
     * @return the default web.xml url
     */
    public URL getDefaultWebXml() {
        return defaultWebXml;
    }

    /*
     * Return a String representation.
     */
    
    @Override
    public String toString() {
        return ObjectAnalyzer.toString(this);
    }

    // ****************************************************
    // *************    package private. Think long and hard before making public!
    // ****************************************************

    /**
     *
     * @return fake modules directory.  This is for the benefit of StartupContext
     * which insists that the install root should be the PARENT of some other directory.
     * This is some other directory.
     * @throws EmbeddedException
     */

    File getModulesDirectory() throws EmbeddedException {
        mustBeInitialized("getModulesDirectory");
        return modulesDir;
    }

    void cleanup() throws EmbeddedException {
        mustBeInitialized("cleanup");
        if (shouldCleanup()) {
            // note that Logger will not work now because the JVM has shut it down
            System.out.println("Cleaning up files");
            FileUtils.whack(installRoot);

            if (!instanceRoot.equals(installRoot)) {
                FileUtils.whack(instanceRoot);
            }
        }
    }

    /* do NOT make this public!
     * if user set their own stuff - just validate.  If not then setup defaults
     * calling this method shuts the window on ALL setters so we don't have to
     * worry about getting into a weird inconsistent state later.
     */
    void initialize() throws EmbeddedException {
        initializeDirectories();
        initializeDomainXml(); // very complicated!!
        setSystemProps();
        initializeLogFile();
        initialized = true;
    }

    /**
     * If we are not using an external user-provided domain.xml then we will
     * need to add virtual srvers and listeners.  This method will reveal where the
     * domain.xml has come from.
     * @return true when the source configuration file is not the domain.xml bundled
     * inside the Embedded GlassFish jar file.
     */
    boolean isUserDomainXml() {
        return domainXmlSource != DEFAULT_DOMAIN_XML_URL;

    }


    // ****************************************************
    // *************    private
    // ****************************************************

    private void initializeDirectories() throws EmbeddedException {
        initializeInstanceAndInstallDirs();
        // bnevins note: 
        //it is too early to call the getters for installRoot and instanceRoot - so
        // we access the variables themselves.
        appsDir         = initializeDirectory(instanceRoot, APPLICATIONS_DIR_NAME, "Applications");
        generatedDir    = initializeDirectory(instanceRoot, GENERATED_DIR_NAME, "Generated");
        modulesDir      = initializeDirectory(installRoot, MODULES_DIR_NAME, "Modules");

        if(docRootDir == null)
            docRootDir =  initializeDirectory(instanceRoot, DOCROOT_DIR_NAME, "Docroot");
    }

    /**
     * The idea here is that the Url ALWAYS points at the source of data.
     * the File always points t where on disk the data will be written to (and
     * maybe is the source of data)
     * @throws EmbeddedException
     */
    private void initializeDomainXml() throws EmbeddedException {
        initializeTargetDomainXml();
        initializeSourceDomainXml();
    }


    private synchronized void initializeInstanceAndInstallDirs() throws EmbeddedException {
        // booleans for readability...
        boolean hasInstance = instanceRoot != null;
        boolean hasInstall = installRoot != null;

        /*
         * both are set to something, just return
         * note that if they are set then they are already GUARANTEED to be kosher.
         */
        if(hasInstance && hasInstall) {
            return;
        }

        /*
         * both are null -- use all defaults
         */
        if(!hasInstance && !hasInstall) {
            setInstallRoot(createDefaultInstallDir());
            setInstanceRoot(new File(installRoot, DEFAULT_PATH_TO_INSTANCE));
            
            if(!autoDeleteWasExplicitlySet)
                autoDelete = true;
        }

        /*
         * instance but no install.
         */
        else if(hasInstance && !hasInstall) {
            setInstallRoot(createDefaultInstallDir());
        }

        /*
         * install but no instance.
         */
        else { // if(hasInstall && !hasInstance)
            setInstanceRoot(new File(installRoot, DEFAULT_PATH_TO_INSTANCE));
        }
    }
    
    private File createDefaultInstallDir() throws EmbeddedException {
        // note that we still hold a mutex here!
        long num = System.currentTimeMillis();
        File f = null;
        int maxAttempts = 20; // prevents an infinite loop.

        do {
            String middle = Long.toString(num);
            middle = middle.substring(middle.length() - 4); // last 4 digits
            String filename = DEFAULT_INSTALL_DIR_PREFIX + middle + DEFAULT_INSTALL_DIR_SUFFIX;
            f = new File(filename);
            num++;
        } while(!f.mkdir() && --maxAttempts > 0);    // it MUST not exist and it must have been created OK

        if(maxAttempts <= 0)
            throw new EmbeddedException("cant_create_dir", "install", f);

        return f;
    }

    private void initializeLogFile() throws EmbeddedException {

        // The user may have already specified a logfile.
        // if so - do a sanity check and return it.

        if(logFile != null) {
            return;
        }

        File logDir = new File(instanceRoot, LOG_FILE_DIR);

        if (!EmbeddedUtils.mkdirsIfNotExist(logDir)) {
            throw new EmbeddedException("cant_make_log_dir", logDir);
        }

        logFile = new File(logDir, LOG_FILE);
    }

    private void initializeTargetDomainXml() throws EmbeddedException {
        if (domainXmlTarget == null) {
            domainXmlTarget = new File(instanceRoot, DEFAULT_PATH_TO_DOMAIN_XML);
        } else {
            // they specified it -- insist that it exist and be non-empty
            if (!ok(domainXmlTarget)) {
                throw new EmbeddedException("EFS_bad_domain_xml_file", domainXmlTarget);
            }
        }

        File parent = new File(domainXmlTarget, "..");
        parent = SmartFile.sanitize(parent); // get rid of the trailing ".."

        if (!EmbeddedUtils.mkdirsIfNotExist(parent)) {
            throw new EmbeddedException("cant_create_parent_dir_domain_xml", parent);
        }
    }

    private void initializeSourceDomainXml() throws EmbeddedException {
        File dx = SmartFile.sanitize(new File(instanceRoot, DEFAULT_PATH_TO_DOMAIN_XML));

        // case 1: they specified domain.xml somewhere, anywhere.
        if (domainXmlSource != null) {
            // they specified it -- insist that it be bonafide.
            if (!ok(domainXmlSource)) {
                throw new EmbeddedException("EFS_bad_domain_xml_file", domainXmlTarget);
            }
        }

        // case 2: There is a domain.xml in the correct place under the instance root
        else if (dx.isFile()) {
            try {
                domainXmlSource = dx.toURI().toURL();
                return;
            } catch (MalformedURLException ex) {
                throw new EmbeddedException(ex);
            }
        }

        // case 3: We use the domain.xml inside this jar
        else {
            domainXmlSource = DEFAULT_DOMAIN_XML_URL; // use the hard-wired file.
        }
    }
 
    private File initializeDirectory(File parent, String filename, String messageName) throws EmbeddedException {
        File dir = new File(parent, filename);

        if (!EmbeddedUtils.mkdirsIfNotExist(dir)) {
            throw new EmbeddedException("cant_create_dir", messageName, dir);
        }
        return dir;
    }

    private void setSystemProps() {
        System.setProperty(INSTANCE_ROOT_PROPERTY, instanceRoot.getPath());
        System.setProperty(INSTALL_ROOT_PROPERTY, installRoot.getPath());

        System.setProperty(INSTANCE_ROOT_URI_PROPERTY, instanceRoot.toURI().toString());
        System.setProperty(INSTALL_ROOT_URI_PROPERTY, installRoot.toURI().toString());

        // Surprisingly this is the most reliable way to get parent with JDK!
        File domainsDir = SmartFile.sanitize(new File(instanceRoot, ".."));

        System.setProperty(DOMAINS_ROOT_PROPERTY, domainsDir.getPath());
    }

    private boolean shouldCleanup() {
        // don't EVER delete if the flag is false!!!
        return autoDelete;
    }

    private void mustBeInitialized(String name) throws EmbeddedException {
        if (!initialized) {
            throw new EmbeddedException("must_be_initialized", name);
        }
    }

    private void mustNotBeInitialized(String name) throws EmbeddedException {
        if (initialized) {
            throw new EmbeddedException("must_not_be_initialized", name);
        }
    }

    private boolean ok(File f) {
        return f.length() > 0L;
    }

    private boolean ok(URL url) {
        // TODO -- what else besides not null?
        return url != null;
    }
    private File                installRoot;
    private File                instanceRoot;
    private File                domainXmlTarget;
    private File                appsDir;
    private File                docRootDir;
    private File                generatedDir;
    private File                modulesDir;
    private File                logFile;
    private URL                 domainXmlSource;
    private URL                 defaultWebXml;
    private boolean             initialized         = false;

    // bnevins -
    // autoDelete is false by default.  If at init time it is discovered that the
    // user set no dirs -- we automatically set this flag to true.
    // If the user explicitly sets it to true -- then the dirs are deleted no matter
    // if they are user-supplied or not.
    // I.e. the user's files are safe if he specifies directory|ies AND he does not
    // set autoDelete to true.

    private boolean             autoDelete          = false;

    // if the user explicitly sets it to true or false -- respect it!
    private boolean             autoDeleteWasExplicitlySet = false;
}
