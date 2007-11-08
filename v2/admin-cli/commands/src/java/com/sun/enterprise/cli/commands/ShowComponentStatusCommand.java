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
 *  This class is called for the show-comonent-status command.
 *  It uses the application mbean (object name: ias:type=applications,category=config)
 *  and the getStatus operation.  This information is defined in
 *  CLIDescriptor.xml,
 *  <p>
 *  The getStatus returns true if component is enabled else returns false.
 *  </p>
 *  @version  $Revision: 1.3 $
 */
public class ShowComponentStatusCommand extends S1ASCommand
{
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
        validateOptions();
        String objectName = getObjectName();
        Object[] params = getParamsInfo();
        String operationName = getOperationName();
        String[] types = getTypesInfo();

	//use http connector
	MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                     getUser(), getPassword());

        try
        { 
            final Object returnValue = mbsc.invoke(new ObjectName(objectName), 
					     operationName, params, types);
            displayComponentStatus(returnValue);
	    CLILogger.getInstance().printDetailMessage(getLocalizedString(
						       "CommandSuccessful",
						       new Object[] {name}));
        }
        catch(Exception e)
        { 
	    if (e.getLocalizedMessage() != null)
		CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
						     new Object[] {name} ), e);
        }        
    }


    /**
     *  display the status of the component.
     *  if the returnValue is true, then the component is enabled, else disabled.
     *  @throws CommandException is could not determine the status of the component.
     */
    private void displayComponentStatus(Object returnValue)
	throws CommandException
    {
	final String componentName = (String) getOperands().get(0);
	if (returnValue == null)
	    throw new CommandException(getLocalizedString("UndetermineStatus",
							  new Object[] {componentName}));
	else 
	{
	    //the return value must be Boolean
	    final String returnValClassName = returnValue.getClass().getName();
	    if (returnValClassName.equals(BOOLEAN_CLASS))
	    {
		final boolean status = ((Boolean)returnValue).booleanValue();
		if (status)
		    CLILogger.getInstance().printMessage(getLocalizedString(
			     "ComponentIsEnabled", new Object[] {componentName}));
		else
		    CLILogger.getInstance().printMessage(getLocalizedString(
			     "ComponentIsDisabled", new Object[] {componentName}));
	    }
	    else
		throw new CommandException(getLocalizedString("UndetermineStatus",
							      new Object[] {componentName}));
	}
    }

}
