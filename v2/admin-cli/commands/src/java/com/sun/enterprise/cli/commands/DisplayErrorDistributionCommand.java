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
import java.util.HashMap;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


/**
 *  This class is the implementation for display-error-distribution command.
 **/

public class DisplayErrorDistributionCommand extends BaseLoggingCommand
{
    private static final String TARGET_OPTION = "target";

    /**
     *  A method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        if (!validateOptions())
            throw new CommandValidationException("Validation is false");
        
        //use http connector
        MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                                              getUser(), getPassword());
        String instanceName = getOption(TARGET_OPTION);
        verifyTargetInstance(mbsc, instanceName);
        final String objectName = getObjectName();
        final String operationName = getOperationName();
        final long timeStamp = Long.valueOf((String) getOperands().get(0)).longValue(); 
        
        try
        {
            // get the modules with SEVERE count
            Object[] params = new Object[]{timeStamp, "SEVERE"};
            String[] types = getTypesInfo();
            Map returnMap = (Map) mbsc.invoke(new ObjectName(objectName), 
                                                operationName, params, types);
            Map errorsMap = handleErrorDistribution(returnMap, 
                                                        new HashMap(), true);
            
            // get the modules with WARNING count
            params = new Object[]{timeStamp, "WARNING"};
            returnMap = (Map) mbsc.invoke(new ObjectName(objectName), 
                                                operationName, params, types);
            errorsMap = handleErrorDistribution(returnMap, errorsMap, false);

            //Display the error distribution
            displayErrorDistribution(errorsMap);
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }

        CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                   "CommandSuccessful",
                                                   new Object[] {name}));
    }


    private Map handleErrorDistribution(Map errorMap, Map resultMap, boolean isSevere) 
    {
        java.util.Set<String> keySet = errorMap.keySet();
        for (String module : keySet) {
            Integer count = (Integer) errorMap.get(module);
            if (count > 0)
            {
                if (isSevere)
                {
                    int[] errorCounts = new int[]{count,0};
                    resultMap.put(module, errorCounts);
                }
                else
                {
                    if (resultMap.containsKey(module))
                    {
                        int[] errorCounts = (int[])resultMap.get(module);
                        errorCounts[1] = count;
                        resultMap.put(module, errorCounts);
                    }
                    else
                    {
                        int[] errorCounts = new int[]{0, count};
                        resultMap.put(module, errorCounts);
                    }
                }
            }
        }
        return resultMap;
    }

    
    private void displayErrorDistribution(Map errorsMap)
    {
        if (errorsMap.size() > 0)
        {
            //display header
            String sTitle = String.format("%1$-9s %2$-14s %3$-40s",
                                          getLocalizedString("Severity"),
                                          getLocalizedString("Warning"),
                                          getLocalizedString("ModuleID"));
           
            CLILogger.getInstance().printDetailMessage(sTitle);
           
            CLILogger.getInstance().printDetailMessage("---------------------------------------------------------");
            
        }
        else
        {
            CLILogger.getInstance().printMessage(getLocalizedString("NoElementsToList"));
            return;
        }
        java.util.Set<String> keySet = errorsMap.keySet();
        for (String module : keySet) 
        {
            int[] errorCounts = (int[]) errorsMap.get(module);
            final String sErrorRecord = String.format("    %1$-8s %2$-8s %3$-40s",
                                                  errorCounts[0], errorCounts[1], module);
            CLILogger.getInstance().printMessage(sErrorRecord);
        }
    }
}
