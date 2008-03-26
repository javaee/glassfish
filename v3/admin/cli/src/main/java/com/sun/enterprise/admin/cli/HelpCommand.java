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

package com.sun.enterprise.admin.cli;

//import com.sun.enterprise.cli.framework.*;

//jdk 
import com.sun.enterprise.cli.framework.CLIManFileFinder;
import com.sun.enterprise.cli.framework.Command;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.More;
import java.io.Reader;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The help command will display the help text for all the commands and their
 * options
 */
public class HelpCommand extends Command 
{

  private static final int DEFAULT_PAGE_LENGTH = 50;
  private static final int NO_PAGE_LENGTH = -1;
  private static final String DEFAULT_HELP_PAGE = "help";

        /** Creates new HelpCommand */
  public HelpCommand() 
  {
  }

        /**
         * override abstract method validateOptions()
         */
    public boolean validateOptions() throws CommandValidationException
    {
        return true;
    }
    
	  /**
	   * Executes the command
	   * @throws CommandException
	   */
  public void runCommand() throws CommandException, CommandValidationException 
  {

	try {
	new More(getPageLength(),
        getSource(),
        getDestination(),
        getUserInput(),
        getUserOutput(),
        getQuitChar(),
        getPrompt());
	}
	catch (IOException ioe){
	  throw new CommandException(ioe);
	}
  }

  private String getCommandName(){
	return (operands.size() > 0
			? (String) getOperands().get(0)
			: DEFAULT_HELP_PAGE);
  }

  private Writer getDestination(){
	return new OutputStreamWriter(System.out);
  }

  private int getPageLength(){
      if ((getOption("isMultiMode")!=null &&
          getBooleanOption("isMultiMode")) &&
          (getOption("interactive")!=null &&
           getBooleanOption("interactive")) )
          return DEFAULT_PAGE_LENGTH;
      else
          return NO_PAGE_LENGTH;
  }

  private String getPrompt(){
      return getLocalizedString("ManpagePrompt");
  }
  
  private String getQuitChar(){
      return getLocalizedString("ManpageQuit");
  }

  private Reader getSource(){
	CLIManFileFinder c = new CLIManFileFinder();
	return c.getCommandManFile(getCommandName());
  }

  
  private Reader getUserInput(){
	return new InputStreamReader(System.in);
  }

  private Writer getUserOutput(){
	return new OutputStreamWriter(System.err);
  }

}
