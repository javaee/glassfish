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

package org.glassfish.persistence.jpa;


import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import javax.naming.NamingException;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.sql.DataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.DeploymentUtils;

/**
 * This class implements {@link PersistenceUnitInfo} interface.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    /* This class is public because it is used in verifier */

    private static final String DEFAULT_PROVIDER_NAME =
            "org.eclipse.persistence.jpa.PersistenceProvider"; // NOI18N

    private static final String DEFAULT_DS_NAME = "jdbc/__default";  // NOI18N

    // We allow the default provider to be specified using -D option.
    private static String defaultProvider;

    private static Logger logger = LogDomains.getLogger(PersistenceUnitInfoImpl.class, LogDomains.LOADER_LOGGER);

    //TODO Need to move the relavent strings from the original bundle to somewhere here
    private static final StringManager localStrings = StringManager.getManager(PersistenceUnitInfoImpl.class);

    private PersistenceUnitDescriptor persistenceUnitDescriptor;

    private ProviderContainerContractInfo providerContainerContractInfo;

    private File absolutePuRootFile;

    private DataSource jtaDataSource;

    private DataSource nonJtaDataSource;

    private List<URL> jarFiles;


    public PersistenceUnitInfoImpl(
            PersistenceUnitDescriptor persistenceUnitDescriptor,
            ProviderContainerContractInfo providerContainerContractInfo) {
        this.persistenceUnitDescriptor = persistenceUnitDescriptor;
        this.providerContainerContractInfo = providerContainerContractInfo;
        jarFiles = _getJarFiles();
        String jtaDataSourceName = _calculateJtaDataSourceName();
        String nonJtaDataSourceName = _calculateNonJtaDataSourceName();
        try {
            jtaDataSource = jtaDataSourceName == null ? null : providerContainerContractInfo.lookupDataSource(jtaDataSourceName);
            nonJtaDataSource = nonJtaDataSourceName == null ? null : providerContainerContractInfo.lookupNonTxDataSource(nonJtaDataSourceName);
        } catch (NamingException e) {
			throw new RuntimeException(e);
		}

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

    public SharedCacheMode getSharedCacheMode() {
        return persistenceUnitDescriptor.getSharedCacheMode();
    }

    public ValidationMode getValidationMode() {
        return persistenceUnitDescriptor.getValidationMode();
    }

    /**
     * {@inheritDoc}
     */
    public Properties getProperties() {
        return persistenceUnitDescriptor.getProperties(); // its already a clone
    }

    public String getPersistenceXMLSchemaVersion() {
        return persistenceUnitDescriptor.getParent().getSpecVersion();
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getClassLoader() {
        return providerContainerContractInfo.getClassLoader();
    }

    /**
     * {@inheritDoc}
     */
    public void addTransformer(ClassTransformer transformer) {
        providerContainerContractInfo.addTransformer(transformer);
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getNewTempClassLoader() {
        return providerContainerContractInfo.getTempClassloader();
    }

    @Override public String toString() {
        /*
         * This method is used for debugging only.
         */
        StringBuilder result = new StringBuilder("<persistence-unit>"); // NOI18N
        result.append("\n\t<PURoot>").append(getPersistenceUnitRootUrl()).append("</PURoot>"); // NOI18N
        result.append("\n\t<name>").append(getPersistenceUnitName()).append("</name>"); // NOI18N
        result.append("\n\t<provider>").append(getPersistenceProviderClassName()).append("</provider>"); // NOI18N
        result.append("\n\t<transaction-type>").append(getTransactionType()).append("</transaction-type>"); // NOI18N
        result.append("\n\t<jta-data-source>").append(getJtaDataSource()).append("</jta-data-source>"); // NOI18N
        result.append("\n\t<non-jta-data-source>").append(getNonJtaDataSource()).append("</non-jta-data-source>"); // NOI18N
        for (URL jar : getJarFileUrls()) {
            result.append("\n\t<jar-file>").append(jar).append("</jar-file>"); // NOI18N
        }
        for (String mappingFile : getMappingFileNames()) {
            result.append("\n\t<mapping-file>").append(mappingFile).append("</mapping-file>"); // NOI18N
        }
        for (String clsName : getManagedClassNames()) {
            result.append("\n\t<class-name>").append(clsName).append("</class-name>"); // NOI18N
        }
        result.append("\n\t<exclude-unlisted-classes>").append(excludeUnlistedClasses()).append("</exclude-unlisted-classes>"); // NOI18N
        result.append("\n\t<properties>").append(getProperties()).append("</properties>"); // NOI18N
        result.append("\n\t<class-loader>").append(getClassLoader()).append("</class-loader>"); // NOI18N
        result.append("\n</persistence-unit>\n"); // NOI18N
        return result.toString();
    }

    protected String _calculateJtaDataSourceName() {
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
        logger.logp(Level.FINE, "PersistenceUnitLoaderImpl", // NOI18N
                "_getJtaDataSource", "JTADSName = {0}", // NOI18N
                DSName);
        return DSName;
    }

    protected String _calculateNonJtaDataSourceName() {
        /*
         * If non-JTA name is *not* provided
         * - use the JTA DS name (if supplied)
         * If non-JTA name is provided
         * - use non-JTA DS name
         * (this is done for ease of use, because user does not have to
         * explicitly mark a connection pool as non-transactional.
         * Calling lookupNonTxDataSource() with a resource which is
         * already configured as non-transactional has no side effects.)
         * If neither non-JTA nor JTA name is provided
         * use DEFAULT_DS_NAME.
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
        logger.logp(Level.FINE,
                "PersistenceUnitInfoImpl", // NOI18N
                "_getNonJtaDataSource", "nonJTADSName = {0}", // NOI18N
                DSName);
        return DSName;
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

                jarFile = new File(parentPath, DeploymentUtils.
                        getRelativeEmbeddedModulePath(parentPath.
                        getAbsolutePath(), nameComponent));
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
        // TODO caller of this method are _getJarFiles() and getPersitenceUnitRootUrl(). Both of them can be implemented using helper methods in PersistenceUnitDescriptor to better encapsulate
        if (absolutePuRootFile == null) {
            absolutePuRootFile = new File(providerContainerContractInfo.getApplicationLocation(),
                    getAbsolutePuRootWithinApplication().replace('/', File.separatorChar));
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
    private String getAbsolutePuRootWithinApplication() {
        // TODO shift this into PersistenceUnitDescriptor to better encapsulate 
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
                // The module is embedded in an ear (an ejb jar or war)
                final String moduleLocation =        // Would point to the directory where module is expanded. For example myejb_jar 
                        DeploymentUtils.getRelativeEmbeddedModulePath(
                        providerContainerContractInfo.getApplicationLocation(), module.getArchiveUri());
                return moduleLocation + '/' + puRoot; // see we always '/'
            }
        }
    }


    /**
     * This method first checks if default provider is specified in the
     * environment (e.g. using -D option in domain.xml). If so, we use that.
     * Else we defaults to EclipseLink.
     *
     * @return
     */
    public static String getDefaultprovider() {
        final String DEFAULT_PERSISTENCE_PROVIDER_PROPERTY =
                "com.sun.persistence.defaultProvider"; // NOI18N
        if(defaultProvider == null) {
            defaultProvider =
                    System.getProperty(DEFAULT_PERSISTENCE_PROVIDER_PROPERTY,
                        DEFAULT_PROVIDER_NAME);
        }

        return defaultProvider;
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
