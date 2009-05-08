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

import java.util.Iterator;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Map;

/**
 * The <code>CLIMain</code> contains a main that calls the appropriate
 * objects in the CLI framework and invokes the command object.
 *    @version  $Revision: 1.12 $
 */
public class CLIMain 
{
    // Help command and options for asadmin
    private static final String HelpCommandAndOptions = "help|--help|-[?]";
    private static final String VERSION_OPTION = "-V";
    private static final String VERSION_COMMAND = "version";
    private static final int MAX_COMMANDS_TO_DISPLAY = 75;


    public static void invokeCLI(String cmdline, InputsAndOutputs io)
        throws CommandException, CommandValidationException, InvalidCommandException
    {
        InputsAndOutputs.setInstance(io);
        String[] args = splitStringToArray(cmdline);
        new CLIMain().invokeCommand(args);
    }

    
    public static void main(String[] args) 
    {
        long startTime = 0;        
        boolean time = false;
        if (System.getProperty("com.sun.appserv.cli.timing") != null) {
           time = true;
           startTime = System.currentTimeMillis();
        }
        
        try 
        {
            new CLIMain().invokeCommand(args);
            if (time) {
                CLILogger.getInstance().printDebugMessage(
                    "Command execution time: " + 
                    (System.currentTimeMillis() - startTime) + " ms");
            }
            System.exit(0);
        }
        catch (Throwable ex) 
        {
            CLILogger.getInstance().printExceptionStackTrace(ex);
            CLILogger.getInstance().printError(ex.getLocalizedMessage());
            System.exit(1);
        } 
    }

    

    /**
     *  This is the important bulk of reading the xml, validating, creating
     *  command vi command factory and invoke the command.
     */
    public void invokeCommand(String[] args)
        throws CommandException, CommandValidationException, InvalidCommandException
    {
        invokeCommand(args, null);
    }


    /**
     */
    public void invokeCommand(String[] args, Object caller)
        throws CommandException, CommandValidationException, InvalidCommandException
    {
        ValidCommand validCommand = null;

        try
        {
            //read CLI descriptor
            final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();

            if (args.length < 1)
            {
                cliDescriptorsReader.setSerializeDescriptorsProperty(CLIDescriptorsReader.DONT_SERIALIZE);
                    //pass in null as the command Name.  The default command
                    //shall be returned, else an exception is thrown.
                validCommand = cliDescriptorsReader.getCommand(null);
                    //set args[0] to the default command.
                args = new String[] {cliDescriptorsReader.getDefaultCommand()};
            }
            else
            {
                //check if command is -V, as stated in the CLIP that
                //-V is supported and it is the same as version command
                if (args[0].equals(VERSION_OPTION))
                    args[0] = VERSION_COMMAND;

                //cliDescriptorsReader.setSerializeDescriptorsProperty(CLIDescriptorsReader.SERIALIZE_COMMANDS_TO_FILE);
                cliDescriptorsReader.setSerializeDescriptorsProperty(CLIDescriptorsReader.SERIALIZE_COMMANDS_TO_FILES);

                //get the validCommand object from CLI descriptor
                validCommand = cliDescriptorsReader.getCommand(args[0]);
            }
            setCommandLocalizedProperty(cliDescriptorsReader.getProperties());

                //check if command is help then throw the HelpException to
                //display the manpage or usage-text
            if (args[0].matches(HelpCommandAndOptions))
                throw new HelpException(args);



            checkValidCommand(validCommand, args[0]);

            //parse the command line arguments
            final CommandLineParser clp = new CommandLineParser(args, validCommand);

            //creates the command object using command factory
            final Command command = CommandFactory.createCommand(
                                                   validCommand, clp.getOptionsMap(),
                                                   clp.getOperands());

            //validate the Command with the validCommand object
            new CommandValidator().validateCommandAndOptions(validCommand,
                                                             command.getOptions(),
                                                             command.getOperands());


            command.setCaller(caller);

            //invoke the command
            command.runCommand();
            return;
        }
        catch (HelpException he)
        {
            invokeHelpClass(he.getHelpClassName(), he.getCommandName(), he.getUsageText(),he.isShell());
        }
        catch (CommandValidationException cve )
        {
                //rethrow CommandValidationException with Usage Text
            throw new CommandValidationException(getUsageTextFromValidCommand(validCommand)
                                                 + "\n" + cve.getLocalizedMessage());
        }

    }


