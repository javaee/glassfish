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
 * $Id: MultiProcessCommand.java,v 1.7 2007/01/06 07:56:18 janey Exp $
 */


package com.sun.enterprise.cli.commands;

import java.lang.Character;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;

import com.sun.enterprise.cli.framework.*;

/**
   Creates a multimode environment for executing the commands from a file or
   by supplying commands interactively. Displays the help message for the
   command if help is sought.
*/
public class MultiProcessCommand extends S1ASCommand
{

    private static final String FILE_OPTION      = "file";
    private static final String PRINTPROMPT_OPTION      = "printprompt";
    private static final String ENCODING_OPTION  = "encoding";
    private static final String EXIT             = "exit";
    private boolean mDone               = false;
    private boolean printPrompt         = true;
    private final String kPromptString  = getLocalizedString("AsadminPrompt");
    //localEnvironment contains the keys for the password variables
    // set using the export command in multimode
    private static HashMap localEnvironment = new HashMap();

    /**
     *  Default constructor to initialize the class.
     */
    public MultiProcessCommand()
    {
        super();
    }

    /**
     *	Checks whether options are specified at the command line.
     *	If options are specified, then it validates for the options else
     *	it assumes no options are specified. In MultiProcessCommand without
     *	specifying an option brings up the interactive mode, otherwise it
     *	reads the commands from a file with option specified.
     */
    public boolean validateOptions() throws CommandValidationException
    {
        printPrompt = getBooleanOption(PRINTPROMPT_OPTION);
        return super.validateOptions();
    }


    /**
     *	Implements the execution of MultiProcessCommand whether the input
     *	is from a file or from a standard input interactively.
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();
        String line = null;
     
        try
        {
            if (isFileOptionSpecified()) 
            { 
                CLILogger.getInstance().printDebugMessage("file option specified");
                checkForFileExistence(null, getOption(FILE_OPTION));
                setInputStreamToFile();
            }
            else
                printExitMessage();
            line = printPromptAndReadLine();

            while (!isExitLine(line))
            {
                if (isExecutableLine(line)) 
                {
                    processLine(line);
                }
                line = printPromptAndReadLine();
            } 
        }
        catch ( CommandException ce )
        {
            throw ce;
        }
        catch ( Exception e )
        {
            throw new CommandException(e);
        }
    }


    
    /**
     * Prints the exit message for the mulitprocess command input
     */
    private void printExitMessage()
    {
        CLILogger.getInstance().printMessage(getLocalizedString("ExitMessage"));
    }


    /**
     * Prints the prompt to the standard output
     * Reads the line from the buffered input stream
     * @return String The line read from the inputstream or file
     */
    private String printPromptAndReadLine() throws CommandException
    {
        try 
        {
            if (printPrompt)
                InputsAndOutputs.getInstance().getUserOutput().print( kPromptString );
            String line = InputsAndOutputs.getInstance().getUserInput().getLine();
            if (line == null && isFileOptionSpecified() == true) 
                return EXIT;
            else
                return line;
        }
        catch (IOException ioe)
        {
            throw new CommandException(getLocalizedString("CouldNotPrintOrRead"), 
                                       ioe);
        }
    }

    /**
     * Checks to see if the line is executable, i.e if the line in non-empty and
     * the line doesnt start with "#"
     */
    private boolean isExecutableLine( final String line )
    {
        boolean isExecutable = true;
        if ( line == null )
        {
            isExecutable    = false;
        }
        else if ( line.trim().equals("") ||
                  line.startsWith("#") ||
                  line.length() < 1 )
        {
            isExecutable = false;
        }
        return isExecutable;
    }


    /**
     *  set user input stream
     *  this method assumes that file option is set
     *  @throws CommandException
     */
    private void setInputStreamToFile() throws CommandException
    {
        try
        {
            final String sEncoding = getOption("ENCODING_OPTION");
            if (sEncoding == null) 
            {
                CLILogger.getInstance().printDebugMessage("Set input stream");
                InputsAndOutputs.getInstance().setUserInputFile(
                    getOption(FILE_OPTION));
            }
            else 
            {
                InputsAndOutputs.getInstance().setUserInputFile(
                    getOption(FILE_OPTION), sEncoding);
            }
        }
        catch (IOException ioe) 
        {
            throw new CommandException(getLocalizedString("CouldNotSetInputStream"),
                                       ioe);
        }
    }


    /**
     *  This method will split the string to array of string separted by space(s)
     *  If there is a quote " around the string, then the string will not be 
     *  splitted.  For example, if the string is: abcd efg "hij klm", the string
     *  array will contain abcd|efg|hij klm|
     *  @param line - string to be converted
     *  @return - string array
     *  @throws CommandException
     */
    private String[] splitStringToArray( String line )
        throws CommandException
    {
        final CLITokenizer cliTokenizer = new CLITokenizer(line, " ");
        String[] strArray = new String[cliTokenizer.countTokens()];
        int ii=0;
        while (cliTokenizer.hasMoreTokens()) 
        {
            strArray[ii++] = cliTokenizer.nextTokenWithoutEscapeAndQuoteChars();
            CLILogger.getInstance().printDebugMessage("CLIToken = [" + strArray[ii-1] +"]");

        }
        return strArray;
    }


    /**
     * Checks to see if the user supplied an "exit" or "quit"
     */
    private boolean isExitLine( final String line )
    {
        if ( line == null ||
             (line != null && (line.equalsIgnoreCase( "exit" ) ||
                               line.equalsIgnoreCase( "quit" )) ))
        {
            return true;
        }
        return false;
    }    


