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
 * BaseLoggingCommand.java
 *
 * Created on August 1, 2006, 11:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.j2ee.J2EEServer;
import java.util.Map;

/**
 *
 * @author prashanth
 */
abstract public class BaseLoggingCommand extends S1ASCommand
{
    private static final String TARGET_OPTION = "target";

    /**
     *  method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();
        try        
        {
            //use http connector
            final MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(),
                                                                  getUser(), getPassword());
            final String instanceName = getOption(TARGET_OPTION);
            verifyTargetInstance(mbsc, instanceName);
            final String objectName = getObjectName();
            final Object[] params = getParamsInfo();
            final String operationName = getOperationName();
            final String[] types = getTypesInfo();

            //if (System.getProperty("Debug") != null) printDebug(mbsc, objectName);
            Object returnValue = mbsc.invoke(new ObjectName(objectName),
                                             operationName, params, types);
            handleReturnValue(returnValue);
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }
    }

    
    protected void verifyTargetInstance(MBeanServerConnection mbsc, String instanceName) 
        throws CommandException
    {
        try 
        {
            final DomainRoot domainRoot = 
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
                final J2EEServer server = servers.get( instanceName );

                final boolean running = server != null &&
                                    server.getstate() == server.STATE_RUNNING || 
                                    server.getstate() == server.STATE_STARTING;
                if (!running)
                    throw new CommandException(getLocalizedString("InstanceNotRunning",
                                                      new Object[] {instanceName}));
            }        
        }
        catch (Exception e)
        {
            throw new CommandException(e);
        }
    }
}
