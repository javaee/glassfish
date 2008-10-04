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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * The <code>CommandLineParser</code> object is used to parse the
 * command line and verify that the command line is CLIP compliant.
 *    @version  $Revision: 1.9 $
 */
public class CommandLineParser 
{
    
    // Name of Command
    private String commandName = null;

    // OptionsMap
    private OptionsMap optionsMap = new OptionsMap();
    
    // List of Short Options from command-line argument
    private HashMap optionsList = new HashMap();
    
    // Array of Operands from command-line argument
    private Vector<String> Operands = new Vector<String>();
    
    //Valid Command object
    private ValidCommand validCommand = null;
    
    //Any short option
    private static final String ANY_SHORT_OPTION_REGEXP = "^-[\\w?]+";
    private static final String ANY_SHORT_OPTION_REGEXP_WITH_EQUAL = "^-\\w=.*";
    
    // Regular expression for short options
    private static final String SHORT_OPTION_REGEXP = "^-[\\w?]+";
    
    // Regular expression for short option and argument
    private static final String SHORT_OPTION_ARGUMENT_REGEXP = "^-[\\w?](=.*)";
    
    // Regular expression for long option
    // private static final String LONG_OPTION_REGEXP = "^--.+(=.*)*";
    private final String LONG_OPTION_REGEXP = "^--\\w[-\\w]*(=.*)*";
    
    // Regular expression for command name
    private static final String COMMAND_NAME_REGEXP = "^[a-z\\-][a-z0-9\\-\\_\\ ]*$";

    // Regular expression of help short option.  
    //help short option can either be -h or -?
    private static final String SHORT_OPTION_HELP_REGEXP = "-\\w*[?]\\w*";

    // HELP OPTION STRING
    private static final String HELP_OPTION = "--help";

    //Boolean --no- optoin
    private static final String BOOLEAN_NO_OPTION = "--no-";
    
    // BOOLEAN STRING
    private static final String BOOLEAN = "boolean";
    
    // TRUE STRING
    private static final String TRUE = "true";
    
    // FALSE STRING
    private static final String FALSE = "false";

    
    /** Creates new CommandLineParser */
    public CommandLineParser() 
    {
    }
    
    
    /** Create new CommandLineParser with the given argument
     *  @param validCommand - ValidCommand object containing the specification
     *                        for the command
     */
    public CommandLineParser(ValidCommand validCommand) 
    {
        this.validCommand = validCommand;
    }
    
        
    /** Creates new CommandLineParser with the given argument
     *  @param args - command line arguments
     *  @param validCommand - ValidCommand object containing the specification
     *                        for the command
     *  @throws CommandValidationException if command name is invalid
     */
    public CommandLineParser(String[] args, ValidCommand validCommand)
        throws CommandValidationException, InvalidCommandException, HelpException
    {
        this.validCommand = validCommand;
        if (validCommand != null) 
        {
            parseCommandLine(args);
        }
        
    }
    
    
    
