/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.server;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.deploy.shared.OutputJarArchive;
import com.sun.enterprise.loader.InstrumentableClassLoader;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.shared.ArchivistUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.persistence.spi.ClassTransformer;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements {@link PersistenceUnitInfo} interface.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    /* This class is public because it is used in verifier */

    private static final String DEFAULT_PROVIDER_NAME =
            "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider"; // NOI18N

    private static final String DEFAULT_DS_NAME = "jdbc/__default";  // NOI18N

    // We allow the default provider to be specified using -D option.
    private static String defaultProvider;

    private static Logger logger = LogDomains.getLogger(
            LogDomains.LOADER_LOGGER);;

    private static StringManager localStrings = StringManager.getManager(
            "com.sun.enterprise.server"); // NOI18N

    private PersistenceUnitDescriptor persistenceUnitDescriptor;

    private String applicationLocation;

    private File absolutePuRootFile;

    private DataSource jtaDataSource;

    private DataSource nonJtaDataSource;

    private List<URL> jarFiles;

    private InstrumentableClassLoader classLoader;

    public PersistenceUnitInfoImpl(
            PersistenceUnitDescriptor persistenceUnitDescriptor,
            String applicationLocation,
            InstrumentableClassLoader classLoader) {
        this.persistenceUnitDescriptor = persistenceUnitDescriptor;
        this.applicationLocation = applicationLocation;
        this.classLoader = classLoader;
        jarFiles = _getJarFiles();
        jtaDataSource = _getJtaDataSource();
        nonJtaDataSource = _getNonJtaDataSource();
    }

    // Implementation of PersistenceUnitInfo interface

    /**
     * {@inheritDoc}
     */
    public String getPersistenceUnitName() {
        return persistenceUnitDescriptor.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getPersistenceProviderClassName() {
        return getPersistenceProviderClassNameForPuDesc(persistenceUnitDescriptor);
    }

    /**
     * {@inheritDoc}
     */
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(
                persistenceUnitDescriptor.getTransactionType());
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public URL getPersistenceUnitRootUrl() {
        try {
            return getAbsolutePuRootFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getMappingFileNames() {
        return persistenceUnitDescriptor.getMappingFiles(); // its already unmodifiable
    }

    /**
     * {@inheritDoc}
     */
    public List<URL> getJarFileUrls() {
        return jarFiles;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getManagedClassNames() {
        return persistenceUnitDescriptor.getClasses(); // its already unmodifiable
    }

    public boolean excludeUnlistedClasses() {
        return persistenceUnitDescriptor.isExcludeUnlistedClasses();
    }

    /**
     * {@inheritDoc}
     */
    public Properties getProperties() {
        return persistenceUnitDescriptor.getProperties(); // its already a clone
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getClassLoader() {
        return ClassLoader.class.cast(classLoader);
    }

    /**
     * {@inheritDoc}
     */
    public void addTransformer(ClassTransformer transformer) {
        classLoader.addTransformer(transformer);
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getNewTempClassLoader() {
        return classLoader.copy();
    }

    @Override public String toString() {
        /*
         * This method is used for debugging only.
         */
        StringBuilder result = new StringBuilder("<persistence-unit>"); // NOI18N
        result.append("\n\t<PURoot>" + getPersistenceUnitRootUrl() + "</PURoot>"); // NOI18N
        result.append("\n\t<name>" + getPersistenceUnitName() + "</name>"); // NOI18N
        result.append("\n\t<provider>" + getPersistenceProviderClassName() + // NOI18N
                "</provider>"); // NOI18N
        result.append("\n\t<transaction-type>" + getTransactionType() + // NOI18N
                "</transaction-type>"); // NOI18N
        result.append("\n\t<jta-data-source>" + getJtaDataSource() + // NOI18N
                "</jta-data-source>"); // NOI18N
        result.append("\n\t<non-jta-data-source>" + getNonJtaDataSource() + // NOI18N
                "</non-jta-data-source>"); // NOI18N
        for (URL jar : getJarFileUrls()) {
            result.append("\n\t<jar-file>" + jar + "</jar-file>"); // NOI18N
        }
        for (String mappingFile : getMappingFileNames()) {
            result.append("\n\t<mapping-file>" + mappingFile + // NOI18N
                    "</mapping-file>"); // NOI18N
        }
        for (String clsName : getManagedClassNames()) {
            result.append("\n\t<class-name>" + clsName + "</class-name>"); // NOI18N
        }
        result.append("\n\t<exclude-unlisted-classes>" + excludeUnlistedClasses() + // NOI18N
                "</exclude-unlisted-classes>"); // NOI18N
        result.append("\n\t<properties>" + getProperties() + "</properties>"); // NOI18N
        result.append("\n\t<class-loader>" + getClassLoader() + // NOI18N
                "</class-loader>"); // NOI18N
        result.append("\n</persistence-unit>\n"); // NOI18N
        return result.toString();
    }

    protected DataSource _getJtaDataSource() {
        /*
         * Use DEFAULT_DS_NAME iff user has not specified both jta-ds-name
         * and non-jta-ds-name; and user has specified transaction-type as JTA.
         * See Gf issue #1204 as well.
         */
        if (getTransactionType() != PersistenceUnitTransactionType.JTA) {
            logger.logp(Level.FINE,
                    "PersistenceUnitInfoImpl", // NOI18N
                    "_getJtaDataSource", // NOI18N
                    "This PU is configured as non-jta, so jta-data-source is null"); // NOI18N
            return null; // this is a non-jta-data-source
        }
        String DSName;
        String userSuppliedJTADSName = persistenceUnitDescriptor.getJtaDataSource();
        if (!isNullOrEmpty(userSuppliedJTADSName)) {
            DSName = userSuppliedJTADSName; // use user supplied jta-ds-name
        } else if (isNullOrEmpty(persistenceUnitDescriptor.getNonJtaDataSource())) {
            DSName = DEFAULT_DS_NAME;
        } else {
            String msg = localStrings.getString("puinfo.jta-ds-not-configured", // NOI18N
                    new Object[] {persistenceUnitDescriptor.getName()});
            throw new RuntimeException(msg);
        }
        try {
            logger.logp(Level.FINE, "PersistenceUnitLoaderImpl", // NOI18N
                    "_getJtaDataSource", "JTADSName = {0}", // NOI18N
                    DSName);
            return DataSource.class.cast(lookupPMDataSource(DSName));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get resource of type "__PM"
     * @param DSName resource name
     * @return Object (datasource)  representing the resource
     * @throws NamingException when the resource is not available
     */
    protected DataSource  lookupPMDataSource(String DSName) throws NamingException {
        return (DataSource)ConnectorRuntime.getRuntime().lookupPMResource(DSName, false);
    }

    /**
     * get resource of type "nontx"
     * @param DSName resource name
     * @return Object (datasource)  representing the resource
     * @throws NamingException when the resource is not available
     */
    protected DataSource lookupNonJtaDataSource(String DSName) throws NamingException {
        return (DataSource)ConnectorRuntime.getRuntime().lookupNonTxResource(DSName, false);
    }

    protected DataSource _getNonJtaDataSource() {
        /*
         * If non-JTA name is *not* provided
         * - use the JTA DS name (if supplied) to call lookupNonTxResource
         * If non-JTA name is provided
         * - use non-JTA DS name to call lookupNonTxResource
         * (this is done for ease of use, because user does not have to
         * explicitly mark a connection pool as non-transactional.
         * Calling lookupNonTxResource() with a resource which is
         * already configured as non-transactional has no side effects.)
         * If neither non-JTA nor JTA name is provided
         * use DEFAULT_DS_NAME to call lookupNonTxResource
         */
        String DSName;
        String userSuppliedNonJTADSName = persistenceUnitDescriptor.getNonJtaDataSource();
        if (!isNullOrEmpty(userSuppliedNonJTADSName)) {
            DSName = userSuppliedNonJTADSName;
        } else {
            String userSuppliedJTADSName = persistenceUnitDescriptor.getJtaDataSource();
            if (!isNullOrEmpty(userSuppliedJTADSName)) {
                DSName = userSuppliedJTADSName;
            } else {
                DSName = DEFAULT_DS_NAME;
            }
        }
        try {
            logger.logp(Level.FINE,
                    "PersistenceUnitInfoImpl", // NOI18N
                    "_getNonJtaDataSource", "nonJTADSName = {0}", // NOI18N
                    DSName);
            return DataSource.class.cast(lookupNonJtaDataSource(DSName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<URL> _getJarFiles() {
        List<String> jarFileNames = new ArrayList<String>(
                persistenceUnitDescriptor.getJarFiles());
        List<URL> jarFiles = new ArrayList<URL>(jarFileNames.size() + 1);
        String absolutePuRoot = getAbsolutePuRootFile().getAbsolutePath();
        for (String jarFileName : jarFileNames) {
            String nativeJarFileName = jarFileName.replace('/',
                    File.separatorChar);
            final File parentFile = new File(absolutePuRoot).getParentFile();
            // only components are exploded, hence first look for original archives.
            File jarFile = new File(parentFile, nativeJarFileName);
            if (!jarFile.exists()) {
                // if the referenced jar is itself a component, then
                // it might have been exploded, hence let's see
                // if that is the case.

                // let's calculate the name component and path component from this URI
                // e.g. if URI is ../../foo_bar/my-ejb.jar,
                // name component is foo_bar/my-ejb.jar and
                // path component is ../../
                // These are my own notions used here.
                String pathComponent = "";
                String nameComponent = jarFileName;
                if(jarFileName.lastIndexOf("../") != -1) {
                    final int separatorIndex = jarFileName.lastIndexOf("../")+3;
                    pathComponent = jarFileName.substring(0,separatorIndex);
                    nameComponent = jarFileName.substring(separatorIndex);
                }
                logger.fine("For jar-file="+ jarFileName+ ", " + // NOI18N
                        "pathComponent=" +pathComponent + // NOI18N
                        ", nameComponent=" + nameComponent); // NOI18N
                File parentPath = new File(parentFile, pathComponent);
                
                // XXX JD : We will need a better way to handle this
                /*jarFile = new File(parentPath, DeploymentUtils.
                        getRelativeEmbeddedModulePath(parentPath.
                        getAbsolutePath(), nameComponent));
                 */
            }
            if (jarFile.exists()) {
                try {
                    jarFiles.add(jarFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Should be a caught by verifier. So, just log a message
                String msg = localStrings.getString("puinfo.referenced_jar_not_found",
                        new Object[]{absolutePuRoot, jarFileName, jarFile});
                logger.log(Level.WARNING, msg);
            }
        }
        return jarFiles;
    }

    private File getAbsolutePuRootFile() {
        if (absolutePuRootFile == null) {
            absolutePuRootFile = new File(applicationLocation,
                    getAbsolutePuRoot().replace('/', File.separatorChar));
            if (!absolutePuRootFile.exists()) {
                throw new RuntimeException(
                        absolutePuRootFile.getAbsolutePath() + " does not exist!");
            }
        }
        return absolutePuRootFile;
    }

    /**
     * This method calculates the absolute path of the root of a PU. 
     * Absolute path is not the path with regards to root of file system. 
     * It is the path from the root of the Java EE application this 
     * persistence unit belongs to.
     * Returned path always uses '/' as path separator.
     * @return the absolute path of the root of this persistence unit
     */
    private String getAbsolutePuRoot() {
        RootDeploymentDescriptor rootDD = persistenceUnitDescriptor.getParent().
                getParent();
        String puRoot = persistenceUnitDescriptor.getPuRoot();
        if(rootDD.isApplication()){
            return puRoot;
        } else {
            ModuleDescriptor module = BundleDescriptor.class.cast(rootDD).
                    getModuleDescriptor();
            if(module.isStandalone()) {
                return puRoot;
            } else {
                final String moduleLocation =
                        DeploymentUtils.getRelativeEmbeddedModulePath(
                        applicationLocation, module.getArchiveUri());
                return moduleLocation + '/' + puRoot; // see we always '/'
            }
        }
    }


    /**
     * This method first checks if default provider is specified in the
     * environment (e.g. using -D option in domain.xml). If so, we use that.
     * Else we defaults to TopLink.
     *
     * @return
     */
    private static String getDefaultprovider() {
        final String DEFAULT_PERSISTENCE_PROVIDER_PROPERTY =
                "com.sun.persistence.defaultProvider"; // NOI18N
        if(defaultProvider == null) {
            defaultProvider =
                    System.getProperty(DEFAULT_PERSISTENCE_PROVIDER_PROPERTY,
                        DEFAULT_PROVIDER_NAME);
        }

        return defaultProvider;
    }

    /**
     * Utility method to create a Jar file out of a directory.
     * Right now this not used because TopLink Essential can handle
     * exploded directories, but for better pluggability with different provoder
     * we may have to do use this method to create a jar out of exploded dir.
     *
     * @param sourcePath      is the source dir name.
     * @param destinationPath is the target jar file that's going to be created.
     *                        destinationPath first gets deleted if already
     *                        exists.
     * @throws java.io.IOException
     */
    private static void createJar(String sourcePath, String destinationPath)
            throws IOException {
        FileArchive source = new FileArchive();
        OutputJarArchive destination = new OutputJarArchive();
        try {
            source.open(sourcePath);
            destination.create(destinationPath);
            for (Enumeration entries = source.entries();
                 entries.hasMoreElements();) {
                String entry = String.class.cast(entries.nextElement());
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = source.getEntry(entry);
                    os = destination.putNextEntry(entry);
                    ArchivistUtils.copyWithoutClose(is, os);
                } finally {
                    if (is != null) is.close();
                    if (os != null) destination.closeEntry();
                }
            }
        } finally {
            source.close();
            destination.close();
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }
    
    public static String getPersistenceProviderClassNameForPuDesc(
            PersistenceUnitDescriptor persistenceUnitDescriptor) {
        String provider = persistenceUnitDescriptor.getProvider();
        if (isNullOrEmpty(provider)) {
            provider = getDefaultprovider();
        }
        return provider;
    }
    
}
