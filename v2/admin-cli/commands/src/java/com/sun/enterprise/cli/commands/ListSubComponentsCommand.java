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

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import com.sun.enterprise.admin.common.ObjectNames;

// jdk imports
import java.io.File;
import java.util.Iterator;

/**
 *  Lists the submodules of a deployed component
 *  @version  $Revision: 1.3 $
 */
public class ListSubComponentsCommand extends S1ASCommand
{
    private static final String GET_MODULE_COMPONENTS = "getModuleComponents";
    private static final String APPNAME_OPTION        = "appname";
    private static final String TYPE_OPTION           = "type";
    private static final String EJBS                  = "ejbs";
    private static final String SERVLETS              = "servlets";
    private static final String EJB_SUB_MODULE        = "EJBModule|SessionBean|StatelessSessionBean|StatefulSessionBean|MessageDrivenBean|EntityBean";
    private static final String SERVLET_SUB_MODULE    = "WebModule|Servlet";
    private static final String TYPE_OPTION_VALUES    = "ejbs|servlets";

    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of 
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
        //Check if the type option arguments are valid ejbs|servlets
	final String typeOption = getOption(TYPE_OPTION);
        if ((typeOption != null) && !typeOption.matches(TYPE_OPTION_VALUES))
            throw new CommandValidationException(getLocalizedString(
                                                     "InvalidTypeOption"));
    	return super.validateOptions();
    }

    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() 
            throws CommandException, CommandValidationException
    {
	validateOptions();

        final String objectName = getObjectName();
	final String appname = getOption(APPNAME_OPTION);
	final String typeOption = getOption(TYPE_OPTION);
	final Object[] params = getParams(appname);
        final String[] types = getTypes(appname);

	//use http connector
	final MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                     getUser(), getPassword());
        try
        { 
	    //	    if (System.getProperty("Debug") != null) printDebug(mbsc, objectName);

	    Object returnValue = mbsc.invoke(new ObjectName(objectName), 
					     GET_MODULE_COMPONENTS, params, types);
	    printObjectName(returnValue, typeOption);
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
     *  Returns the paramters to use to pass into GET_MODULE_COMPONENTS method
     *  @param appname - appname  if appname is null then returns the paramter
     *  without appname.
     *  @return the parameter in Object[]
     */
    private Object[] getParams(String appname)
    {
	if (appname == null)
	{
	    return new Object[] {(String)getOperands().get(0)};
	}
	else
	{
	    return new Object[] {appname, (String)getOperands().get(0)};
	}
    }


    /**
     *  Returns the types to use for GET_MODULE_COMPONENTS method
     *  @param appname - appname  if appname is null then returns the type
     *  without appname.
     *  @return the type in String[]
     */
    private String[] getTypes(String appname)
    {
	if (appname == null)
	{
            return new String[] {String.class.getName()};
	}
	else
	{
            return new String[] {String.class.getName(), String.class.getName()};
	}
    }


    /**
     *  This method prints the object name.
     *  @param return value from operation in mbean
     */
    private void printObjectName(Object returnValue, String typeOption) 
                throws CommandException
    {
	if (returnValue == null) 
        {
                    CLILogger.getInstance().printDetailMessage(
                                                getLocalizedString("NoElementsToList"));
        }
	try 
	{
	    if (returnValue.getClass().getName().equals(STRING_ARRAY))
	    {
		final String[] objs = (String[])returnValue;
                boolean nothingToList = true;

		for (int ii=0; ii<objs.length; ii++)
		{
		    CLILogger.getInstance().printDebugMessage("***** " + objs[ii]);
		    final ObjectName on = new ObjectName(objs[ii]);
                    if (typeOption != null)
                    {
                        if ((typeOption.equals(EJBS) && 
                             on.getKeyProperty("j2eeType").matches(EJB_SUB_MODULE)) ||
                            (typeOption.equals(SERVLETS) && 
                             on.getKeyProperty("j2eeType").matches(SERVLET_SUB_MODULE)))
                        {
                            printSubComponent(on); 
                            nothingToList=false;
                        }
                    }
                    else 
                    {
                        printSubComponent(on); 
                        nothingToList=false;
                    }
                }
	        if (nothingToList) 
                {
                    CLILogger.getInstance().printDetailMessage(
                                                getLocalizedString("NoElementsToList"));
                }
            }
        }
	catch (Exception e)
	{
	    throw new CommandException(e);
	}
    }


    /** 
     * This method prints the sub component name 
     */
    private void printSubComponent(ObjectName on)
    {
        CLILogger.getInstance().printMessage(on.getKeyProperty("name") + " <"+
                                             on.getKeyProperty("j2eeType") + ">");
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
