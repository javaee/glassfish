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

/*
 * DeploymentHelper.java
 *
 * Created on September 30, 2003.
 */

package com.sun.jdo.spi.persistence.support.sqlstore.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sun.jdo.api.persistence.support.JDOFatalUserException;
import com.sun.jdo.api.persistence.support.PersistenceManagerFactory;
import com.sun.jdo.spi.persistence.support.sqlstore.LogHelperPersistenceManager;

import com.sun.jdo.spi.persistence.utility.I18NHelper;
import com.sun.jdo.spi.persistence.utility.StringHelper;
import com.sun.jdo.spi.persistence.utility.database.DatabaseConstants;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.connectors.ConnectorRuntime;

/** 
 * This class is used for static method invocations to avoid unnecessary
 * registration requirements to use EJBHelper and/or CMPHelper from 
 * deploytool, verifier, or any other stand-alone client.
 * 
 */
public class DeploymentHelper
    {

    /** 
     * Default DDL name prefix. Need to have something to avoid
     * generating hidden names when a suffix is added to an empty string.
     * E.g. <code>.dbschema</code> name can be difficult to find,
     * while <code>default.dbschema</code> will signal that the default
     * had been used.
     **/
    private final static String DEFAULT_NAME = "default"; // NOI18N

    /** I18N message handler */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
        "com.sun.jdo.spi.persistence.support.sqlstore.Bundle", // NOI18N
        DeploymentHelper.class.getClassLoader());

    /** The logger */
    private static Logger logger = LogHelperPersistenceManager.getLogger();

    /** 
     * Returns name prefix for DDL files extracted from the info instance by the
     * Sun-specific code.
     *   
     * @param info the instance to use for the name generation.
     * @return name prefix as String. 
     */   
    public static String getDDLNamePrefix(Object info) { 
        StringBuffer rc = new StringBuffer();

        if (info instanceof BundleDescriptor) {
            BundleDescriptor bundle = (BundleDescriptor)info;
            rc.append(bundle.getApplication().getRegistrationName());

            Application application = bundle.getApplication();
            if (!application.isVirtual()) {
                String modulePath = bundle.getModuleDescriptor().getArchiveUri();
                int l = modulePath.length();

                // Remove ".jar" from the module's jar name.
                rc.append(DatabaseConstants.NAME_SEPARATOR).
                    append(modulePath.substring(0, l - 4));
            }

        } // no other option is available at this point.

        return (rc.length() == 0)? DEFAULT_NAME : rc.toString();
    }

    /**
     * Returns javatodb flag for this EjbBundleDescriptor
     * @param bundle a EjbBundleDescriptor
     * @return true if there is a property entry associated with the
     * corresponding cmp-resource element, which contains "true" as
     * the value for the <code>DatabaseConstants.JAVA_TO_DB_FLAG</code>
     * key.
     */  
    public static boolean isJavaToDatabase(EjbBundleDescriptor bundle) {
        Properties userPolicy = bundle.getCMPResourceReference()
                .getSchemaGeneratorProperties();
        return isJavaToDatabase(userPolicy);
    }

    /**
     * Returns boolean value for the <code>DatabaseConstants.JAVA_TO_DB_FLAG</code>
     * flag in this Properties object.
     * @param prop a Properties object where flag is located
     * @return true if there is a property value that contains "true" as
     * the value for the <code>DatabaseConstants.JAVA_TO_DB_FLAG</code>
     * key.
     */  
    public static boolean isJavaToDatabase(Properties prop) {
        if (prop != null) {
            String value = prop.getProperty(DatabaseConstants.JAVA_TO_DB_FLAG);
            if (! StringHelper.isEmpty(value)) {
                 if (logger.isLoggable(Logger.FINE))
                     logger.fine(DatabaseConstants.JAVA_TO_DB_FLAG + " property is set."); // NOI18N
                 return Boolean.valueOf(value).booleanValue();
            }
        }
        return false;
    }

    /** Get a Connection from the resource specified by the JNDI name 
     * of a CMP resource.
     * This connection is aquired from a special PM resource which allows
     * to use its connections outside of a business method invocation.
     * The deployment processing is required to use only those connections.
     *
     * @param name JNDI name of a cmp-resource for the connection.
     * @return a Connection.
     * @throws JDOFatalUserException if name cannot be looked up, or we
     * cannot get a connection based on the name.
     * @throws SQLException if can not get a Connection.
     */  
    public static Connection getConnection(String name) throws SQLException {
        if (logger.isLoggable(logger.FINE)) {
            logger.fine("ejb.DeploymentHelper.getconnection", name); //NOI18N
        }
        return ConnectorRuntime.getRuntime().getConnection(name);
    }    

    /** Create a RuntimeException for unexpected instance returned
     * from JNDI lookup.
     *
     * @param name the JNDI name that had been looked up.
     * @param value the value returned from the JNDI lookup.
     * @throws JDOFatalUserException.
     */
    private static void handleUnexpectedInstance(String name, Object value) {
        RuntimeException e = new JDOFatalUserException(
                I18NHelper.getMessage(messages,
                        "ejb.jndi.unexpectedinstance", //NOI18N
                        name, value.getClass().getName()));
        logger.severe(e.toString());
 
        throw e;
 
    }
}
