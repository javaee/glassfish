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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import org.glassfish.persistence.common.*;

import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import javax.naming.NamingException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * For each persistence unit descriptors that is defined for 
 * an application create the ddl scripts. Additionally if the
 * user has requested the tables to be created or dropped from
 * the database complete that action too.
 *
 * These are the principles and expectations of the implementation.
 * We don't want TopLink code to execute the DDLs, it should only 
 * generate them. So, we always set the *generation-mode* to *script* 
 * in the PUInfo object before passing it to createContainerEMF(). 
 * As a result TopLink never creates the actual tables, nor does it drop 
 * them. The DDLs are executed by our code based on user preference which 
 * considers inputs from persistence.xml and CLI. We set the TopLink 
 * property to DROP_AND_CREATE in that map because we want it to always 
 * generate both create- and dropDDL.jdbc files.
 * @author pramodg
 */
public class JPAJava2DBProcessor {

    // Defining the persistence provider class names here that we would use to
    // check if java2db is supported.
    private static final String TOPLINK_PERSISTENCE_PROVIDER_CLASS_NAME_OLD =
        "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider"; // NOI18N
    private static final String TOPLINK_PERSISTENCE_PROVIDER_CLASS_NAME_NEW =
        "oracle.toplink.essentials.PersistenceProvider"; // NOI18N
    private static final String ECLIPSELINK_PERSISTENCE_PROVIDER_CLASS_NAME =
        "org.eclipse.persistence.jpa.PersistenceProvider"; // NOI18N

    // Constants for various property values.

    // Following constants are actually defined in
    // oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider
    // and org.eclipse.persistence.jpa.config.PersistenceUnitProperties
    // This code assumes that the value of constant at both the place is same
    private static final String CREATE_ONLY             = "create-tables"; //NOI18N
    private static final String DROP_AND_CREATE         = "drop-and-create-tables"; //NOI18N
    private static final String NONE                    = "none"; //NOI18N
    private static final String DDL_BOTH_GENERATION     = "both"; //NOI18N
    private static final String DDL_DATABASE_GENERATION = "database"; //NOI18N
    private static final String DDL_SQL_SCRIPT_GENERATION = "sql-script";

    // Constatns defined in oracle.toplink.essentials.config.TargetServer
    // and org.eclipse.persistence.jpa.config.TargetServer
    private static final String  SunAS9 = "SunAS9"; //NOI18N

    // property names for Toplink and EclipseLink
    private static final String TOPLINK_DDL_GENERATION     = "toplink.ddl-generation"; // NOI18N
    private static final String ECLIPSELINK_DDL_GENERATION = "eclipselink.ddl-generation"; // NOI18N

    private static final String TOPLINK_DDL_GENERATION_MODE     = "toplink.ddl-generation.output-mode"; // NOI18N
    private static final String ECLIPSELINK_DDL_GENERATION_MODE = "eclipselink.ddl-generation.output-mode"; // NOI18N

    private static final String TOPLINK_APP_LOCATION         = "toplink.application-location"; // NOI18N
    private static final String ECLIPSELINK_APP_LOCATION     = "eclipselink.application-location"; // NOI18N

    private static final String TOPLINK_CREATE_JDBC_DDL_FILE     = "toplink.create-ddl-jdbc-file-name"; // NOI18N
    private static final String ECLIPSELINK_CREATE_JDBC_DDL_FILE = "eclipselink.create-ddl-jdbc-file-name"; // NOI18N

    private static final String TOPLINK_DROP_JDBC_DDL_FILE       = "toplink.drop-ddl-jdbc-file-name"; // NOI18N
    private static final String ECLIPSELINK_DROP_JDBC_DDL_FILE   = "eclipselink.drop-ddl-jdbc-file-name"; // NOI18N

    private static final String TOPLINK_WEAVING       = "toplink.weaving"; // NOI18N
    private static final String ECLIPSELINK_WEAVING   = "persistence.tools.weaving"; // NOI18N

    private static final String TOPLINK_TARGET_SERVER = "toplink.target-server"; //NOI18N
    private static final String ECLIPSELINK_TARGET_SERVER = "eclipselink.target-server"; //NOI18N

    private static final String  FALSE = "false"; //NOI18N

    /**
     * Holds name of provider specific properties.
     */
    private ProviderPropertyNamesHolder providerPropertyNamesHolder;

