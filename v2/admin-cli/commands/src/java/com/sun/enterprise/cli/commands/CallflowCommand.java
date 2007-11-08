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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;
import javax.management.MBeanServerConnection;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.monitor.CallFlowMonitor;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.j2ee.J2EEServer;
import java.util.Map;


/**
 *  This class is the implementation for start-callflow-monitoring and
 *  start-callflow-monitoring command.
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 *  @version $Revision: 1.6 $
 **/

public class CallflowCommand extends S1ASCommand
{
    private static final String FILTER_TYPE_OPTION = "filtertype";
    private static final String CALLER_IPFILTER = "CallerIPFilter";
    private static final String CALLER_PRINCIPALFILTER = "CallerPrincipalFilter";
    private static final String ENABLED = "Enabled";
    private static final String FILTER_TYPE_USER = "user";
    private static final String FILTER_TYPE_IP = "ip";        
    private static final String START_CALL_FLOW= "start-callflow-monitoring";
    private String sFilterIP = null;
    private String sFilterUserID = null;

    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        if (!validateOptions())
            throw new CommandValidationException("Validation is false");
        
        //use http connector
        MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                                              getUser(), getPassword());
        final String instanceName = (String) getOperands().get(0);
        
        //Set the Enabled Attribute
        try
        {
            verifyTargetInstance(mbsc, instanceName);
            CallFlowMonitor cfm = getCallFlowMonitor(mbsc, instanceName);
            setCallFlowConfig(cfm);
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }

        CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                   "CommandSuccessful",
                                                   new Object[] {name}));
    }


    /**
     *  This method validates filtertype.
     *  There are currently two filtertype.  They are user and ip.
     *  If type user, the attribute to set is CALLER_PRINCIPALFILTER.
     *  If type ip, the attribute to set is CALLER_IPFILTER.
     *  If type other than user or ip then throw a CommandValidationException
     *  If type is not in the correct format, then an exception is thrown.
     *  filterypte should be in the format of name=value with : as the delimiter.
     *  @throws CommandException and CommandValidationException
     **/ 
    private void validateFilterType() throws CommandException, CommandValidationException
    {
        if (getOption(FILTER_TYPE_OPTION) != null) {
            final String filterType = getOption(FILTER_TYPE_OPTION);
            
            final CLITokenizer filterTypeTok = new CLITokenizer(filterType, PROPERTY_DELIMITER);
            while (filterTypeTok.hasMoreTokens()) {
                final String nameAndvalue = filterTypeTok.nextToken();
                final CLITokenizer nameTok = new CLITokenizer(nameAndvalue, PARAM_VALUE_DELIMITER);
                if (nameTok.countTokens() == 2)
                {
                    final String sName = nameTok.nextTokenWithoutEscapeAndQuoteChars();
                    final String sValue = nameTok.nextTokenWithoutEscapeAndQuoteChars();
                    if (sName.equals(FILTER_TYPE_IP))
                        sFilterIP = sValue;
                    else if (sName.equals(FILTER_TYPE_USER))
                        sFilterUserID = sValue;
                    else
                        throw new CommandValidationException(getLocalizedString("InvalidFilterName", new Object[] {sName}));
                } else {
                        throw new CommandValidationException(getLocalizedString("InvalidFilterType", new Object[] {filterType}));
                }
                
            }
        }
    }
    
    
    /**
     *  This method sets the callflow attributes for a given instance
     *  @throws CommandException, CommandValidationException
     **/
    private void setCallFlowConfig(CallFlowMonitor cfm)
         throws CommandException, CommandValidationException
    {
        validateFilterType();
        final Boolean bEnable = Boolean.valueOf(name.equals(START_CALL_FLOW)?true:false);
        
        if (cfm != null){
            cfm.setEnabled(bEnable);
            if (sFilterIP != null)
               cfm.setCallerIPFilter(sFilterIP);
            if (sFilterUserID != null)
                cfm.setCallerPrincipalFilter(sFilterUserID);
        }
    }
    


    private CallFlowMonitor getCallFlowMonitor(MBeanServerConnection mbsc, String instanceName)
    {
        DomainRoot domainRoot = 
                ProxyFactory.getInstance(mbsc).getDomainRoot();
        Map<String,ServerRootMonitor> serverRootMonitorMap = 
                domainRoot.getMonitoringRoot().getServerRootMonitorMap();
        ServerRootMonitor serverRootMonitor = serverRootMonitorMap.get(instanceName);
        //IF the instance is not running or when no monitoring on, just return;
        if (serverRootMonitor == null)
            return null;
        CallFlowMonitor cfm = serverRootMonitor.getCallFlowMonitor();
        return cfm;
    }
    
    private void verifyTargetInstance(MBeanServerConnection mbsc, String instanceName) 
        throws CommandException
    {
        DomainRoot domainRoot = 
                ProxyFactory.getInstance(mbsc).getDomainRoot();
        boolean isServer=false;
        isServer = domainRoot.getDomainConfig().getServerConfigMap().keySet().contains(instanceName);
        if ( ! isServer ) 
        {
            throw new CommandException(getLocalizedString("TargetNotAnInstance", 
                                                 new Object[] {instanceName}));
        }
        else
        {
            final Map<String,J2EEServer> servers =
                domainRoot.getJ2EEDomain().getJ2EEServerMap();
            J2EEServer server = servers.get( instanceName );

            boolean running = server != null &&
                                server.getstate() == server.STATE_RUNNING || 
                                server.getstate() == server.STATE_STARTING;
            if (!running)
                throw new CommandException(getLocalizedString("InstanceNotRunning",
                                                  new Object[] {instanceName}));
        }        
    }
}