    /** Parse the command line arguments accordingly to CLIP
     *  @param args - command line arguments
     *  @throws CommandValidationException if command name is invalid
     */
    public void parseCommandLine(final String[] args)
        throws CommandValidationException, InvalidCommandException, HelpException
    {
        commandName = args[0];


        if (this.validCommand == null)
        {
            throw new InvalidCommandException(commandName);
        }
        
        //get all options
        for (int ii=1; ii<args.length; ii++) 
        {
            //figure out if this is a help option then throw a HelpException
            //so that manpage or usage text gets displayed
            if (args[ii].equals(HELP_OPTION) ||
                args[ii].matches(SHORT_OPTION_HELP_REGEXP))
                throw new HelpException(commandName);


            //verify and get short options
            if (args[ii].matches(ANY_SHORT_OPTION_REGEXP)  ||
                args[ii].matches(ANY_SHORT_OPTION_REGEXP_WITH_EQUAL))
            {
                ii = verifyShortOptions(args, ii);
            }
            
            //get long options
            //long option can be of the following:
            // --<alphanumeric chars>
            // --<alphanumeric chars>-<alphnumeric chars>
            // --<alphanumeric chars>=<option_argument>
            else if (args[ii].matches(LONG_OPTION_REGEXP)) 
            {
                ii = insertLongOption(args, ii);
            }
            
            //get operands
            else 
            {
                ii = insertOperands(Arrays.asList(args).listIterator(ii));
            }
        }
        insertDefaultOptions();
        insertEnvironmentOptions();
        insertPrefFileOptions();
        replaceAlternativeOptions();
    }

    
    /** Checks if the short option is valid.
     *  If valid, then call insertShortOption() to insert
     *  the short options to the HashMap.
     *  According to CLIP, short option consist of a hyphen
     *  followed a single letter or digit,
     *  @params ii   - index in the arguments
     *  @return the index of the next argument
     *  @throws CommandValidationException

     */
    private int verifyShortOptions(final String[] args, int ii)
        throws CommandValidationException
    {
        int index;
        if (args[ii].matches(SHORT_OPTION_REGEXP) ||
            args[ii].matches(SHORT_OPTION_ARGUMENT_REGEXP)) {
            index = insertShortOption(args, ii);
        } else {
            throw new CommandValidationException(getLocalizedString("NoSuchOption",
                                                 new Object[] {args[ii]}));
        }
        return index;
    }
    
    
    /** Insert short options group to the shot options list
     *  @param sOption - group of short options
     *
     */
    private void insertShortOptionsGroup(final String sOptions)
        throws CommandValidationException 
    {
        for (int ii=1; ii<sOptions.length(); ii++) 
        {
            final String optionName = findOptionName(sOptions.charAt(ii));
            insertBooleanOption(optionName);
        }
    }
    
    
    /** call this method only if there is no option argument
     *  The assumption is that if there are no option argument
     *  then the option is of type boolean.
     *  @param optionName - name of option
     *  @throws CommandValidationException if option is not boolean
     */
    private void insertBooleanOption(String optionName)
        throws CommandValidationException 
    {
        if (checkOptionIsBoolean(optionName))
        {
            //optionsList.put(optionName, getDefaultBooleanValue(optionName));
            //Bug #6296862, same as long option, boolean option without argument is always TRUE
            optionsMap.addCLValue(optionName, TRUE);
        }
        else
        {
            optionsMap.addCLValue(optionName, null);
        }
    }

    
    /** Insert short option to the short options list
     *  @params args - arguments of options
     *  @params ii   - index in the arguments
     *  @return the index of the next argument
     *  @throws CommandValidationException
     */
    private int insertShortOption(final String[] args, int ii)
        throws CommandValidationException 
    {

        //if short option length is greater than 2 then it is a
        //short options group
        if (args[ii].length() > 2) 
        {
            final int index = args[ii].indexOf('=');
            //if the long option and option argument is delimited by a space
            if (index == -1) 
            {
                insertShortOptionsGroup(args[ii]);
            }
            else 
            {
                insertOptionWithEqualSign(findOptionName(args[ii].charAt(1)), 
                                          args[ii].substring(index+1));
            }
        }
        else 
        {
            final String optionName = findOptionName(args[ii].charAt(1));
            //make sure that the next argument is valid
            if (ii+1 < args.length) 
            {
                
                //if the next argument starts with "-" then it's not
                //an option argument
                //if argument is a boolean option then next argument
                //is not an option argument
                if (args[ii+1].startsWith("-") ||
                    checkOptionIsBoolean(optionName) ) 
                {
                    insertBooleanOption(optionName);
                }
                else 
                {
                    optionsMap.addCLValue(optionName, args[ii+1]);
                    ii++;
                }
            }
            else 
            {
                insertBooleanOption(optionName);
            }
        }
        return ii;
    }
    
    
    /** Insert long option to the long options list
     *  @params args - arguments of options
     *  @params ii   - index in the arguments
     *  @return the index of the next argument
     *  @throws CommandValidationException
     */
    private int insertLongOption(final String[] args, int ii)
        throws CommandValidationException 
    {
        final int index = args[ii].indexOf('=');
        //if the long option and option argument is delimited by a space
        if (index == -1) 
        {
            //boolean option with "--no-" always means false
            if (args[ii].startsWith(BOOLEAN_NO_OPTION) && 
                checkOptionIsBoolean(args[ii].substring(BOOLEAN_NO_OPTION.length())) )
            {
                optionsMap.addCLValue(args[ii].substring(BOOLEAN_NO_OPTION.length()), FALSE);
            }
            else if (checkOptionIsBoolean(args[ii].substring(2)))
            {
                //long boolean option is always true
                optionsMap.addCLValue(args[ii].substring(2), TRUE);
            }
            else if (ii+1<args.length) 
            {
                //if the next argument starts with "-" then it's missing an option value
                if (args[ii+1].startsWith("-") )
                {
                    optionsMap.addCLValue(args[ii].substring(2), null);
                }
                else 
                {
                    optionsMap.addCLValue(args[ii].substring(2), args[ii+1]);
                    ii++;
                }
            }
            else
                optionsMap.addCLValue(args[ii].substring(2), null);
        }
        //if the long option and option argument is delimited by '='
        else
        {
            insertOptionWithEqualSign(args[ii].substring(2, index),
                                      args[ii].substring(index+1));
        }
        return ii;
    }