    /**
     * Checks to see if the 'file' option is specified
     * @return boolean returns true if specified else false
     */
    private boolean isFileOptionSpecified()
    {
        return ( getOption(FILE_OPTION) != null );
    }


    /**
     * check if validCommand is null
     */
    private void checkValidCommand(ValidCommand validCommand, 
                                   String commandName) 
        throws CommandException
    {
        try {
            if (validCommand == null) {
                CLILogger.getInstance().printMessage(getLocalizedString("InvalidCommand",
                                                     new Object[]{commandName}));
                CLIMain.displayClosestMatch(commandName);
                throw new CommandException(getLocalizedString("UseHelpCommand"));
            }
        }
        catch (CommandException ce) {
                //throw a different exception with different message.
            throw new CommandException(getLocalizedString("UseHelpCommand"));
        }
    }
    

    /**
     *  Process the input line supplied by the user.
     *  The line is converted to array of args and supplied to CommandFactory
     **/
    private void processLine(String line) 
    {
        ValidCommand validCommand = null;
        try 
        {
            String[] commandLine = splitStringToArray(line);

                //get CLI descriptor instance
            CLIDescriptorsReader cliDescriptorsReader = 
            CLIDescriptorsReader.getInstance();

                //get the validCommand object from CLI descriptor
            validCommand = cliDescriptorsReader.getCommand(
                commandLine[0]);

                //check if command is help then throw the HelpException to
                //display the manpage or usage-text
            if (commandLine[0].equals("help")) throw new HelpException(
                commandLine.length<2?null:commandLine[1]);

            checkValidCommand(validCommand, commandLine[0]);

                //parse the command line arguments
            CommandLineParser clp = new CommandLineParser(commandLine, 
                                                          validCommand);


            //creates the command object using command factory
            final Command command = CommandFactory.createCommand(
                                                   validCommand, clp.getOptionsMap(),
                                                   clp.getOperands());
            
            //validate the Command with the validCommand object
            new CommandValidator().validateCommandAndOptions(validCommand,
                                                             command.getOptions(),
                                                             command.getOperands());
                //invoke the command
            command.runCommand();
        }
        catch (HelpException he)
        {
            invokeHelpClass(he.getHelpClassName(), he.getCommandName(), he.getUsageText());

        }
        catch (CommandValidationException cve)
        {
            printUsageText(validCommand);
            CLILogger.getInstance().printExceptionStackTrace(cve);
            CLILogger.getInstance().printError(cve.getLocalizedMessage());
        }
        catch (CommandException ce)
        {
            CLILogger.getInstance().printExceptionStackTrace(ce);
            CLILogger.getInstance().printError(ce.getLocalizedMessage());
        }
    }

    /**
     *  This method invokes the help command class.
     *  If help command clss is invalid then the usage text is displayed.
     */
    private void invokeHelpClass(String helpClassName,
                                 String helpCommandName,
                                 String commandUsageText)
    {
        try
        {
            Command helpCommand = null;
            Class  helpClass = Class.forName(helpClassName);
            helpCommand = (Command)helpClass.newInstance();
            helpCommand.setName(helpCommandName);
            if (helpCommandName != null)
                helpCommand.setOperands(new java.util.Vector(java.util.Arrays.asList(
                                                             new String[]{helpCommandName})));
            //set an internal option called isMultiMode
            helpCommand.setOption("isMultiMode", "true");

            //get interactive value from the environment
            final String interactiveVal = (String)CommandEnvironment.getInstance().
                                          getEnvironments().get(INTERACTIVE);
            //set the interactive mode
            helpCommand.setOption(INTERACTIVE,
                                  interactiveVal==null?"true":interactiveVal);
            
            helpCommand.runCommand();
        }
        catch (Exception e)
        {
            if (commandUsageText == null) {
                CLILogger.getInstance().printMessage(getLocalizedString("NoManualEntry",
                                                     new Object[]{helpCommandName}));
                try {
                    CLIMain.displayClosestMatch(helpCommandName);
                }
                catch (CommandException ce) {
                    CLILogger.getInstance().printMessage(ce.getLocalizedMessage());
                }
                
            }
            else
                CLILogger.getInstance().printMessage(getLocalizedString("Usage",
                                                     new Object[]{commandUsageText}));
        }
    }


    /** 
     *  this method prints the usage text from validCommand
     *  @param validCommand - contains the usage text
     */
    private void printUsageText(final ValidCommand validCommand)
    {
        if (validCommand != null && validCommand.getUsageText() != null)
        {
            CLILogger.getInstance().printError(getLocalizedString("Usage",
                                               new Object[]{validCommand.getUsageText()}));
        }
    }


    /**
     * This method adds a name and value to the local environment.
     * @param name - option name 
     *  @param value - option value
     */
    public static void setLocalEnvironment(String name, String value)
    {
        if ( localEnvironment.containsKey(name) )
        {
            localEnvironment.remove( name );
        }
        localEnvironment.put(name, value );
    }


    /**
     *  This method removes environment variable from the local environment.
     *  @param name is the name in the local environment to be removed.
    */
    public static Object removeLocalEnvironment( String name )
    {
        return localEnvironment.remove(name);
    }


    /**
     *  returns the local environment value by the given key
     */
    public static String getLocalEnvironmentValue(String key)
    {
        return (String) localEnvironment.get(key);
    }


    /**
	Returns the HashMap of Local environment.
    */
    public static HashMap getLocalEnvironments()
    {
        return localEnvironment;
    }


    /**
	Returns the number of elements in local environment
    */
    public static int getNumLocalEnvironments()
    {
        return localEnvironment.size();
    }

    
    /**
     *  This method removes all environment variables from HashMap
    */
    public static void removeAllEnvironments()
    {
        localEnvironment.clear();
    }


}

