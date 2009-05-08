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

import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import java.util.Collections;
/**
 *  <code>Command<code> object is the super Base class for all the commands.
 *  This is a generic Command Class which would fit 
 *  into any Command Line Interface (CLI) module.
 *  @version  $Revision: 1.9 $
 */
abstract public class Command implements ICommand
{

    private static final char REPLACE_START_CHAR = '{';    
    private static final char REPLACE_END_CHAR = '}';    
    private static final char VARIABLE_START_CHAR = '$';    
    private static final char OPERAND_START_CHAR = '#';    
    private static final String PATTERN_MATCHING = "\\{([\\$\\#])(\\w[\\w\\-]*)\\}";
        //private static final String PATTERN_MATCHING = "\\{([\\$\\#])(\\w+)\\}";
    
    protected String name = null;
    protected OptionsMap optionsMap;
    protected Vector operands;
    protected String usageStr = null;
    protected Hashtable properties;
    private   Object   caller; // used to get info needed by start-domain from AsadminMain
    
    /** Creates new Command */
    public Command() 
    {
        operands = new Vector();
        optionsMap = new OptionsMap();
    }
    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    abstract public void runCommand() 
        throws CommandException, CommandValidationException;

    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of 
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    abstract public boolean validateOptions() throws CommandValidationException;

    
    /**
     *  Gets the name of the command
     *  @return String the name of the command
     */
    public String getName() 
    {
        return name;
    }
    
    /**
     *  Sets the name of the command
     *  @param String the name of the command
     */
    public void setName(String name) 
    {
        this.name = name;
    }
    
    /**
     *  Gets the list of operands of this command
     *  @return Vector the list of operands.
     */
    public Vector getOperands() 
    {
        return operands;
    }
    
    /**
     *  Sets the list of operands for this command
     *  @param Vector the list of operands.
     */
    public void setOperands(Vector operands) 
    {
        this.operands = operands;
    }

    
    /**
     *  Gets the list of options for this Command
     *  @return Vector List of options for this command
     */
    public Map getOptions() 
    {
        return this.optionsMap.getOptions();
    }
    
    
    /**
     *  Gets the list of options read from command line for this Command
     *  @return Map of options for this command
     */
    public Map getCLOptions() 
    {
        return this.optionsMap.getCLOptions();
        
    }
    
    /**
     *  Gets the list of options read from environment for this Command
     *  @return Map of options for this command
     */
    public Map getENVOptions() 
    {
        return this.optionsMap.getEnvOptions();
    }


    /**
     *  Sets OptionsMap
     *  @param options List of options for this command
     */
    public void setOptionsMap(OptionsMap options) 
    {
        this.optionsMap = options;
    }
    
    
    /**
     *  Finds the option with the give name
     *  @return Option return option if found else return null
     */
    public String getOption(String optionName) 
    {
        return this.optionsMap.getOption(optionName);
    }
    
    
    /**
     *  Finds the option with the give name
     *  @return Option return option if found else return null
     */
    public String getCLOption(String optionName) 
    {
        Map<String, String> map = this.optionsMap.getCLOptions();
        return map.get(optionName);
    }
    
    
    /**
     *  Finds the option with the give name
     *  @return Option return option if found else return null
     */
    public String getENVOption(String optionName) 
    {
        Map<String, String> map = this.optionsMap.getEnvOptions();
        return map.get(optionName);
    }

    
    /**
     *  Finds the option with the give name
     *  Options set bo "Other" are usually set by the command module
     *  @return Option return option if found else return null
     */
    public String getOtherOption(String optionName) 
    {
        Map<String, String> map = this.optionsMap.getOtherOptions();
        return map.get(optionName);
    }

    
    
    /**
     *  Sets the option value for the give name
     *  @param optionName name of the option
     *  @param optionValue value of the option
     */
    public void setOption(String optionName, String optionValue) 
    {
        this.optionsMap.addOptionValue(optionName, optionValue);
    }


    /**
     *  Finds the option with the give name
     *  @return boolean return boolean type of the option value
     */
    protected boolean getBooleanOption(String optionName) 
    {
        return Boolean.valueOf(getOption(optionName)).booleanValue();
    }
    
    
    /**
     *  Finds the option with the give name
     *  @return Option return option if found else return -1
     */
    protected int getIntegerOption(String optionName) 
    {
        //assert
        assert(!optionNameExist(optionName));
        return (Integer.valueOf(getOption(optionName)).intValue());
    }

    /*
     * return the reference to the calling class which was saved during
     * construction.
     * Start-domain currently is the only caller
     */

    protected Object getCaller() {
        return caller;
    }