    /**
     *  This method inserts the optionName and optionValue to optionsList
     *  @param optionName - name of option
     *  @param optoinValue - value of option
     *  @throws CommandValidationException if invalid boolean value to insert
     */
    private void insertOptionWithEqualSign(String optionName, String optionValue)
        throws CommandValidationException
    {
        if (checkOptionIsBoolean(optionName))
        {
            //if option is a boolean then the value must be true or false
            if (optionValue!=null &&
                (optionValue.compareToIgnoreCase(TRUE)==0 ||
                 optionValue.compareToIgnoreCase(FALSE)==0))
            {
                optionsMap.addCLValue(optionName, optionValue);
            }
            else
                throw new CommandValidationException(getLocalizedString("OptionIsBoolean",
                                                     new Object[] {optionName} ));
        }    
        else
            optionsMap.addCLValue(optionName, optionValue);
    }

    
    /** 
     *  Insert default option to the long options list
     */
    private void insertDefaultOptions()
    {
        final Vector allOptions = validCommand.getOptions();
        final Map<String, String> clOptions = optionsMap.getCLOptions();
        if (allOptions != null) 
        {
            for (int ii=0; ii<allOptions.size(); ii++) 
            {
                final ValidOption currentOption = (ValidOption)allOptions.get(ii);
                if (currentOption != null &&
                    !clOptions.containsKey(currentOption.getName()) &&
                    !validCommand.hasDeprecatedOption(currentOption) &&
                    currentOption.getDefaultValue()!=null)
                {
                    CLILogger.getInstance().printDebugMessage(
					    "**** insert Default Options " +
                        currentOption.getName() + "  " + 
					    currentOption.getDefaultValue());
                    optionsMap.addDefaultValue(currentOption.getName(), 
                                    currentOption.getDefaultValue());
                }
            }
        }
    }


    /**
     *
     */
    private void insertPrefFileOptions()
    {
        try{
            String homedir = System.getProperty("user.home");
            CLIDescriptorsReader cdr = CLIDescriptorsReader.getInstance();
            String filename = cdr.getEnvironmentFileName();
            String envPrefix = cdr.getEnvironmentPrefix();
            FileInputStream prefsFile = new
                            FileInputStream(homedir+File.separator+filename);
            Properties p = new Properties();
            p.load(prefsFile);
            final Vector allOptions = validCommand.getOptions();
            for (int ii=0; ii<allOptions.size(); ii++)
            {
                final ValidOption currentOption = (ValidOption)allOptions.get(ii);
                final String optionName = currentOption.getName();
                String envVarName = envPrefix+optionName.toUpperCase();
                String value = p.getProperty(envVarName);
                if(!optionsMap.containsName(optionName) && 
                    !isEnvironmentOptionExists(currentOption.getName()) &&
                    (value != null))
                {
                    optionsMap.addPrefValue(optionName,  value);
                }
            }
        }
        catch(IOException e){
                //ignore
        }
    }
    
    
    /** 
     *   Insert environment option to the long options list
     */
    private void insertEnvironmentOptions()
    {
        final HashMap envOptions = CommandEnvironment.getInstance().getEnvironments();
        final Map<String, String> clOptions = optionsMap.getCLOptions();
        final Vector allOptions = validCommand.getOptions();
        if (envOptions != null) 
        {
            for (int ii=0; ii<allOptions.size(); ii++) 
            {
                final ValidOption currentOption = (ValidOption)allOptions.get(ii);
                final String optionName = currentOption.getName();
                if (currentOption != null &&
                    !clOptions.containsKey(currentOption.getName()) &&
                    envOptions.containsKey(optionName))
                {
                    CLILogger.getInstance().printDebugMessage(
					    "**** insert Environment Options " +
                        optionName + "  " + 
					    (String)envOptions.get(optionName) );
                    optionsMap.addEnvValue(optionName, (String)envOptions.get(optionName));
                }
            }
        }
    }


    /** 
     *   Gets all the valid options which are set in the environment for a given 
     *   ValidCommand
     */
    public Map getValidEnvironmentOptions()
    {
        final Map envOptions = CommandEnvironment.getInstance().getEnvironments();
        Map<String, String> validEnvOptions = new HashMap<String, String>();
        final Vector allOptions = validCommand.getOptions();
        if (envOptions != null) 
        {
            for (int ii=0; ii<allOptions.size(); ii++) 
            {
                final ValidOption currentOption = (ValidOption)allOptions.get(ii);
                final String optionName = currentOption.getName();
                if (currentOption != null &&
                    envOptions.containsKey(optionName))
                {
                    CLILogger.getInstance().printDebugMessage(
					    "**** valid Environment Options " +
                        optionName + "  " + 
					    (String)envOptions.get(optionName) );
                    validEnvOptions.put(optionName, (String)envOptions.get(optionName));
                }
            }
        }
        return validEnvOptions;
    }


