/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.jms.admin.cli;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.*;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.RuntimeType;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Cluster;

import java.util.Properties;
import java.util.Map;
import java.beans.PropertyVetoException;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import java.beans.PropertyVetoException;


/**
 * Configure JMS Cluster Command
 *
 */
@Service(name="configure-jms-cluster")
@Scoped(PerLookup.class)
@I18n("configure.jms.cluster")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
//@TargetType({CommandTarget.CLUSTER})

public class ConfigureJMSCluster implements AdminCommand {
    final private static String SUPPORTED_DB_VENDORS = "oracle|postgressql|mysql|derby|db2";
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ConfigureJMSCluster.class);
    // [usemasterbroker] [availability-enabled] [dbvendor] [dbuser] [dbpassword admin] [jdbcurl] [properties props] clusterName

    @Param(name="usemasterbroker", alias="mb", defaultValue="true")
    Boolean useMasterBroker;

    @Param(name="availability-enabled", alias="ae", defaultValue="false")
    Boolean availabilityEnabled;

    @Param(name="dbvendor", alias="db", optional=true)
    String dbvendor;

    @Param(name="dbuser", alias="user", optional=true)
    String dbuser;

    @Param(name="dbpassword", alias="password", optional=true)
    String dbpassword;

    @Param(name="jdbcurl", alias="url", optional=true)
    String jdbcurl;

    @Param(name="property", optional=true, separator=':')
    Properties props;

    @Param(primary=true)
    String clusterName;

    @Inject
    Domain domain;

    Config config;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
       // Server targetServer = domain.getServerNamed(target);
        //String configRef = targetServer.getConfigRef();

        Cluster cluster =domain.getClusterNamed(clusterName);

        if (cluster == null) {
            report.setMessage(localStrings.getLocalString("configure.jms.cluster.invalidClusterName",
                            "No Cluster by this name has been configured"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if(Boolean.valueOf(useMasterBroker) == false || Boolean.valueOf(availabilityEnabled) == true){
          if (dbvendor == null) {
            report.setMessage(localStrings.getLocalString("configure.jms.cluster.nodbvendor",
                            "No DataBase vendor specified"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
         }
         else if (jdbcurl == null) {
            report.setMessage(localStrings.getLocalString("configure.jms.cluster.nojdbcurl",
                            "No JDBC URL specified"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
         }
         else if (! isSupportedDbVendor()){
             report.setMessage(localStrings.getLocalString("configure.jms.cluster.invaliddbvendor",
                            "Invalid DB Vednor specified"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
         }
        }

        if(availabilityEnabled)
        {
            if(useMasterBroker)
                report.setMessage(localStrings.getLocalString("configure.jms.cluster.useMaterBroker",
                                "Configuring an Enhanced MQ cluster. Ignoring the usemasterbroker option ..."));

        }

        config = domain.getConfigNamed(cluster.getConfigRef());

        JmsAvailability jmsAvailability = config.getAvailabilityService().getJmsAvailability();
        JmsService jmsservice = config.getJmsService();

        try {
            ConfigSupport.apply(new SingleConfigCode<JmsAvailability>() {
                public Object run(JmsAvailability param) throws PropertyVetoException, TransactionFailure {

                    param.setAvailabilityEnabled(availabilityEnabled.toString());
                    param.setDbVendor(dbvendor);
                    param.setDbUsername(dbuser);
                    param.setDbPassword(dbpassword);
                    param.setJdbcUrl(jdbcurl);

                    if(props != null)
                    {
                       for (Map.Entry e: props.entrySet()){
                        Property prop = param.createChild(Property.class);
                        prop.setName((String)e.getKey());
                        prop.setValue((String)e.getValue());
                        param.getProperty().add(prop);
                        }
                    }
                    return param;
                  }
               }, jmsAvailability);

            //update the useMasterBroker flag on the JmsService only if availabiltyEnabled is false
            if(!availabilityEnabled && useMasterBroker != null){
              ConfigSupport.apply(new SingleConfigCode<JmsService>() {
                public Object run(JmsService param) throws PropertyVetoException, TransactionFailure {

                    param.setUseMasterBroker(useMasterBroker.toString());
                    return param;
                }
            }, jmsservice);
            }

        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("configure.jms.cluster.fail",
                            "Unable to Configure JMS Cluster for cluster {0}.", clusterName) +
                            " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
        report.setMessage(localStrings.getLocalString("configure.jms.cluster.success",
                "JMS Cluster Configuration updated for Cluster {0}.", clusterName));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private boolean isSupportedDbVendor(){
        if (dbvendor != null)
        {
            return SUPPORTED_DB_VENDORS.contains(dbvendor.toLowerCase());
        }
        return false;
    }
}
