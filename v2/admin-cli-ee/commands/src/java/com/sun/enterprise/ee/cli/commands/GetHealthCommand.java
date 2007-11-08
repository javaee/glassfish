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

package com.sun.enterprise.ee.cli.commands;

import com.sun.enterprise.cli.commands.GenericCommand;
import com.sun.enterprise.cli.framework.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import java.util.Date;
import com.sun.enterprise.util.i18n.StringManager;


public class GetHealthCommand extends GenericCommand {

    private static final String HEARTBEAT_ENABLED = "heartbeat-enabled";
    private static final StringManager _strMgr =
        StringManager.getManager(GetHealthCommand.class);

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();

        try {
            MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                                                  getUser(), getPassword());
            String clusterName = (String) getOperands().get(0);
            verifyTargetCluster(mbsc, clusterName);

            final String objectName = getObjectName();
            final Object[] params = getParamsInfo();
            final String operationName = getOperationName();
            final String[] types = getTypesInfo();
            boolean gmsEnabled = true;

            if (!(gmsEnabled = isGMSEnabled(mbsc, clusterName)))
            {
                  CLILogger.getInstance().printMessage(_strMgr.getString("GMSNotEnabled"));
            }
            Object returnValue = mbsc.invoke(new ObjectName(objectName), 
                                             operationName, params, types);
            displayClusterHealth((Map) returnValue, gmsEnabled);
            
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                           "CommandSuccessful",
                                                           new Object[] {name}));
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }        
    }

    
    /*
     * Formulate and Returns the attributes from the given string
     */
    private boolean isGMSEnabled(final MBeanServerConnection mbsc, final String clusterName) throws Exception
    {
        ObjectName objName = new ObjectName("com.sun.appserv:type=cluster,name=" 
                                            + clusterName + ",category=config");
        String isGmsEnabledObj = (String) mbsc.getAttribute(objName, HEARTBEAT_ENABLED);
        return (Boolean.valueOf(isGmsEnabledObj)).booleanValue();
    }
    
    
    /*
     * Formulate and Returns the attributes from the given string
     */
    private void displayClusterHealth(Map healthMap, boolean gmsEnabled) throws Exception
    {
        for (Object instanceName : healthMap.keySet())
        {
            //String instance = e.getKey();
            final List<Long> instanceHealth = (List) healthMap.get(instanceName);
            final long health = instanceHealth.get(0);
                //health is in the range of 0-3
            if (health < 4 && health >-1) {
                String timestamp = null;
                if ((instanceHealth.size() == 2) && (instanceHealth.get(1) != -1))
                    timestamp = new Date(instanceHealth.get(1)).toLocaleString();
                String gmsStr = (gmsEnabled)?"GMS":"";
                CLILogger.getInstance().printMessage(
                                    _strMgr.getString(gmsStr+"InstanceHealth-"+health, 
                                                      new Object[] {instanceName, timestamp}));
            }
            else {
                CLILogger.getInstance().printMessage(_strMgr.getString("InstanceHealthUnKnown",
                                                     new Object[] {instanceName}));
            }
        }
    }

    
    private void verifyTargetCluster(MBeanServerConnection mbsc, String clusterName) 
        throws CommandException
    {
        DomainRoot domainRoot = 
                ProxyFactory.getInstance(mbsc).getDomainRoot();
        boolean isCluster=false;
        isCluster = domainRoot.getDomainConfig().getClusterConfigMap().keySet().contains( clusterName );
        if ( ! isCluster ) 
        {
            throw new CommandException(_strMgr.getString("TargetNotACluster", 
                                                 new Object[] {clusterName}));
        }
    }
}
