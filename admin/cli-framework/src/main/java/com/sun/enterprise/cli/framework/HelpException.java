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

package com.sun.enterprise.cli.framework;

/**
 *  Whenever the HelpException is thrown, it will use the helpclass defined
 *  in the xml to display the usage text or manpage.
 * @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 * @version $Revision: 1.3 $
 */
public class HelpException extends java.lang.Exception 
{
    private String command = null;
    private boolean isShell = false;

    /**
     * Creates new <code>HelpException</code> without detail message.
     */
    public HelpException() 
    {
    }

    /**
     * Creates new <code>HelpException</code> with the command name to display the help
     */
    public HelpException(String commandName) 
    {
        command = commandName;
    }

    public HelpException(String[] args) 
    {
        if (args.length<2) {
            command = null;
        }
        else {
                //help, help --shell, help command --shell
            int next = 1;
            if(!args[next].startsWith("--"))
                command = args[next++];
            if(args.length>next && args[next].equals("--shell"))
                isShell = true;
        }
    }

    /**
     *  Returns the help class to invoke.
     */
    public String getHelpClassName()
    {
        //read CLI descriptor
        final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
	return cliDescriptorsReader.getHelpClass();
    }
    
    /**
     *  Returns the command to display the help.
     */
    public String getCommandName()
    {
        return command;
    }

    /**
     *  Returns the usage text of the command.
     */
    public String getUsageText()
    {
        try 
        {
                //read CLI descriptor
            final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
                //get the validCommand object from CLI descriptor
            final ValidCommand validCommand = cliDescriptorsReader.getCommand(command);
            return validCommand.getUsageText();
        }
        catch (Exception e) 
        {
            return null;
        }
    }
    
    public boolean isShell(){
        return isShell;
    }
}


