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
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import com.sun.enterprise.admin.common.ObjectNames;


// jdk imports
import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CreateSecurityMapCommand extends SecurityMapCommand{
    
    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand()throws CommandException, CommandValidationException
    {
        if (!validateOptions())
            throw new CommandValidationException("Validation failed");
        
        
           String objectName = getObjectName();
            Object[] params = getParamsInfo();
            String operationName = getOperationName();
            String[] types = getTypesInfo();    
            String[] map = null;            
            String[] principals = null;
            String[] usergroups = null;
            AttributeList list = null;
    
             list =(AttributeList) params[0];
             if(list != null){
                int s = list.size();
                int i = 0 ;
                for(i=0;i<s;i++)
                {
                    Attribute attribute =(Attribute)list.get(i);
                    if(attribute.getName().equalsIgnoreCase("principal"))
                    {
                        String principal = (String)attribute.getValue();
                        principals  =((String[]) getListOfValues(principal));
                        list.set(i,new Attribute("principal",principals));
                     }
                     if ((attribute.getName().equalsIgnoreCase("user_group")))
                     {
                         String usergroup = (String)attribute.getValue();
                         usergroups = ((String[])getListOfValues(usergroup));
                         list.set(i,new Attribute("user_group",usergroups));
                    }
                       
                }    
            }
               
             
           MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                    getUser(), getPassword());
        try 
        { 
	     ObjectName object  =(ObjectName)mbsc.invoke(new ObjectName(objectName), 
				     operationName, params, types);
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
    
                
        
    private String[] getListOfValues(String sOptions){
        StringTokenizer optionTokenizer   = new StringTokenizer(sOptions,",");
        int             size            = optionTokenizer.countTokens();
        String []       sOptionsList = new String[size];
        for (int ii=0; ii<size; ii++){
            sOptionsList[ii] = optionTokenizer.nextToken();
        } 
        return sOptionsList;
   }
    
    public boolean validateOptions() throws CommandValidationException {
        return super.validateOptions();
    }
}
