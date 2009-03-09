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

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
//import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
//import com.sun.enterprise.deployment.backend.DeploymentStatus;

import com.sun.jdo.spi.persistence.support.sqlstore.ejb.DeploymentHelper;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.EJBHelper;
import com.sun.jdo.api.persistence.support.JDOFatalUserException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

/**
 * If the application contains cmp 2.x beans process them. Check if
 * tables have to created or dropped depending on where we are called
 * in a deploy/undeploy case.
 * @author pramodg
 */
public class CMPProcessor
    extends BaseProcessor {
    
    /**
     * Creates a new instance of CMPProcessor
     * @param info the deployment info object.
     * @param create true if tables must be created as part of this event
     * @param cliCreateTables the cli string related to creating tables
     * at deployment time.
     * @param cliDropAndCreateTables the cli string that indicates that
     * old tables have to be dropped and new tables created.
     * @param cliDropTables the cli string to indicate that the tables
     * have to dropped at undeploy time.
     */
/*
    public CMPProcessor(
            DeploymentEventInfo info, boolean create, 
            String cliCreateTables, String cliDropAndCreateTables, String cliDropTables) {
        super(info, create, cliCreateTables,
            cliDropAndCreateTables, cliDropTables);
    }
  */
    /**
     * The entry point into this class. Process
     * any ejb bundle descriptors if defined 
     * for this application.
     */
    protected void processApplication() {        
        Collection bundleCollection = application.getEjbBundleDescriptors();
        if ( bundleCollection  == null) {
            return;
        }
        Iterator bundleItr = bundleCollection.iterator();

        // Process only those Bundle descriptors that contain CMP beans.
        while ( bundleItr.hasNext() ) {
             processAppBundle((EjbBundleDescriptor)bundleItr.next()); 
        } 
    } 
    
    /**
     * This method does all the work of checking
     * and processing each ejb bundle descriptor. 
     * @param bundle the ejb bundle descriptor that is being worked on.
     */
   private void processAppBundle(EjbBundleDescriptor bundle) {
           if (!bundle.containsCMPEntity()) {
                return;
            }

            ResourceReferenceDescriptor cmpResource = 
                    bundle.getCMPResourceReference();

            // If this bundle's beans are not created by Java2DB, then skip to
            // next bundle.
            if (!DeploymentHelper.isJavaToDatabase(
                    cmpResource.getSchemaGeneratorProperties())) {
                return;
            }
      
            boolean createTables = getCreateTablesValue(cmpResource) ;                   
            boolean dropTables = getDropTablesValue(cmpResource);

            if (debug) {                
                logger.fine("ejb.CMPProcessor.createanddroptables", //NOI18N
                    new Object[] {new Boolean(createTables), new Boolean(dropTables)});
            }

            if (!createTables && !dropTables) { 
                // Nothing to do.
                return;
            }
        
            // At this point of time we are sure that we would need to create 
            // the sql/jdbc files required to create or drop objects from the 
            // database. Hence setup the required directories from the info object.
            setApplicationLocation();
            setGeneratedLocation();
            
            constructJdbcFileNames(bundle);
            if (debug) {
                logger.fine("ejb.CMPProcessor.createanddropfilenames", 
                    createJdbcFileName, dropJdbcFileName); //NOI18N            
            }            

            String resourceName = cmpResource.getJndiName();
            if (dropTables) {
                executeStatements(dropJdbcFileName, resourceName);
            } else { 
                // else can only be createTables as otherwise we'll not reach here
                executeStatements(createJdbcFileName, resourceName);
            }
   }    

   /**
     * We need to create tables only on  deploy, and 
     * only if the CLI options cliCreateTables or 
     * cliDropAndCreateTables are not set to false. 
     * If those options are not set (UNDEFINED)
     * the value is taken from the 
     * create-tables-at-deploy element of the
     * sun-ejb-jar.xml 
     * (cmpResource.isCreateTablesAtDeploy()).
     * @param cmpResource the cmp resource reference descriptor.
     * @return true if tables have to created.
     */
    protected boolean getCreateTablesValue(
            ResourceReferenceDescriptor cmpResource) {
            boolean createTables = 
                create 
/*
                    && (cliCreateTables.equals(Constants.TRUE)
                        || (cmpResource.isCreateTablesAtDeploy()
                            && cliCreateTables.equals(Constants.UNDEFINED)))*/;
            return createTables;
    }    

    /**
     *  We need to drop tables on undeploy and redeploy, 
     * if the corresponding CLI options cliDropAndCreateTables 
     * (for redeploy) or cliDropTables (for undeploy) are 
     * not set to false. 
     * If the corresponding option is not set (UNDEFINED)
     * the value is taken from the drop-tables-at-undeploy 
     * element of the sun-ejb-jar.xml 
     * (cmpResource.isDropTablesAtUndeploy()).
     * @param cmpResource the cmp resource reference descriptor.
     * @return true if the tables have to be dropped.
     */
    protected boolean getDropTablesValue(
            ResourceReferenceDescriptor cmpResource) {
        boolean dropTables = 
            (!create 
                /*&& (cliDropAndCreateTables.equals(Constants.TRUE)
                    || cliDropTables.equals(Constants.TRUE) 
                    || (cmpResource.isDropTablesAtUndeploy()
                        && cliDropAndCreateTables.equals(Constants.UNDEFINED)
                        && cliDropTables.equals(Constants.UNDEFINED)))*/);
       return dropTables;
    } 
    
    /**
     * Construct the name of the create and 
     * drop jdbc ddl files that would be 
     * created. These name would be either
     * obtained from the persistence.xml file
     * (if the user has defined them) or we would
     * create default filenames
     * @param ejbBundle the ejb bundle descriptor being worked on.
     */
    private void  constructJdbcFileNames(EjbBundleDescriptor ejbBundle) {
        String filePrefix = EJBHelper.getDDLNamePrefix(ejbBundle);
        
        createJdbcFileName = filePrefix + CREATE_DDL_JDBC_FILE_SUFFIX;
        dropJdbcFileName = filePrefix + DROP_DDL_JDBC_FILE_SUFFIX;
    }
    
    /**
     * If the file is present, execute the corresponding statements
     * in the database.
     * @param fileName the name of the file to execute.
     * @param resourceName the jdbc resource name that would be used 
     * to get a connection to the database.
     */
    private void executeStatements(
            String fileName, String resourceName) {
        File file = getDDLFile(
                appGeneratedLocation + fileName, false);
        if (file.exists()) {
            executeDDLStatement(file, resourceName);
        } else {
            logI18NWarnMessage(
                 ((create)? "ejb.BaseProcessor.cannotcreatetables" //NOI18N
                 : "ejb.BaseProcessor.cannotdroptables"), //NOI18N
                appRegisteredName, file.getName(), null);
        }
    }
    
    /**
     * Get the ddl files eventually executed 
     * against the database. This method deals 
     * with both create and drop ddl files.
     * @param fileName  the create or drop jdbc ddl file.
     * @param resourceName the jdbc resource name that would be used 
     * to get a connection to the database.
     * @return true if the tables were successfully 
     *    created/dropped from the database. 
     */
    private boolean executeDDLStatement(File fileName, String resourceName) {
        boolean result = false;
        Connection conn = null;
        Statement sql = null;
        try {
            try {           
                    conn = DeploymentHelper.getConnection(resourceName);
                    sql = conn.createStatement();
                    result = true;
                } catch (SQLException ex) {
                    cannotConnect(resourceName, ex);
                } catch (JDOFatalUserException ex) {
                    cannotConnect(resourceName, ex);
                }
        
                if(result) {               
                    executeDDLs(fileName, sql);
                }
        } catch (IOException e) {
            fileIOError(application.getRegistrationName(), e);            
        } finally { 
            closeConn(conn);
        }
        return result;        
    }
        
}
