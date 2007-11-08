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
import java.util.Map;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.Iterator;
import javax.management.Attribute;
import javax.management.AttributeList;

public class DisplayLogRecordsCommand extends BaseLoggingCommand 
{
    
    private final static String TIMESTAMP_OPTION = "timestamp";
    private final static String MODULEID_OPTION = "moduleid";
    private final static String LEVEL_OPTION = "errorlevel";
    private final static String MODULE_DELIMITER = ":";

    protected Object[] getParamsInfo()throws CommandException, CommandValidationException
    {
        Object[] paramsInfo = new Object[11];
        long timestamp = Long.valueOf(getOption(TIMESTAMP_OPTION)).longValue(); 
        String errorLevel = getOption(LEVEL_OPTION);
        
        paramsInfo[0] = null;
        paramsInfo[1] = null;
        paramsInfo[2] = Boolean.FALSE;
        paramsInfo[3] = Boolean.FALSE;
        paramsInfo[4] = Integer.valueOf(1000);
        paramsInfo[5] = new Date(timestamp);
        paramsInfo[6] = new Date(timestamp + 3600000);
        paramsInfo[7] = (errorLevel == null) ? Level.WARNING.toString() : errorLevel;
        paramsInfo[8] = (errorLevel != null) ? Boolean.TRUE : Boolean.FALSE;
        paramsInfo[9] = getModuleIds();
        paramsInfo[10] = null;
        for (Object param : paramsInfo)
        {
            if (param != null)
                CLILogger.getInstance().printDebugMessage(param.toString());
            else
                CLILogger.getInstance().printDebugMessage("null");
        }
        return paramsInfo;
    }

    
    protected void handleReturnValue(Object retVal) 
    {
        //final Map errorDistribution = (Map)retVal;
        final AttributeList results  = (AttributeList) retVal;
        List headerRow = (List)(((Attribute)results.get(0)).getValue());
        List rowList = (List)(((Attribute)results.get(1)).getValue());
        List row;

        Iterator it = rowList.iterator();
        if (it.hasNext())
        {
            CLILogger.getInstance().printDetailMessage(
                    "----------------------------------------------------------------------");
            
        }
        else
        {
            CLILogger.getInstance().printDetailMessage(
                                        getLocalizedString("NoElementsToList"));
        }
        while (it.hasNext())
        {
            row = (List)it.next();
	    if (row.size() != headerRow.size()) {
                //throw new CommandException(
                CLILogger.getInstance().printDebugMessage(
		"Row had '"+row.size()+"' columns, header has '"+
		    headerRow.size()+"' columns!");
	    }
            
            CLILogger.getInstance().printMessage(getLocalizedString("LogRecNumber") 
                                                    + " = " + row.get(0));
            CLILogger.getInstance().printMessage(getLocalizedString("LogDateTime") 
                                                    + " = " + row.get(1));
            CLILogger.getInstance().printMessage(getLocalizedString("LogMsgId") 
                                                    + " = " + row.get(6));
            CLILogger.getInstance().printMessage(getLocalizedString("LogLevel") 
                                                    + " = " + row.get(2));
            CLILogger.getInstance().printMessage(getLocalizedString("LogProductName") 
                                                    + " = " + row.get(3));
            CLILogger.getInstance().printMessage(getLocalizedString("LogLogger") 
                                                    + " = " + row.get(4));
            CLILogger.getInstance().printMessage(getLocalizedString("Lognvp") 
                                                    + " = " + row.get(5));
            CLILogger.getInstance().printMessage(getLocalizedString("LogMessage") 
                                                    + " = " + row.get(7));
            CLILogger.getInstance().printDetailMessage(
                    "----------------------------------------------------------------------");
        }
    }

    
    /**
     * Formulate and Returns module-id's from the given string
     * @return Properties
     */
    protected List getModuleIds()
        throws CommandException, CommandValidationException
    {
        final List moduleIds = new ArrayList();

        String modulesStr = (String) getOperands().get(0);
        final CLITokenizer modulesTok = new CLITokenizer(modulesStr, MODULE_DELIMITER);
        while (modulesTok.hasMoreTokens()) {
            moduleIds.add(modulesTok.nextToken());
        }
        return moduleIds;
    }
}
