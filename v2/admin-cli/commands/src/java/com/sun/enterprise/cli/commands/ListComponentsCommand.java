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
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;


import java.util.Vector;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Map;

/**
 *  This class is called for the list-components command.
 *  It uses the application mbean (object name: ias:type=applications,category=config)
 *  and the getAllDeployedComponents() operation.  This information is defined in
 *  CLIDescriptor.xml,
 *  <p>
 *  The getAllDeployedComponents returns an array of ObjectName[].  From the ObjectName,
 *  the type can be determined.  If the --type option is used, the appropriate type 
 *  will be displayed otherwise all deployed module will be displayed.
 *  </p>
 *  @version  $Revision: 1.4 $
 */
public class ListComponentsCommand extends S1ASCommand
{
    private static final String TYPE_OPTION = "type";
    private static final String TYPE_OPTION_REG = "application|web|ejb|connector|webservice";
    private static final String WEBSERVICE_TYPE = "webservice";

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
	//if validateOptions is false, then it must  be that --help option 
	//is provided and there is no need to execute the command since 
	//either manpage or usage text is displayed
	if (!validateOptions())
	    return;
    
        String objectName = getObjectName();
        Object[] params = getParamsInfo();
        String operationName = getOperationName();
        String[] types = getTypesInfo();

	//use http connector
	MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                     getUser(), getPassword());

        try
        { 
	    if (System.getProperty("Debug") != null) printDebug(mbsc, objectName);
	    // handleReturnValue(returnValue);
            String type = getTypeOption();
            boolean nothingToList = true;
            if ((type != null) && (type.equals(WEBSERVICE_TYPE)))
            {
                nothingToList = printWebServices(mbsc);
            }
            else
            {
                Object returnValue = mbsc.invoke(new ObjectName(objectName), 
                                                 operationName, params, types);
                nothingToList = printDeployedComponents(returnValue);
                // if type is null, also print the webservices
                if (type == null)
                {
                    if (printWebServices(mbsc) == false) 
                        nothingToList=false;
                }
            }
            if (nothingToList)
            {
                        CLILogger.getInstance().printDetailMessage(
                                                    getLocalizedString("NoElementsToList"));
            }
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
     *  This method prints the object name.
     *  @param return value from operation in mbean
     */
    private boolean printDeployedComponents(Object returnValue)
	throws CommandValidationException
    {
	if (returnValue == null) return true;
        boolean nothingToList = true;
        if (returnValue.getClass() == new ObjectName[0].getClass())
        {
            final ObjectName[] objs = (ObjectName[])returnValue;
            final String displayType = (String)((Vector)getProperty(DISPLAY_TYPE)).get(0);
	    final String type = getTypeOption();
            for (int ii=0; ii<objs.length; ii++)
            {
		final ObjectName objectName = (ObjectName)objs[ii];
		CLILogger.getInstance().printDebugMessage("ObjectName = " + objectName);
		final String componentType = objectName.getKeyProperty("type");

		if ((type == null) || (componentType.indexOf(type) != -1)) 
		{
		    CLILogger.getInstance().printMessage(
					    objectName.getKeyProperty(displayType) + 
					    " <" + componentType + "> " );
                    nothingToList = false;
		}
            }
        }
        return nothingToList;
    }


    /**
     * this method will print the fully qualified webservice endpoints 
     * ex. jaxrpc-simple#jaxrpc-simple.war#HelloIF <webservice>
     * @return nothingToLis
     */
    private boolean printWebServices(MBeanServerConnection mbsc)
    {
        DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
        Map map = domainRoot.getWebServiceMgr().getWebServiceEndpointKeys();
        Iterator keys = map.keySet().iterator();
        if (!keys.hasNext())
            return true;
        while (keys.hasNext())
            CLILogger.getInstance().printMessage(keys.next().toString() + 
                                                " <" + WEBSERVICE_TYPE + "> ");        
        return false;
    }
    /**
     *  this method gets the value for type option.
     *  type option must be application, ejb, web or connector
     *  @return value of type option
     *  @throws CommandValidationException if type option is invalid.
     */
    private String getTypeOption() throws CommandValidationException
    {
        final String type = getOption(TYPE_OPTION);
	if (type == null) return type;
	if (type.matches(TYPE_OPTION_REG))
	    return type;
	throw new CommandValidationException(getLocalizedString("InvalidTypeOption"));
    }


    /** 
     * This method prints the objecName info for debugging purpose
     */
    private void printDebug(MBeanServerConnection mbsc, String objectName)
	throws Exception
    {
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
	    CLILogger.getInstance().printDebugMessage("****** TYPE *****");
	    final javax.management.MBeanParameterInfo[]  mbpi  = mboinfo[ii].getSignature();
	    for (int kk=0; kk<mbpi.length; kk++)
	    {
		CLILogger.getInstance().printDebugMessage("type = " + mbpi[kk].getType());
	    }
 	    CLILogger.getInstance().printDebugMessage("returnType = " + mboinfo[ii].getReturnType());


	}
    }

}
