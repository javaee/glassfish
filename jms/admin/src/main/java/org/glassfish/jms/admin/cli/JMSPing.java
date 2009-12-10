/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.AdminCommand;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import org.jvnet.hk2.annotations.Inject;

import javax.resource.ResourceException;
import java.util.Properties;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;


@Service(name="jms-ping")
@Scoped(PerLookup.class)
@I18n("jms-ping")

public class JMSPing implements AdminCommand {
    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(JMSPing.class);

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Inject
    Configs configs;

    @Inject
    Domain domain;

    @Inject
    CommandRunner commandRunner;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

         Server targetServer = domain.getServerNamed(target);
         String configRef = targetServer.getConfigRef();

         JmsService jmsservice = null;
               for (Config c : configs.getConfig()) {

                      if(configRef.equals(c.getName()))
                            jmsservice = c.getJmsService();
                   }
         String defaultJmshostStr = jmsservice.getDefaultJmsHost();
         JmsHost defaultJmsHost = null;
               for (JmsHost jmshost : jmsservice.getJmsHost()) {

                      if(defaultJmshostStr.equals(jmshost.getName()))
                            defaultJmsHost = jmshost;
                   }
         String tmpJMSResource = "test_jms_adapter";
         ActionReport subReport = report.addSubActionsReport();
         createJMSResource(defaultJmsHost, subReport, tmpJMSResource);
         if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())){
                report.setMessage(localStrings.getLocalString("jms-ping.cannotCreateJMSResource",
                         "Unable to create a temporary Connection Factory to the JMS Host"));
               report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
         }
        try{
            boolean value = pingConnectionPool(tmpJMSResource);
            
            if(!value){
                 report.setMessage(localStrings.getLocalString("jms-ping.pingConnectionPoolFailed",
                         "Pinging to the JMS Host failed."));
                 report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }else {
                  report.setMessage(localStrings.getLocalString("jms-ping.pingConnectionPoolSuccess",
                         "JMS-ping command executed successfully"));
                 report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            }
        }catch (ResourceException e)
        {
            report.setMessage(localStrings.getLocalString("jms-ping.pingConnectionPoolException",
                         "An exception occured while trying to ping the JMS Host.", e.getMessage()));
           report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        deleteJMSResource(subReport, tmpJMSResource);
        if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())){
                report.setMessage(localStrings.getLocalString("jms-ping.cannotdeleteJMSResource",
                         "Unable to delete the temporary JMS Resource " + tmpJMSResource + ". Please delete this manually.", tmpJMSResource));
               report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
         }
    }

   void createJMSResource(JmsHost defaultJmsHost, ActionReport subReport, String tmpJMSResource)
   {
        String userName = defaultJmsHost.getAdminUserName();
        String password = defaultJmsHost.getAdminPassword();
        String host = defaultJmsHost.getHost();
        String port = defaultJmsHost.getPort();

        ParameterMap aoAttrList = new ParameterMap();

        Properties properties = new Properties();
        properties.put("imqDefaultUsername",userName);
         if (isPasswordAlias(password)){
                       //If the string is a password alias, it needs to be escapted with another pair of quotes...
                       properties.put("imqDefaultPassword", "\"" + password + "\"");
         }else
             properties.put("imqDefaultPassword",password);

       //need to escape the addresslist property so that they get passed on correctly to the create-connector-connection-pool command
        properties.put("AddressList", "\"mq://"+host + ":"+ port +"\"");

        String propString = "";
        for (java.util.Map.Entry<Object, Object>prop : properties.entrySet()) {
                propString += prop.getKey() + "=" + prop.getValue() + ":";
        }
        propString = propString.substring(0, propString.length());
        aoAttrList.set("property", propString);

        aoAttrList.set("restype",  "javax.jms.QueueConnectionFactory");
        aoAttrList.set("DEFAULT",  tmpJMSResource);

        commandRunner.getCommandInvocation("create-jms-resource", subReport).parameters(aoAttrList).execute();

    }
    private boolean isPasswordAlias(String password){
        if (password != null && password.contains("${ALIAS"))
            return true;

        return false;
    }

    boolean pingConnectionPool(String tmpJMSResource) throws ResourceException
    {
        return connectorRuntime.pingConnectionPool(tmpJMSResource);
    }
    
    void deleteJMSResource(ActionReport subReport, String tmpJMSResource)
    {
        ParameterMap aoAttrList = new ParameterMap();
        aoAttrList.set("DEFAULT",  tmpJMSResource);

        commandRunner.getCommandInvocation("delete-jms-resource", subReport).parameters(aoAttrList).execute();
    }
}
