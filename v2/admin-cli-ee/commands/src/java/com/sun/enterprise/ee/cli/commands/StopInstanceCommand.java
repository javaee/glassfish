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
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;

import java.util.Vector;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *  This is a modified stop instance command to accommodate --force option
 *  @version  $Revision: 1.2 $
 */
public class StopInstanceCommand extends S1ASCommand {

    public final static String KILL = "kill";    

    public boolean validateOptions() throws CommandValidationException {
    	return super.validateOptions();
    }
   
    public void runCommand() 
    throws CommandException, CommandValidationException {
    
        if (!validateOptions())
            throw new CommandValidationException("Validation is false");
            //use http connector 
        MBeanServerConnection mbsc = getMBeanServerConnection(
            getHost(), getPort(), getUser(), getPassword());
        final String objectName = getObjectName();
        Object[] params = getParamsInfo();
        final String operationName = getOperationName();
        String[] types = getTypesInfo();
        /*if (getCLOption(KILL) != null)  {
            Object[] newparams = new Object[params.length+1];
            newparams[params.length] = getIntegerOption(KILL);
            System.arraycopy(params, 0, newparams, 0, params.length);
            params = newparams;
            String[] newtypes = new String[types.length+1];
            newtypes[types.length] = "int";            
            System.arraycopy(types, 0, newtypes, 0, types.length);
            types = newtypes;
        }*/

        try
        { 
            Object returnValue = mbsc.invoke(new ObjectName(objectName), 
					     operationName, params, types);
            handleReturnValue(returnValue);
	    CLILogger.getInstance().printDetailMessage(getLocalizedString(
                    "CommandSuccessful", new Object[] {name}));
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }        
    }


    private void printDebug(MBeanServerConnection mbsc, String objectName)
	throws Exception
    {
        CLILogger.getInstance().printDebugMessage("********** queryNames **********");
        CLILogger.getInstance().printDebugMessage("LIST OF ALL REGISTERED MBEANS:");
        final java.util.Set allMBeans = mbsc.queryNames( null, null);
        int jj=1;
        for (java.util.Iterator ii = allMBeans.iterator(); ii.hasNext(); ) {
            ObjectName name = (ObjectName) ii.next();
            CLILogger.getInstance().printDebugMessage("("+ jj++ + ")  " + name.toString());
        }


        CLILogger.getInstance().printDebugMessage("********** getMBeanInfo **********");
        final javax.management.MBeanInfo mbinfo = mbsc.getMBeanInfo(new ObjectName(objectName));
        CLILogger.getInstance().printDebugMessage("Description = " + mbinfo.getDescription());
        CLILogger.getInstance().printDebugMessage("Classname = " + mbinfo.getClassName());
        final javax.management.MBeanOperationInfo[] mboinfo = mbinfo.getOperations();
        for (int ii=0; ii<mboinfo.length; ii++) 
        {
            CLILogger.getInstance().printDebugMessage("("+ii+") Description = " + 
                                                      mboinfo[ii].getDescription());
            CLILogger.getInstance().printDebugMessage("("+ii+") Name = " + 
                                                      mboinfo[ii].getName());
        }

    }

}