    /**
     * The object that would be used by the
     * toplink code when creating a container.
     */
    private PersistenceUnitInfo pi;

    private static Logger logger = LogDomains.getLogger(JPAJava2DBProcessor.class, LogDomains.DPL_LOGGER);
    private  final static ResourceBundle messages = logger.getResourceBundle();

    // TODO
    private boolean create;

    private Java2DBProcessorHelper helper = null;

    /**
     * Creates a new instance of PersistenceProcessor
     * @param info the deployment info object.
     * @param create true if tables are to be created as part of this event.
     * @param cliCreateTables the cli string related to creating tables
     * at deployment time.
     * @param cliDropAndCreateTables the cli string that indicates that
     * old tables have to be dropped and new tables created.
     * @param cliDropTables the cli string to indicate that the tables
     * have to dropped at undeploy time.
    public JPAJava2DBProcessor(
            DeploymentEventInfo info, boolean create,
            String cliCreateTables, String cliDropAndCreateTables,
            String cliDropTables) {
        super(info, create, cliCreateTables,
            cliDropAndCreateTables, cliDropTables);
    }
     */

    /**
     * The entry point into this class. Process
     * any persistence unit descriptors if
     * found for this application.
     */
    protected void processApplication(PersistenceUnitDescriptor pu) {
        helper.setApplicationLocation();
        helper.setGeneratedLocation();

        processAppBundle(pu);
    }

    /**
     * This method does all the work of checking
     * and processing each persistence unit
     * descriptor.
     * @param bundle the persistence unit descriptor that is being worked on.
     */
    private void processAppBundle(PersistenceUnitDescriptor bundle) {
        String providerClassName = getProviderClassName(bundle);

        // Initialize with TLE names for each bundle to handle apps that have
        // multiple pus
        providerPropertyNamesHolder = new ProviderPropertyNamesHolder();

        // Override with EclipseLink names if running against EclipseLink
        if (ECLIPSELINK_PERSISTENCE_PROVIDER_CLASS_NAME.equals(providerClassName)) {
            providerPropertyNamesHolder.appLocation       = ECLIPSELINK_APP_LOCATION;
            providerPropertyNamesHolder.createJdbcDdlFile = ECLIPSELINK_CREATE_JDBC_DDL_FILE;
            providerPropertyNamesHolder.dropJdbcDdlFile   = ECLIPSELINK_DROP_JDBC_DDL_FILE;
            providerPropertyNamesHolder.ddlGeneration     = ECLIPSELINK_DDL_GENERATION;
            providerPropertyNamesHolder.ddlGenerationMode = ECLIPSELINK_DDL_GENERATION_MODE;
            providerPropertyNamesHolder.targetServer      = ECLIPSELINK_TARGET_SERVER;
            providerPropertyNamesHolder.weaving           = ECLIPSELINK_WEAVING;
        }

       String ddlGenerate = getPersistencePropVal(bundle,
                providerPropertyNamesHolder.ddlGeneration, NONE);
       String ddlMode = getPersistencePropVal(bundle,
                providerPropertyNamesHolder.ddlGenerationMode, DDL_BOTH_GENERATION);
        boolean createTables =
                getCreateTablesValue(ddlGenerate, ddlMode);
        boolean dropTables =
                getDropTablesValue(ddlGenerate, ddlMode);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(I18NHelper.getMessage(messages,
                "PersistenceProcessor.createanddroptables", //NOI18N
                new Object[] {createTables, dropTables}));
        }

        if (!createTables && !dropTables) {
            // Nothing to do.
            return;
        }

