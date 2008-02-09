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
package com.sun.enterprise.v3.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

/**
 * Create JDBC Connection Pool Command
 * 
 */
@Service(name="create-jdbc-connection-pool")
@Scoped(PerLookup.class)
@I18n("create.jdbc.connection.pool")
public class CreateJdbcConnectionPool implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJdbcConnectionPool.class);    

    @Param(name="datasourceclassname")
    String datasourceclassname;

    @Param(name="restype", optional=true)
    String restype;

    @Param(name="steadypoolsize", optional=true)
    String steadypoolsize = "8";

    @Param(name="maxpoolsize", optional=true)
    String maxpoolsize = "32";
    
    @Param(name="maxwait", optional=true)
    String maxwait = "60000";

    @Param(name="poolresize", optional=true)
    String poolresize = "2";
    
    @Param(name="idletimeout", optional=true)
    String idletimeout = "300";
        
    @Param(name="isolationlevel", optional=true)
    String isolationlevel;
            
    @Param(name="isisolationguaranteed", optional=true)
    String isisolationguaranteed = Boolean.TRUE.toString();
                
    @Param(name="isconnectvalidatereq", optional=true)
    String isconnectvalidatereq = Boolean.FALSE.toString();
    
    @Param(name="validationmethod", optional=true)
    String validationmethod = "auto-commit";
    
    @Param(name="validationtable", optional=true)
    String validationtable;
    
    @Param(name="failconnection", optional=true)
    String failconnection = Boolean.FALSE.toString();
    
    @Param(name="allownoncomponentcallers", optional=true)
    String allownoncomponentcallers = Boolean.FALSE.toString();
    
    @Param(name="nontransactionalconnections", optional=true)
    String nontransactionalconnections = Boolean.FALSE.toString();
    
    @Param(name="description", optional=true)
    String description;
    
    //@Param(name="property", optional=true)
    
    @Param(name="jdbc_connection_pool_id", primary=true)
    String jdbc_connection_pool_id; 

    @Inject
    Resources resources;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        
        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof JdbcConnectionPool) {
                if (((JdbcConnectionPool) resource).getName().equals(jdbc_connection_pool_id)) {
                    report.setMessage(localStrings.getLocalString("create.jdbc.connection.pool.duplicate",
                            "A JDBC connection pool named {0} already exists.", jdbc_connection_pool_id));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;                    
                }
            }
        }
        
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    JdbcConnectionPool newResource = ConfigSupport.createChildOf(param, JdbcConnectionPool.class);
                    newResource.setAllowNonComponentCallers(allownoncomponentcallers);
                    //newResource.setAssociateWithThread(restype);
                    //newResource.setConnectionCreationRetryAttempts(restype);
                    //newResource.setConnectionCreationRetryIntervalInSeconds(restype);
                    //newResource.setConnectionLeakReclaim(restype);
                    //newResource.setConnectionLeakTimeoutInSeconds(restype);
                    //newResource.setConnectionValidationMethod(restype);
                    newResource.setDatasourceClassname(datasourceclassname);
                    newResource.setDescription(description);
                    newResource.setFailAllConnections(failconnection);
                    newResource.setIdleTimeoutInSeconds(idletimeout);
                    newResource.setIsConnectionValidationRequired(isconnectvalidatereq);
                    newResource.setIsIsolationLevelGuaranteed(isisolationguaranteed);
                    //newResource.setLazyConnectionAssociation(restype);
                    //newResource.setLazyConnectionEnlistment(restype);
                    //newResource.setMatchConnections(restype);
                    //newResource.setMaxConnectionUsageCount(restype);
                    newResource.setMaxPoolSize(maxpoolsize);
                    newResource.setMaxWaitTimeInMillis(maxwait);
                    newResource.setName(jdbc_connection_pool_id);
                    newResource.setNonTransactionalConnections(nontransactionalconnections);    
                    newResource.setPoolResizeQuantity(poolresize);
                    newResource.setResType(restype);
                    //newResource.setStatementTimeoutInSeconds(restype);
                    newResource.setSteadyPoolSize(steadypoolsize);
                    newResource.setTransactionIsolationLevel(isolationlevel);
                    newResource.setValidateAtmostOncePeriodInSeconds(restype);
                    newResource.setValidationTableName(validationtable);
                    //newResource.setWrapJdbcObjects(restype);
                    param.getResources().add(newResource);                    
                    return newResource;
                }
            }, resources);

        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("create.jdbc.connection.pool.fail", "{0} create failed ", jdbc_connection_pool_id));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
        report.setMessage(localStrings.getLocalString("create.jdbc.connection.pool.success", "{0} created successfully", jdbc_connection_pool_id));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
