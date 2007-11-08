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

package com.sun.enterprise.ee.diagnostics.collect;

import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.Defaults;
import com.sun.enterprise.diagnostics.collect.*;
import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.admin.server.core.jmx.AppServerMBeanServerFactory;
import com.sun.enterprise.admin.server.core.jmx.InitException;
import com.sun.logging.LogDomains;

import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * To Collect HADB related information
 *
 * @author Jagadish Ramu
 */
public class HadbInfoCollector implements Collector {

    private static final String GET_COMMAND = " get ";
    private static final String LIST_COMMAND = " list ";

    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);


    private static final String HADBM = "hadbm ";
    private static int ERROR = -1;
    private static int NOERROR = 0;
    private static boolean isOldHadbVer = false;

    private String adminPassword;
    private String databaseName;
    private String clusterName;
//    private String fileName;
    private String destFolder;

    private static final String OBJECT_NAME =
            "com.sun.appserv:type=hadb-config,category=config";

    public final static String STRING = (new String()).getClass().getName();


    /**
     * To collect HADB related Information for the given cluster
     * @param clusterName
     */
    public HadbInfoCollector(String clusterName, String destFolder) {
        try{
            this.clusterName = clusterName;
            initialize(clusterName);
            this.destFolder = destFolder;
        }catch(ConfigException ce){
            logger.log(Level.WARNING, ce.getMessage(), ce.fillInStackTrace());
        }
    }

    /**
     * Retrieves the password for the HADB to get database properties.
     * @param clusterName
     * @throws ConfigException
     */
    private void initialize(String clusterName) throws ConfigException {

        ConfigContext configContext = com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext();

        Domain domain = ServerBeansFactory.getDomainBean(configContext);

        Clusters clusters = domain.getClusters();
        Cluster cluster = clusters.getClusterByName(clusterName);

        String configReference = cluster.getConfigRef();

        Config config = ConfigAPIHelper.getConfigByName(configContext, configReference);

        AvailabilityService availService = config.getAvailabilityService();

        if( availService.isAvailabilityEnabled() ){
           String storePoolName = availService.getStorePoolName();
            databaseName = availService.getHaStoreName();

            if( Resources.JDBC_RESOURCE.equals(ResourceHelper.getResourceType(configContext, storePoolName))){
                ConfigBean resource = ResourceHelper.findResource(configContext,storePoolName);

                if(resource instanceof JdbcResource){
                    JdbcResource jdbcResource = (JdbcResource)resource ;

                    String poolName = jdbcResource.getPoolName();

                    if( Resources.JDBC_CONNECTION_POOL.equals(ResourceHelper.getResourceType(configContext, poolName))){
                        ConfigBean conPoolResource = ResourceHelper.findResource(configContext, poolName);

                        if(conPoolResource instanceof JdbcConnectionPool){
                            ElementProperty passwordProperty = ((JdbcConnectionPool)conPoolResource).getElementPropertyByName("Password");
                            adminPassword = passwordProperty.getValue();
                        }
                    }
                }
            }
        }
    }

    /**
     * Capture HADB databases information
     *
     * @ throws DiagnosticException
     */
    public Data capture() throws DiagnosticException {

         Data data = null;

        if(adminPassword==null ){
            logger.log(Level.WARNING,"Admin Password in HADBInfoCollector is null");
            return data;
        }

        if(databaseName==null ){
            logger.log(Level.WARNING,"Database Name in HADBInfoCollector is null");
            return data;
        }


        final Object[] params = {null,null,adminPassword,null,databaseName};
        final String[] types = new String[]{STRING,STRING,STRING,STRING,STRING};
        final MBeanServer mbs = AppServerMBeanServerFactory.getMBeanServerInstance();
        try{
            // we always invoke with an array, and so the
            //  result is always an Object []
            final Object[] returnValues = (Object[]) mbs.invoke(new ObjectName(OBJECT_NAME),
                    "getHADBInfo", params, types);

            File parent = new File(destFolder);
            parent.mkdirs();
            String fileName = destFolder + Defaults.HADB_INFO_FILE  + "_" + clusterName ;
            File file = new File(fileName);


            PrintWriter writer = new PrintWriter(fileName);

            for(Object value : returnValues){
                writer.write(value.toString() + "\n");
            }
            writer.close();

            data = new FileData(file.getName(),DataType.HADB_INFO);
       }
        catch(Exception e){
            logger.log(Level.WARNING, e.getMessage(), e.fillInStackTrace());
        }
        return data;
    }

    /**
     * getter for admin password
     *
     * @return String
     */
    public String getAdminPassword() {
        return adminPassword;
    }

    /**
     * setter for admin password
     *
     * @param adminPassword
     */
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