    /** 
     *   Checks to see if the value for this environment option is set
     *   Used in insertDefaultOptions(), dont want to insert default if the env.
     *   is set.
     */
    public boolean isEnvironmentOptionExists(String optionName)
    {
        final HashMap envOptions = CommandEnvironment.getInstance().getEnvironments();
        if (envOptions.containsKey(optionName))
            return true;
        else
            return false;
    }


    /**
     *  This method search for the alternative options and replace it with the
     *  actual option name.
     */
    private void replaceAlternativeOptions()
    {
        final Vector<String> optionNames = new Vector<String>(optionsMap.nameSet());
        
        for (int ii=0; ii<optionNames.size(); ii++) 
        {
            final String optionName = (String)optionNames.get(ii);
            if (validCommand.hasAlternateDeprecatedOption(optionName))
            {
                //set the value of actual option and remove the alternate option
                //from the options list.
                
                final String alternateOptionName = 
                validCommand.getAlternateDeprecatedOption(optionName).getName();
 
                optionsMap.addCLValue(alternateOptionName, optionsMap.getOption(optionName));

                CLILogger.getInstance().printWarning(getLocalizedString("OptionDeprecatedUseNew",
                                                     new Object[] {optionName, alternateOptionName}));
                optionsMap.remove(optionName);
            }
        }

    }
    
    
    /** Insert the list of operand and the Operand variable
     *  @param operandIter - list of operand
     *  @return last index of the list
     */
    private int insertOperands(final ListIterator operandIter) 
        throws CommandValidationException
    {
        try 
        {
            //check if the first element is a "--"
            if (!((String)operandIter.next()).equals("--")) 
            {
                Operands.add((String)operandIter.previous());
                //call next to advance to the next element
                operandIter.next();
            }
            
            while (operandIter.hasNext()) 
            {
                Operands.add((String)operandIter.next());
            }
        }
        catch (NoSuchElementException nsee) 
        {
            throw new CommandValidationException(nsee);
        }
        return operandIter.nextIndex();
    }
    
    
    /** find long option name from short option
     *  @param ValidCommand object
     *  @param shortoption string
     *  @return the long option string
     */
    private String findOptionName(final char shortOption)
        throws CommandValidationException 
    {
        //search in required and valid options list
        final Vector allOptions = validCommand.getOptions();
        if (allOptions != null) 
        {
            for (int ii=0; ii<allOptions.size(); ii++) 
            {
                Vector shortOptions = 
                ((ValidOption)allOptions.get(ii)).getShortNames();
                
                if (shortOptions.contains(String.valueOf(shortOption))) 
                {
                    return ((ValidOption)allOptions.get(ii)).getName();
                }
            }
        }
        throw new CommandValidationException(getLocalizedString("NoSuchOption",
                                             new Object[] {String.valueOf(shortOption)} ));
    }
    
    
    /** check if the option is a boolean value
     *  @param optionName - name of option
     *  @return true if this option is a boolean value
     */
    private boolean checkOptionIsBoolean(final String optionName) 
    {
        final ValidOption option = validCommand.getOption(optionName);
        if (option != null) 
        {
            if ((option.getType().compareToIgnoreCase(BOOLEAN)) == 0)
                return true;
            else
                return false;
        }
        else
        {
            return false;
        }
    }
    
    /** return the opposite of the default value for boolean type
     *  @param optionName - name of option
     *  @return the opposite of the default value for boolean type
     */
    /* // Due to the bug #6296862, this method is not being used.
    private String getDefaultBooleanValue(final String optionName) 
    {
        final ValidOption option = validCommand.getOption(optionName);
        if (option != null) 
        {
            if (option.getDefaultValue() !=null &&
                option.getDefaultValue().compareToIgnoreCase(TRUE)==0)
                return FALSE;
        }
        //should an exception be thrown here if there is no default
        //value for boolean option?
        return TRUE;
    }
    */
    
    /**
     *  returns a Map with all the options in optionsMap
     *  @return optionsList
     */
    public Map getOptionsList() 
    {
        return optionsMap.getOptions();
    }

        /**
         * returns OptionsMap object
         */
    public OptionsMap getOptionsMap()
    {
        return optionsMap;
    }
    
    
    
    /** gets the operandsList
     *  @return operadsList
     */
    public Vector getOperands() 
    {
        return Operands;
    }

    /** 
     *  returns the localized string from framework's LocalStrings.properties.
     *  Calls the LocalStringsManagerFactory.getFrameworkLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     *  @param toInsert, the strings to be inserted in the placeholders
     */
    protected String getLocalizedString(String key, Object[] toInsert)
    {
        try
        {
            final LocalStringsManager lsm = 
            LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            return lsm.getString(key, toInsert);
        }
        catch (CommandValidationException cve)
        {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
    }
    
    
    public String toString() 
    {
        return "\n**********\nname = " + commandName +
        "\nOptions = " + optionsMap +
        "\nOperands = " + Operands + "\n**********\n";
    }
    
}
