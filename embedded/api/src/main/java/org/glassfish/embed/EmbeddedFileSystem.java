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

import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.*;
import java.net.MalformedURLException;
import static org.glassfish.embed.ServerConstants.*;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.net.URL;

/**
 * There are currently some ugly things we MUST do:
 * <ul>
 * <li>write out our hard-wired domain.xml to disk for use by core V3
 * <li>write out magic JDBC xml files to disk
 * </ul>
 * We are concentrating these things here for ease of maintenance.
 *
 * @author bnevins
 */
public final class EmbeddedFileSystem {
    public EmbeddedFileSystem() {
        //defaultRoots.mkdirs();
        //installRoot = defaultRoots;
        //instanceRoot = defaultRoots;
        //setSystemPropsInternal();
    }

    // ****************************************************
    // *************    public setters
    // ****************************************************

    public void setRoot(File f) throws EmbeddedException {
        setInstallRoot(f);
        setInstanceRoot(f);
    }
    
    public void setInstallRoot(File f) throws EmbeddedException {
        mustNotBeInitialized("setInstallRoot");
        installRoot = SmartFile.sanitize(f);
        f.mkdirs();

        if (!f.isDirectory()) {
            throw new EmbeddedException("bad_install_root", f);
        }
    }

    public void setInstanceRoot(File f) throws EmbeddedException {
        mustNotBeInitialized("setInstanceRoot");
        instanceRoot = SmartFile.sanitize(f);
        f.mkdirs();

        if (!f.isDirectory()) {
            throw new EmbeddedException("bad_instance_root", f);
        }
    }

    public void setDomainXmlFile(File f) throws EmbeddedException {
        mustNotBeInitialized("setDomainXmlFile");
        domainXmlFile = SmartFile.sanitize(f);
    }

    public void setDomainXmlUrl(URL url) throws EmbeddedException {
        mustNotBeInitialized("setDomainXmlUrl");
        domainXmlUrl = url;
    }

    public void setAutoDelete(boolean b) throws EmbeddedException {
        mustNotBeInitialized("setAutoDelete");
        autoDelete = b;
    }
    // ****************************************************
    // *************    public getters
    // ****************************************************


    public File getInstallRoot() throws EmbeddedException {
        mustBeInitialized("getInstallRoot");
        return installRoot;
    }

    public File getInstanceRoot() throws EmbeddedException {
        mustBeInitialized("getInstanceRoot");
        return instanceRoot;
    }

    public File getDomainXmlFile() throws EmbeddedException{
        mustBeInitialized("getDomainXmlFile");
        return domainXmlFile;
    }

    public URL getDomainXmlUrl() throws EmbeddedException{
        mustBeInitialized("getDomainXmlUrl");
        return domainXmlUrl;
    }
    // ****************************************************
    // *************    package private. Think long and hard before making public!
    // ****************************************************

    void cleanup() throws EmbeddedException {
        mustBeInitialized("cleanup");
        if (shouldCleanup()) {
            // note that Logger will not work now because the JVM has shut it down
            System.out.println("Cleaning up files");
            FileUtils.whack(installRoot);

            if(!instanceRoot.equals(installRoot))
                FileUtils.whack(instanceRoot);

            defaultsAreInUse = false;
        }
    }

    /*
    URL getDomainXmlUrl() {
        File dom = new File(getInstanceRoot(), "domain.xml");

        if(!dom.exists())
            return null;
        try {
            return dom.toURI().toURL();
        }
        catch (MalformedURLException ex) {
            return null;
        }
    }
    void validate() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
*/

    /* do NOT make this public!
     * if user set their own stuff - just validate.  If not then setup defaults
     * calling this method shuts the window on ALL setters so we don't have to
     * worry about getting into a weird inconsistent state later.
     */

    void initialize() throws EmbeddedException {
        if(instanceRoot == null || installRoot == null ) {
            if(defaultsAreInUse)
                throw new EmbeddedException("EFS_defaults_in_use");
            defaultsAreInUse = true;
        }

        if(installRoot == null)
            setInstallRoot(defaultInstallRoot);

        if(instanceRoot == null)
            setInstanceRoot(new File(installRoot, DEFAULT_PATH_TO_INSTANCE));

        initializeDomainXml(); // very complicated!!
        setSystemProps();

        initialized = true;
    }

    // ****************************************************
    // *************    private
    // ****************************************************


    /**
     * The idea here is that the Url ALWAYS points at the source of data.
     * the File always points t where on disk the data will be written to (and
     * maybe is the source of data)
     * @throws org.glassfish.embed.EmbeddedException
     */
    private void initializeDomainXml() throws EmbeddedException {
        if(domainXmlFile != null) {
            try {
                // File yes, url yes
                if (domainXmlUrl != null)
                    throw new EmbeddedException("EFS_two_domain");

                // File yes, url no
                if (!ok(domainXmlFile))
                    throw new EmbeddedException("EFS_bad_domain_xml_file", domainXmlFile);

                domainXmlUrl = domainXmlFile.toURI().toURL();
            }
            catch(MalformedURLException ex) {
                throw new EmbeddedException("EFS_error_making_URL", domainXmlFile, ex.toString());
            }
        }
        else {
            domainXmlFile = new File(instanceRoot, DEFAULT_PATH_TO_DOMAIN_XML);

            if(domainXmlUrl == null) {
                domainXmlUrl = DEFAULT_DOMAIN_XML_URL;
            }
        }
        File parent = new File(domainXmlFile, "..");
        parent.mkdirs();
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
        // don't EVER delete if they specified either directory

        if(autoDelete == true &&
                defaultInstallRoot.equals(installRoot)    &&
                defaultInstanceRoot.equals(instanceRoot))
            return true;

        return false;
    }

    private void mustBeInitialized(String name) throws EmbeddedException {
        if(!initialized)
            throw new EmbeddedException("must_be_initialized", name);
    }

    private void mustNotBeInitialized(String name) throws EmbeddedException {
        if(initialized)
            throw new EmbeddedException("must_not_be_initialized", name);
    }

    private boolean ok(File f) {
        return f.length() > 0L;
    }

    private static final File   defaultInstallRoot     = SmartFile.sanitize(new File(DEFAULT_GFE_DIR));
    private static final File   defaultInstanceRoot    = SmartFile.sanitize(new File(defaultInstallRoot, "domains/domain1"));
    private static boolean      defaultsAreInUse        = false;
    private File                installRoot;
    private File                instanceRoot;
    private File                domainXmlFile;
    private URL                 domainXmlUrl;
    private boolean             autoDelete      = true;
    private boolean             initialized     = false;
}