        constructJdbcFileNames(bundle);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(I18NHelper.getMessage(messages,
               "PersistenceProcessor.createanddropfilenames", //NOI18N
               helper.getCreateJdbcFileName(), helper.getDropJdbcFileName())); 
        }

        // These calls will be a no-op if it's not a corresponding event.
        dropTablesFromDB(dropTables, bundle);
        createTablesInDB(createTables, bundle, ddlMode);
    }

    /**
     * We need to create tables only on deploy, and only
     * if the CLI options cliCreateTables or cliDropAndCreateTables
     * are not set to false. If those options are not set (UNDEFINED)
     * the value is taken from the ddl-generate property if defined
     * in persistence.xml.
     * @param ddlGenerate the property name specified by the user.
     * @param ddlMode the property value specified by the user.
     * @return true if tables have to created.
     */
    protected boolean getCreateTablesValue(
            String ddlGenerate, String ddlMode) {
        boolean createTables =
            create;
/**
                && (cliCreateTables.equals(Constants.TRUE)
                    || (
                        (ddlGenerate.equals(CREATE_ONLY)
                            || ddlGenerate.equals(DROP_AND_CREATE))
                        && !ddlMode.equals(NONE))
                        && cliCreateTables.equals(Constants.UNDEFINED));
**/
        return createTables;
    }

    /**
     * We need to drop tables on deploy and redeploy, if the
     * corresponding CLI options cliDropAndCreateTables
     * (for redeploy) or cliDropTables (for undeploy) are not
     * set to false.
     * If the corresponding option is not set (UNDEFINED)
     * the value is taken from the ddl-generate property
     * if defined in persistence.xml.
     * @param ddlGenerate the property name specified by the user.
     * @param ddlMode the property value specified by the user.
     * @return true if the tables have to be dropped.
     */
    protected boolean getDropTablesValue(
            String ddlGenerate, String ddlMode) {
        boolean dropTables =
            !create;
/**
                && (cliDropAndCreateTables.equals(Constants.TRUE)
                    || cliDropTables.equals(Constants.TRUE)
                    || ((ddlGenerate.equals(DROP_AND_CREATE)
                        && (ddlMode.equals(DDL_DATABASE_GENERATION)
                            || ddlMode.equals(DDL_BOTH_GENERATION))
                        && cliDropAndCreateTables.equals(Constants.UNDEFINED)
                        && cliDropTables.equals(Constants.UNDEFINED))));
**/

        return dropTables;
    }

    /**
     * Construct the name of the create and
     * drop jdbc ddl files that would be
     * created. These name would be either
     * obtained from the persistence.xml file
     * (if the user has defined them) or we would
     * create default filenames
     * @param parBundle the persistence unit descriptor that is being worked on.
     */
    private void constructJdbcFileNames(PersistenceUnitDescriptor parBundle)  {
        String createJdbcFileName =
                getPersistencePropVal(parBundle,
                providerPropertyNamesHolder.createJdbcDdlFile, null);
        String dropJdbcFileName =
                getPersistencePropVal(parBundle,
                providerPropertyNamesHolder.dropJdbcDdlFile, null);

        if((null != createJdbcFileName) && (null != dropJdbcFileName)) {
            return;
        }

        String filePrefix =
                    Java2DBProcessorHelper.getDDLNamePrefix(parBundle.getParent().getParent());

        if(null == createJdbcFileName) {
            createJdbcFileName = filePrefix + DatabaseConstants.NAME_SEPARATOR + parBundle.getName() +
                DatabaseConstants.CREATE_DDL_JDBC_FILE_SUFFIX;
        }
        if(null == dropJdbcFileName) {
            dropJdbcFileName = filePrefix + DatabaseConstants.NAME_SEPARATOR + parBundle.getName() +
                DatabaseConstants.DROP_DDL_JDBC_FILE_SUFFIX;
        }

        helper.setCreateJdbcFileName(createJdbcFileName);
        helper.setDropJdbcFileName(dropJdbcFileName);
    }

    /**
     * We need to get the datasource  information if
     * that has been defined  in the persistence.xml.
     * Did not want to duplicate code here. So we
     * create a persistence unit info object and get
     * the nonJTADatasource from that object
     * Then if the drop file is present, drop the tables
     * from the database.
     * If the user has specified a persistence provider other than the default
     * toplink one, then java2db feature will not be a supported feature. In such
     * cases the drop file would not be present.
     *
     * @param dropTables true if the table need to be dropped.
     * @param bundle the persistence unit descriptor that is being worked on.
     */
    private void dropTablesFromDB(boolean dropTables,
            PersistenceUnitDescriptor bundle) {
        if(dropTables) {
            File dropFile = getDDLFile(bundle, helper.getDropJdbcFileName(), false);
            if (dropFile.exists()) {
                PersistenceUnitInfo pi = new Java2DBPersistenceUnitInfoImpl(
                    bundle,
                    helper.getDeployedLocation(),
                    null);
                executeDDLStatement(dropFile, pi.getNonJtaDataSource());
            } else if (isSupportedPersistenceProvider(bundle)){
                helper.logI18NWarnMessage(
                    "ejb.BaseProcessor.cannotdroptables", //NOI18N
                    helper.getAppRegisteredName(), dropFile.getName(), null);
            }

        }
    }

    /**
     * Currently java2db feature is supported for toplink persistence provider
     * only, as toplink is the default persistence provider for glassfish.
     * If the provider is toplink, call into toplink code to generate the ddl files.
     * Once the jdbc files have been created, use the create jdbc ddl file and
     * execute it against the database to have the required objects created.
     * @param createTables true if tables have to be created.
     * @param bundle the persistence unit descriptor that is being worked on
     * @param ddlMode the ddl-generate property value specified by the user.
     */
    private void createTablesInDB(boolean createTables,
            PersistenceUnitDescriptor bundle, String ddlMode) {
        if(createTables) {
            pi = loadPersistenceUnitBundle(bundle);

            // if pi is null it means that the user has defined a persistence
            // provider that is not supported. We should also skip DDL execution
            // if the user chose only to create the files.
            if ((null != pi)  && isDDLExecution(ddlMode)) {
                File createFile = getDDLFile(bundle, helper.getCreateJdbcFileName(), true);
                if(createFile.exists()) {
                    executeDDLStatement(createFile, pi.getNonJtaDataSource());
                } else {
                    helper.logI18NWarnMessage(
                        "ejb.BaseProcessor.cannotcreatetables", //NOI18N
                        helper.getAppRegisteredName(), createFile.getName(), null);
                }
            }
        }
    }


    /**
     * Get the ddl files eventually executed
     * against the database. This method deals
     * with both create and drop ddl files.
     * @param fileName the create or drop jdbc ddl file.
     * @param nonJtaDataSource the nonJtaDataSource
     *    that is obtained from the persistence info object that we had created.
     * @return true if the tables were successfully
     *    created/dropped from the database.
     */
    private boolean executeDDLStatement(File fileName, DataSource nonJtaDataSource ) {
        boolean result = false;
        Connection conn = null;
        Statement sql = null;
        try {
            try {
                conn = nonJtaDataSource.getConnection();
                sql = conn.createStatement();
                result = true;
            } catch (SQLException ex) {
                helper.logI18NWarnMessage(
                     "ejb.BaseProcessor.cannotConnect",
                    helper.getAppRegisteredName(),  null, ex);
            }
            if(result) {
                helper.executeDDLs(fileName, sql);
            }
        } catch (IOException e) {
            helper.fileIOError(helper.getAppRegisteredName(), e);
        } finally {
            helper.closeConn(conn);
        }
        return result;
    }


    /**
     * Since the ddl files are actually created
     * by the toplink code, we ensure that the
     * correct properties are put in the persistence
     * unit info object.
     * @param puDescriptor the persistence unit descriptor that is being worked on.
     */
    private void addPropertiesToPU(PersistenceUnitDescriptor puDescriptor) {
        addPropertyToDescriptor(puDescriptor, providerPropertyNamesHolder.targetServer,
                SunAS9);
        addPropertyToDescriptor(puDescriptor, providerPropertyNamesHolder.appLocation,
                helper.getGeneratedLocation());
        addPropertyToDescriptor(puDescriptor, providerPropertyNamesHolder.createJdbcDdlFile,
                helper.getCreateJdbcFileName());
        addPropertyToDescriptor(puDescriptor, providerPropertyNamesHolder.dropJdbcDdlFile,
                helper.getDropJdbcFileName());
    }

    /**
     * Utility method that is used to actually set the property into the persistence unit descriptor.
     * @param descriptor the persistence unit descriptor that is being worked on.
     * @param propertyName the name of the property.
     * @param propertyValue the value of the property.
     */
    private void addPropertyToDescriptor(PersistenceUnitDescriptor descriptor,
            String propertyName, String propertyValue) {
        String oldPropertyValue = descriptor.getProperties().getProperty(propertyName);
        if(null == oldPropertyValue) {
            descriptor.addProperty(propertyName, propertyValue);
        }
    }

    /**
     * Since the actual jdbc files are generated
     * by the toplink code, we need to create
     * a persistence unit info object and then
     * call into the toplink code.
     * @param persistenceUnitDescriptor the persistence unit descriptor that is
     * being worked on.
     * @return the persistence unit info object that
     * would used by  provider code.
     */
    private PersistenceUnitInfo loadPersistenceUnitBundle(
            PersistenceUnitDescriptor persistenceUnitDescriptor) {
        logger.entering("loadPersistenceUnitBundle", "load",
                new Object[]{persistenceUnitDescriptor});

        if (! isSupportedPersistenceProvider(persistenceUnitDescriptor)) {
            // Persistence provider is not supported, hence exit from java2db code
/**
            if (cliCreateTables.equals(Constants.TRUE) ||
                    cliDropAndCreateTables.equals(Constants.TRUE)) {
                helper.logI18NWarnMessage(
                    "ejb.PersistenceProcessor.nondefaultprovider",
                    getProviderClassName(persistenceUnitDescriptor),
                    persistenceUnitDescriptor.getName(), null);
            }
**/
            return null;
        }

        addPropertiesToPU(persistenceUnitDescriptor);

        // We should not override some properties, but pass them as a Map instead.
        Map<String, String> overrides = new HashMap<String, String>();
        overrides.put(providerPropertyNamesHolder.ddlGeneration, DROP_AND_CREATE);

        // Turn off enhancement during Java2DB. For details,
        // refer to https://glassfish.dev.java.net/issues/show_bug.cgi?id=1646
        overrides.put(providerPropertyNamesHolder.weaving, FALSE); 

        // As part of the deployment cycle we set the DDL_GENERATION_MODE. This
        // would ensure that the toplink code would create the required jdbc files,
        // but won't execute DDLs themselves.
        // As part of the normal application load we would set this property to a
        // value of "NONE".
        overrides.put(providerPropertyNamesHolder.ddlGenerationMode, DDL_SQL_SCRIPT_GENERATION);

        final ClassLoader cl = persistenceUnitDescriptor.getClassLoader();
        PersistenceUnitInfo pi = new Java2DBPersistenceUnitInfoImpl(
                persistenceUnitDescriptor,
                helper.getDeployedLocation(),
                cl);

        if(logger.isLoggable(Level.FINE))
            logger.fine("PersistenceInfo for PU is :\n" + pi);

        // Construct provider reflectively
        String providerClassname = getProviderClassName(persistenceUnitDescriptor);
        PersistenceProvider provider = null;
        EntityManagerFactory emf = null;
        try {
            provider = PersistenceProvider.class.cast(
                                        ClassLoader.class.cast(cl)
                                        .loadClass(providerClassname)
                                        .newInstance());
            emf = provider.createContainerEntityManagerFactory(pi, overrides);
            emf.createEntityManager();
            if(logger.isLoggable(Level.FINE))
                logger.fine(I18NHelper.getMessage(messages,
                    "PersistenceProcessor", new Object[] {"loadPersistenceUnitBundle", // NOI18N
                    "emf = {0}", emf})); // NOI18N
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if(emf != null) {
                emf.close();
            }
        }
        return pi;
    }

    /**
     * Given a persistence unit descriptor
     * return the value of a property if the
     * user has specified it.
     * If the user has not defined this property
     * return the default value.
     * @param parBundle the persistence unit descriptor that is being worked on.
     * @param propertyName the property name being checked.
     * @param defaultValue the default value to be used.
     * @return the property value.
     */
    private String getPersistencePropVal(PersistenceUnitDescriptor parBundle,
            String propertyName, String defaultValue) {
        return parBundle.getProperties().getProperty(propertyName, defaultValue);
    }

    /**
     * The java2db feature is currently implemented only for toplink (the default
     * persistence povider in glassfish). Hence we check for the name of the
     * persistence provider class name. It it is not toplink, stop processing.
     *
     * @param persistenceUnitDescriptor the persistence unit descriptor that is being worked on.
     * @return true if persistence provider is toplink.
     */
    private boolean isSupportedPersistenceProvider(
            final PersistenceUnitDescriptor persistenceUnitDescriptor) {

        String providerClassName = getProviderClassName(persistenceUnitDescriptor);

        return providerClassName.equals(TOPLINK_PERSISTENCE_PROVIDER_CLASS_NAME_OLD) ||
                providerClassName.equals(TOPLINK_PERSISTENCE_PROVIDER_CLASS_NAME_NEW) ||
                providerClassName.equals(ECLIPSELINK_PERSISTENCE_PROVIDER_CLASS_NAME);
    }

   /**
    * Create and read the ddl file constructing the proper disk location.
    * @param bundle the persistence unit descriptor that is being worked on.
    * @param fileName the name of the file.
    * @param create true if this event results in creating tables.
    * @return the jdbc ddl file.
    */
    private File getDDLFile(PersistenceUnitDescriptor bundle,
            String fileName, boolean create) {
        // Application location might be already set to the
        // generated directory but that would happen only if the
        // deploy happened in the same VM as a redeploy or an undeploy.
        String appLocation = getPersistencePropVal(bundle,
                providerPropertyNamesHolder.appLocation,
                helper.getGeneratedLocation());

        // Delegate the rest to the superclass.
        return helper.getDDLFile(appLocation + File.separator + fileName, create);
    }

    /**
     * Return provider class name as specified in the persistence.xml
     * or the default provider as known to the system.
     * @param persistenceUnitDescriptor the persistence unit descriptor.
     * @return provider class name as a String
     */
     private String getProviderClassName(
            PersistenceUnitDescriptor persistenceUnitDescriptor) {

        return PersistenceUnitInfoImpl.getPersistenceProviderClassNameForPuDesc(
                persistenceUnitDescriptor);
     }

    /**
     * Check if we should skip DDL execution which can be the case if the
     * user chose only to create the files.
     * @param ddlMode the ddl-generate property value specified by the user.
     * @return true if the DDL need to be executed in the database.
     */
     private boolean isDDLExecution (String ddlMode) {
         boolean rs = true;
/**
         if (cliCreateTables.equals(Constants.UNDEFINED)) {
             rs = !ddlMode.equals(DDL_SQL_SCRIPT_GENERATION);
         }
**/

         return rs;
     }

    /**
     * Java2DB create/drop tables during deploy/undeploy needs access to jdbc
     * resources in DAS <br>
     * This class will use the special api of connector runtime to get access to<br>
     * jdbc resources even if they are not enabled in DAS
     */
    class Java2DBPersistenceUnitInfoImpl extends PersistenceUnitInfoImpl {
        public Java2DBPersistenceUnitInfoImpl(
                PersistenceUnitDescriptor persistenceUnitDescriptor,
                String applicationLocation,
                ClassLoader classLoader) {
            // XXX super(persistenceUnitDescriptor, applicationLocation, classLoader);
            super(persistenceUnitDescriptor, null);
        }

        /**
         * get resource of type "__PM", if the resource is enabled in DAS <br>
         * <b>if the resource is available, and not enabled in DAS, resource without "__PM" capability will
         * be returned</b>
         *
         * @param DSName resource name
         * @return Object (datasource)  representing the resource
         * @throws NamingException when the resource is not available
         */
        protected DataSource lookupPMDataSource(String DSName) throws NamingException {
       	    // TODO - pass Habitat or ConnectorRuntime as an argument.
            Habitat habitat = Globals.getDefaultHabitat();
            DataSource ds = null;
            ConnectorRuntime connectorRuntime = habitat.getByContract(ConnectorRuntime.class);
            return (DataSource)connectorRuntime.lookupPMResource(DSName, true);
        }

        /**
         * get resource of type "nontx"
         *
         * @param DSName resource name
         * @return Object (datasource)  representing the resource
         * @throws NamingException when the resource is not available
         */
        protected DataSource  lookupNonJtaDataSource(String DSName) throws NamingException {
       	    // TODO - pass Habitat or ConnectorRuntime as an argument.
            Habitat habitat = Globals.getDefaultHabitat();
            DataSource ds = null;
            ConnectorRuntime connectorRuntime = habitat.getByContract(ConnectorRuntime.class);
            return (DataSource)connectorRuntime.lookupNonTxResource(DSName, true);
        }
    }

    /**
     * Holds names of provider specific property
     */
    private static class ProviderPropertyNamesHolder {
        // Initialize property names with TLE specific properties
        // for backward compatibility
        String appLocation       = TOPLINK_APP_LOCATION;
        String createJdbcDdlFile = TOPLINK_CREATE_JDBC_DDL_FILE;
        String dropJdbcDdlFile   = TOPLINK_DROP_JDBC_DDL_FILE;
        String ddlGeneration     = TOPLINK_DDL_GENERATION;
        String ddlGenerationMode = TOPLINK_DDL_GENERATION_MODE;
        String targetServer      = TOPLINK_TARGET_SERVER;
        String weaving           = TOPLINK_WEAVING;
    }

}