    /**
     *  This method invokes the help command class.  
     *  If help command clss is invalid then the usage text is displayed.
     */
    private void invokeHelpClass(String helpClassName, 
                                        String helpCommandName, 
                                        String commandUsageText,
                                        boolean isShell)
        throws InvalidCommandException
    {
        try 
        {
            Command helpCommand = null;
            Class  helpClass = Class.forName(helpClassName);
            helpCommand = (Command)helpClass.newInstance();
            helpCommand.setName(helpCommandName);
            if (helpCommandName != null)
                helpCommand.setOperands(new java.util.Vector<String>(java.util.Arrays.asList(
                                        new String[]{helpCommandName})));
            if(isShell){
                helpCommand.setOption("isMultiMode","true");
                final String interactiveVal = (String)CommandEnvironment.getInstance().
                                          getEnvironments().get("interactive");
                    //set the interactive mode
                helpCommand.setOption("interactive",
                                  interactiveVal==null?"true":interactiveVal);
            }
            helpCommand.runCommand();
        }
        catch (Exception e)
        {
            if (commandUsageText == null) {
                    //displayClosestMatch(helpCommandName, null, null);
                throw new InvalidCommandException(helpCommandName);
            }
            else
                CLILogger.getInstance().printMessage(getLocalizedString("Usage",
                                                     new Object[]{commandUsageText}));
        }
    }


    public static void displayClosestMatch(final String commandName,
                                           final Map<String, String> moreCommands,
                                           final String msg)
        throws InvalidCommandException
    {
        try {
                //remove leading "*" and ending "*" chars
            int beginIndex = 0;
            int endIndex = commandName.length();
            if (commandName.startsWith("*")) beginIndex = 1;
            if (commandName.endsWith("*")) endIndex = commandName.length()-1;
            final String trimmedCommandName = commandName.substring(beginIndex, endIndex);
                
            //add all matches to the search String since we want
            //to search all the commands that matches the string
            final String[] matchedCommands = SearchCommands.getMatchedCommands(".*"+trimmedCommandName+".*", moreCommands);
                //don't want to display more than 50 commands
            if (matchedCommands.length > 0 && matchedCommands.length<MAX_COMMANDS_TO_DISPLAY)
            {
                System.out.println(msg==null?
                                   getLocalizedString("ClosestMatchedCommands",null):
                                   msg);
                for (String eachCommand : matchedCommands)
                {
                    System.out.println("    "+eachCommand);
                }
            }
                //find the closest distance
            else {
                final String[] allCommands = SearchCommands.getAllCommands();
                final String nearestString = StringEditDistance.findNearest(commandName, allCommands);
                    //do not want to display the string if the edit distance is too large
                if (StringEditDistance.editDistance(commandName, nearestString) < 5) {
                    System.out.println(msg==null?
                                       getLocalizedString("ClosestMatchedCommands",null):
                                       msg);
                    System.out.println("    "+nearestString);
                }
                else
                    throw new InvalidCommandException(commandName);
            }
        }
        catch (Exception e)
        {
            throw new InvalidCommandException(commandName);
        }
    }
    
    
    /**
     *  This method returns the usages text from validCommand object
     *  @param validCommand
     *  @return usage text
     */
    private String getUsageTextFromValidCommand(final ValidCommand validCommand)
    {
        if (validCommand != null && validCommand.getUsageText() != null)
        {
            return getLocalizedString("Usage", new Object[]{validCommand.getUsageText()});
        }
        return "";
    }


    /**
     * check if validCommand is null
     */
    private void checkValidCommand(ValidCommand validCommand, 
                                          String commandName) 
        throws InvalidCommandException, CommandException
    {
        if (validCommand == null) {
            //displayClosestMatch(commandName);
                //throw an empty exception so that exit code is 1.
            throw new InvalidCommandException(commandName);
        }
    }

    

    /** 
     *  sets the localized property in the command module
     *  @params Iterator - iterator containing the localized properties
     */
    private void setCommandLocalizedProperty(Iterator<Properties> localizePropertiesIter)
        throws CommandValidationException
    { 
        LocalStringsManagerFactory.setCommandLocalStringsManagerProperties(
            localizePropertiesIter);
    }


    /** 
     *  returns the localized string from the properties file 
     *  Calls the LocalStringsManagerFactory.getFrameworkLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     *  @param toInsert, the strings to be inserted in the placeholders
     */
    private static String getLocalizedString(String key, Object[] toInsert)
    {
        try
        {
            final LocalStringsManager lsm = LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            if (toInsert == null) 
                return lsm.getString(key);
            else
                return lsm.getString(key, toInsert);
        }
        catch (CommandValidationException cve)
        {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
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
    private static String[] splitStringToArray( String line )
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

}
