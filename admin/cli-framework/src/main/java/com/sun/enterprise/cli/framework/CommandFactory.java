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
 * CommandFactory.java
 *
 * Created on June 23, 2003, 4:01 PM
 */

package com.sun.enterprise.cli.framework;


import java.util.Vector;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 *  This is a factory class that creates a Command object.
 *    @version  $Revision: 1.5 $
 *    @author  pa100654
 */
public class CommandFactory 
{
    
    /** Creates a new instance of CommandFactory */
    public CommandFactory() 
    {
    }

    
    /**  The static class that creates the command object 
     *   @param commandMatched, the ValidCommand object that contains 
     *          vital info on the command.
     *   @param options, the options in command line
     *   @param operands,the operands in command line
     *   @throws  CommandValidationException if there is an error creating 
     *            the command.
     */
    public static Command createCommand(ValidCommand commandMatched, OptionsMap options, 
                                        Vector operands) 
        throws CommandValidationException, InvalidCommandException
    {
        Command command = null;
        String commandName = commandMatched.getName();
        String className = commandMatched.getClassName();
        if (className == null)
        {
            LocalStringsManager lsm = 
            LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            throw new InvalidCommandException(commandName);
        }
        try
        {
            Class theClass = CommandFactory.class.getClassLoader().loadClass(className);
            //Class  theClass = Class.forName(className);
            command = (Command)theClass.newInstance();
            command.setName(commandName);
            command.setOptionsMap(options);
            command.setOperands(determineOperand(operands,
                                                 commandMatched.getDefaultOperand()));
            
            //command.setOperands(operands);
            command.setUsageText(commandMatched.getUsageText());        
            command.setProperties(commandMatched.getProperties());
        }
        catch(Throwable e)
        {
            // Throwable because NoClassDedFoundError is *not* an Exception
            LocalStringsManager lsm = 
            LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            throw new CommandValidationException(lsm.getString("UnableToCreateCommand",
                                                               new Object[] {commandName}), e);
            //e.printStackTrace();
        }
        return command;
    }


        /**
         *  This api will check if the user enters an operand, if not, then
         *  replace it with the defaultoperand specified in the
         *  CLIDescriptor.xml
         */
    private static Vector determineOperand(Vector operands, String defaultOperand)
    {
        return operands.size()<1 && defaultOperand!=null ?
               new Vector<String>(java.util.Arrays.asList(
               new String[]{defaultOperand})):operands;
    }
    
}