    void setCaller(Object caller) {
        this.caller = caller;
    }


    /**
     *  returns true if the option name exist in the options list
     *  @true if option name exist
     */
    private boolean optionNameExist(String optionName)
    {
        return this.optionsMap.containsName(optionName);
    }
    
    
    /**
     *  Gets the Usage text for this command
     *  @return String returns usage text for this command
     */
    public String getUsageText()
    {
        return usageStr;
    }
    
    /**
     *  Sets the Usage text for this command
     *  @param String usage-text for this command
     */
    public void setUsageText(String usageText)
    {
        this.usageStr = usageText;
    }

    /**
     *  Searches for the property with the specified key in this 
     *  properties list
     *  return the property
     */
    public Object getProperty(String key)
    {
        return properties.get(key);
    }


    /** 
     *  Sets the properties of the command
     *  @param properties, the list of properties for this command
     */
    public void setProperty(String key, Object value)
    {
        properties.put(key, value);
    }


    /**
     *  returns the properties list
     *  return the property
     */
    protected Hashtable getProperties(String key)
    {
        return properties;
    }


    /** 
     *  Sets the properties of the command
     *  @param properties, the list of properties for this command
     */
    protected void setProperties(Hashtable properties)
    {
        this.properties = properties;
    }


    /** 
     *  returns the localized string from the properties file as defined in
     *  the CommandProperties element of CLIDescriptor.xml file
     *  Calls the LocalStringsManagerFactory.getCommandLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     */
    protected String getLocalizedString(String key)
    {
        LocalStringsManager lsm = null;
        try
        {
            lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
        }
        catch (CommandValidationException cve)
        {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
        return lsm.getString(key);
    }


    /** 
     *  returns the localized string from the properties file as defined in 
     *  the CommandProperties element of CLIDescriptor.xml file
     *  Calls the LocalStringsManagerFactory.getCommandLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     *  @param toInsert, the strings to be inserted in the placeholders
     */
    protected String getLocalizedString(String key, Object[] toInsert)
    {
        LocalStringsManager lsm = null;
        try
        {
            lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
            return lsm.getString(key, toInsert);
        }
        catch (CommandValidationException cve)
        {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
    }


    /**
     *  Overrides the Object's toString() method
     *  @return String The string representation of Command
     */
    public String toString()
    {
        StringBuffer strbuf = new StringBuffer();

        strbuf.append(getName());
        final Map<String, String> clOptions = this.optionsMap.getOptions();

        Iterator optionNames = clOptions.keySet().iterator();
        while (optionNames.hasNext())
        {
            final String optionKey = (String) optionNames.next();
            strbuf.append(" --" + optionKey );
            final String optionVal = (String)clOptions.get(optionKey);
                //check if the value is boolean
            if (Boolean.TRUE.toString().equalsIgnoreCase(optionVal) ||
                Boolean.FALSE.toString().equalsIgnoreCase(optionVal) )
                strbuf.append("=");
            else
                strbuf.append(" ");
            strbuf.append(optionVal);
        }
        for (int ii=0; ii<operands.size(); ii++)
        {
            strbuf.append(" "+ operands.get(ii).toString());
        }
        return strbuf.toString();
    }


    /**
     *  this method replaces pattern with option or operand values
     *  @param replaceValue - value to replace
     *  @param regexp - regular expression pattern
     *  @return replaced string or null if could not replace string
     */
    public String replacePattern(String replaceValue)
        throws CommandException
    {
        if (replaceValue == null) return null;
        final Pattern patt = Pattern.compile(PATTERN_MATCHING);
        final Matcher match = patt.matcher(replaceValue);
        String outstr = replaceValue;
        
        try 
        {
            if (match.find()) 
            {
                StringBuffer strbuf = new StringBuffer();
                do 
                {
                    String value = findPatternStringValue(match.group(1),
                                                          match.group(2));
                        //value = escapeTheEscape(value);
                    if (value == null) 
                        return value;
                    value = prepareStringForAppend(value);
                    match.appendReplacement(strbuf, value);
                    CLILogger.getInstance().printDebugMessage("strbuf = " + strbuf);
                } while (match.find());
                match.appendTail(strbuf);
                outstr = strbuf.toString();
            }
        }
        catch (java.lang.IllegalArgumentException iae)
        {
            try 
            {
                final LocalStringsManager lsm = 
                LocalStringsManagerFactory.getFrameworkLocalStringsManager();
                throw new CommandException(lsm.getString("RequireEscapeChar"), iae);
            }
            catch (CommandValidationException cve)
            {
                throw new CommandException(cve);
            }
        }
        catch (Exception e)
        {
            throw new CommandException(e);
        }
        return (outstr.length()<1)?null:outstr;
    }


    /**
     *  this method finds the pattern for option and operand
     *  option pattern starts with "$" while operand pattern starts with "#"
     *  once key is determined, the option or operand value is returned
     *  @param pattern
     *  @param key to get option or operand values
     *  @return option/operand value if could not find value, then empty
     *          string is returned
     */
    private String findPatternStringValue(String pattern, String key)
        throws CommandException
    {
        String value = null;
        try 
        {
            if (pattern.equals(String.valueOf(OPERAND_START_CHAR)))
            {
                if (operands.size() > 0)
                    value = (String)getOperands().get(Integer.parseInt(key)-1);
            }
            else if (pattern.equals(String.valueOf(VARIABLE_START_CHAR)))
                value = getOption(key);
        }
        catch(Exception e)
        {
            throw new CommandException(e);
        }
        // (bug 6363010), temporary fix, return null and make sure this isn't 
        //  propagated into the attributeList to the backend
        //return (value==null)?"":value;
        return value;
    }


    /**
     *  this method is a hack 
     *  to add escape character to each esacpe chars encounters except 
     *  the escape character is follow by a '$'.
     *  match.appendReplacement will throw an IllegalArgumentException 
     *  if literal $ does not preceed by an escape char.
     *  the purpose for doing this is for the match.appendReplacement 
     *  which drops the escape character.
     */
    private String prepareStringForAppend(String str)
    {
        final String strTmp = escapeTheEscape(str);
        return addEscapeToLiteral(strTmp);
    }


    /**
     *  This method adds escape character to each escape chars encounters 
     *  the purpose for doing this is is that match.appendReplacement 
     *  drops the escape character.
     *  @param strToEscape - to the string to check for escape characters
     *  @returns the string with the esacpe characters escaped
     */
    private String escapeTheEscape(String strToEscape)
    {
        StringBuffer strbuf = new StringBuffer();
        int previousIndex =0;
        int index =strToEscape.indexOf("\\");
        while (index > -1)
        {
            if (index<strToEscape.length()-1 && strToEscape.charAt(index+1) == '$')
                strbuf.append(strToEscape.substring(previousIndex, index));
            else
                strbuf.append(strToEscape.substring(previousIndex, index)+"\\\\");
            previousIndex = index+1;
            index = strToEscape.indexOf("\\", index+1);
        }
        strbuf.append(strToEscape.substring(previousIndex));
        return strbuf.toString();
    }


    /**
     *  This method adds escape character preceding the '$' character.
     *  The reason for doing this is because match.appendReplacement 
     *  will throw an IllegalArgumentException if the literal '$'
     *  does not precede by an escape character.
     *  The java api class Matcher, uses the regular expression
     *  '$<number>' as a group reference.  So if it encounters '$<non-numeric>
     *  it'll throw an IllegalArgumentException.  In order to by pass this
     *  exception, the literal '$' must precede with an escape character.
     *  @param strToEscape - to the string to check for '$'
     *  @returns the string with the esacpe character added to '$'
     */
    private String addEscapeToLiteral(String strToAdd)
    {
        StringBuffer strbuf = new StringBuffer();
        int previousIndex =0;
        int index = strToAdd.indexOf('$');
        while (index > -1)
        {
            strbuf.append(strToAdd.substring(previousIndex, index)+"\\$");
            previousIndex = index+1;
            index = strToAdd.indexOf('$', index+1);
        }
        strbuf.append(strToAdd.substring(previousIndex));
        return strbuf.toString();

    }


    /**
     * returns the index of the delimeter string in the search string.
     * @param searchStr the string to be searched
     * @param delimeterStr the delimeter string to be searched in the searchStr
     * @param fromIndex the delimeter to be searched at the specified location
     * @return delimeterIndex starting index of the delimeter in search string
     */
    protected int getDelimeterIndex(String searchStr, String delimeter,
                                    int fromIndex)
    {
        return searchStr.indexOf(delimeter, fromIndex);
    }
    
    /**
     * Get the Properties of our environment.
     * In V2 these were set in System.
     * 
     * @return an unmodifiable Map of the properties
     */
    public final Map<String,String> getSystemProperties()
    {
        return systemProps;
    }
    
    /**
     * Get a Property of our environment.
     * In V2 these were set in System.
     * @param name the name of the property
     * @return the value of the property or null if it doesn't exist.
     */
    public final String getSystemProperty(String name)
    {
        return systemProps.get(name);
    }
    
    private final static Map<String,String> systemProps = 
            Collections.unmodifiableMap(new ASenvPropertyReader().getProps());
}
