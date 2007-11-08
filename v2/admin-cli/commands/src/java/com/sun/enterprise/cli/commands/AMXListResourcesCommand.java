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
import javax.management.ObjectName;

import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.base.Util;
import javax.management.MalformedObjectNameException;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.base.XTypes;
import java.lang.NullPointerException;
import javax.management.RuntimeOperationsException;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.lang.IllegalArgumentException;
import java.io.File;
import java.io.IOException;



/**
 *  @version  $Revision: 1.3 $
 */
public class AMXListResourcesCommand extends S1ASCommand
{
    public final static String DOMAIN_CONFIG_OBJECT_NAME = "amx:j2eeType=X-DomainConfig,name=na";
    public final static String SERVER_CONFIG_OBJECT_NAME = "amx:j2eeType=X-StandaloneServerConfig,name=";
    public final static String CLUSTER_CONFIG_OBJECT_NAME = "amx:j2eeType=X-ClusterConfig,name=";
    public final static String TARGET_NAME = "target";

    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of 
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
    	return super.validateOptions();
    }
   
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
        final Vector vTargetName = getOperands();
        final String targetName = (vTargetName.size()>0)?(String)vTargetName.get(0):null;
        
            //if targetName is not null, then try to get the Config ObjectName of the
            //target.
        ObjectName targetON = (targetName!=null && !targetName.equals(DOMAIN))?
                              getTargetConfigObjectName(mbsc, targetName):null;
        
        final Object[] params = getParamsInfo();
        final String operationName = getOperationName();
        final String[] types = getTypesInfo();
        final String j2eeType = (String) ((Vector) getProperty(PARAMS)).get(0);

        try {
            Object resources = mbsc.invoke(Util.newObjectName(DOMAIN_CONFIG_OBJECT_NAME),
                                           operationName,
                                           params, types);

            Map candidates = (Map)resources;

            if (targetON != null ) {
                candidates = getResourcesFromTarget(mbsc, targetON, candidates);
            }
            displayMap("", (Map)candidates);
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
        }
        catch (Exception e) {
            displayExceptionMessage(e);
        }
        

    }


        /**
         * Given the Server/Cluster ObjectName, get all the referenced resources.
         * Then loop through the given  resources in the DomainConfig to
         * determine the referenced resources in the ObjectName.
         * @param mbsc
         * @param targetON
         * @return Map of the referenced resources
         */
    private Map getResourcesFromTarget(MBeanServerConnection mbsc,
                                      ObjectName targetON,
                                      Map candidates)
        throws Exception
    {
        Object resourceRefs = mbsc.invoke(targetON,
                                          "getContaineeObjectNameMap",
                                          new Object[]{new String(XTypes.RESOURCE_REF_CONFIG)},
                                          new String[]{"java.lang.String"});

        final Set resourceKeySet = ((Map)resourceRefs).keySet();
        final Iterator resourceKeyIter = resourceKeySet.iterator();
        Map resMap = new HashMap();
        while (resourceKeyIter.hasNext()) {
            final String valueName = (String)resourceKeyIter.next();
            CLILogger.getInstance().printDebugMessage("Candidate = " + valueName );
            if (candidates.containsKey(valueName)) {
                resMap.put(valueName, candidates.get(valueName));
            }
        }
        return resMap;
    }
    

        /**
         * This routine will display the exception message if the option
         * --terse is given.  This routine will get the root of the exception
         * and display the message.  It will then wrap it with CommaneException
         * and throw the exception to be handled by CLI framework.
         * @param e
         * @throws CommandException
         */
    public void displayExceptionMessage(Exception e) throws CommandException
    {
            //get the root cause of the exception
        Throwable rootException = ExceptionUtil.getRootCause(e);
        
        if (rootException.getLocalizedMessage() != null)
            CLILogger.getInstance().printDetailMessage(rootException.getLocalizedMessage());
        throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                      new Object[] {name} ), e);

    }


        /**
         *  This routine will get the StandaloneServerConfig or ClusterConfig
         *  by the given target name.
         *  @param MBeanServerConnection
         *  @param targetName
         *  @return ObjectName
         */
    private ObjectName getTargetConfigObjectName(final MBeanServerConnection mbsc,
                                                 final String targetName)
        throws CommandException
    {
        try {
            ObjectName scON = Util.newObjectName(SERVER_CONFIG_OBJECT_NAME+targetName);
            if (!mbsc.isRegistered(scON))
                scON = Util.newObjectName(CLUSTER_CONFIG_OBJECT_NAME+targetName);
            if (!mbsc.isRegistered(scON))
                throw new CommandException(getLocalizedString("InvalidTargetName"));
        
            return scON;            
        }
        catch (RuntimeOperationsException roe)
        {
            throw new CommandException(roe);
        }
        catch (IOException ioe)
        {
            throw new CommandException(ioe);
        }
    }

        
    /**
        Display a Map to System.out.
     */
    private void displayMap(final String msg, final Map m)
    {
        final Set keySet = m.keySet();
        if (keySet.isEmpty()) {
            CLILogger.getInstance().printDetailMessage(
                getLocalizedString("NoElementsToList"));
            return;
        }
        final Iterator keyIter = keySet.iterator();
        
        while (keyIter.hasNext()) {
            final String valueName = (String)keyIter.next();
            CLILogger.getInstance().printMessage(msg + " " + valueName );
        }
    }

}
