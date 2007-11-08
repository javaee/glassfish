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
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ListSecurityMapCommand extends GenericCommand{

    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand()throws CommandException, CommandValidationException{
        if (!validateOptions())
            throw new CommandValidationException("Validation failed");
        
           String objectName = getObjectName();
            Object[] params = getParamsInfo();
            String operationName = getOperationName();
            String[] types = getTypesInfo();    
            String[] map = null;            
            String[] principals = null;
            String[] usergroups = null;
            
           MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                    getUser(), getPassword());
        try { 
	     ArrayList list  =(ArrayList)mbsc.invoke(new ObjectName(objectName), 
					     operationName, params, types);
             
             Iterator iterator= list.iterator();
             while (iterator.hasNext()){
                map = (String[]) iterator.next();
                    CLILogger.getInstance().printMessage(" ====================================");
                    CLILogger.getInstance().printMessage(" Security Map Name is :"  +map[0]);
                    CLILogger.getInstance().printMessage(" =====================================");
                    if(map[1] != null)
                        principals = getOptionsList(map[1]);
                    if(map[2] != null)    
                        usergroups = getOptionsList(map[2]);
                        
                    if(principals != null){ 
                        CLILogger.getInstance().printMessage("Principals for Security Map :"+  map[0] +"  are :");
                        for(int i = 0;i<principals.length;i++)
                            CLILogger.getInstance().printMessage("<principal>  "  +principals[i]);  
                    }
                   CLILogger.getInstance().printMessage("\n");
                   if(usergroups != null ){
                   CLILogger.getInstance().printMessage("UserGroups for Security Map :"+ map[0] +"  are :");
                        for(int i = 0;i<usergroups.length;i++)
                            CLILogger.getInstance().printMessage("<user-group>  "+usergroups[i]);  
                    }
                    CLILogger.getInstance().printMessage("\n");
                    if(map[3] != null){
                        String username = map[3];
                        CLILogger.getInstance().printMessage("Backend Principal User Name for :"+map[0] +"  is  :"+username);
                        CLILogger.getInstance().printMessage(" \n");
                    }
                    if(map[4] != null){
                        String password = map[4];
                        CLILogger.getInstance().printMessage("Backend Principal Password for :"+map[0] +"  is  :"+password);
                        CLILogger.getInstance().printMessage("\n");
                    }

                    
                    }     
                 
	    CLILogger.getInstance().printDetailMessage(getLocalizedString(
						       "CommandSuccessful",
            					       new Object[] {name}));
        }catch(Exception e){ 
	    if (e.getLocalizedMessage() != null)
		CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
						     new Object[] {name} ), e);
        }        
    }
        
    private String[] getOptionsList(Object sOptions){
        StringTokenizer optionTokenizer   = new StringTokenizer((String)sOptions,",");
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
