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
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.Serializable;

/**
   The <code>ValidCommand</code> object represents a valid command.
      @version  $Revision: 1.3 $
 */
public class ValidCommand implements ICommand, Serializable
{
    // name of the valid command
    private String name;
    // number of operands in this command
    private String numberOfOperands;
    // List of valid options
    private Vector validOptions;
    // list of required options
    private Vector requiredOptions;
    // list of deprecated options
    private Vector deprecatedOptions;
    // class name of this command
    private String className;
    // usage text for this command
    private String usageText = null;
    // List of properties
    private Hashtable properties;
    //default value for optional operand
    private String defaultOperand = null;
    
    // Regular expression of defaultoperand to be valid for number of operands
    private transient static final String DEFAULTOPERAND_QUANTIFIER_REGEXP = "[\\*\\?]";
    
    /** 
        Construct a vanila ValidCommand object
     */
    public ValidCommand()
    {
        validOptions = new Vector();
        requiredOptions = new Vector();
        deprecatedOptions = new Vector();
        properties = new Hashtable();
    }

   
    /** 
        Construct ValidCommand object with the given arguments.
	@param name the name of the valid command
	@param numberOfOperands number of operands in this command
	@param validOptions a list of validOptions
	@param requiredOptions a list of required options
   	@param deprecatedOptions a list of deprecated options
	@param usageText usage text for this command
	
    */
    public ValidCommand(String name, String numberOfOperands, 
                        Vector validOptions, Vector requiredOptions, 
                        Vector deprecatedOptions, String usageText)
    {
        this.name = name;
        this.numberOfOperands = numberOfOperands;
        this.validOptions = validOptions;
        this.requiredOptions = requiredOptions;
        this.deprecatedOptions = deprecatedOptions;
        this.usageText = usageText;
    }

    /**
        Get the name of the command
        @return the name of the Valid Command
     */
    public String getName()
    {
        return name;
    }

    /**
        Sets the name of the Valid Command
	@param name name of the command
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
        Returns the number of operands in this Command
        @return the number of operands
     */
    public String getNumberOfOperands()
    {
        return numberOfOperands;
    }

   /**
        Get the default operand for this command
        defaultoperand is valid if numberofoperand attribute is
        either * or ?.  If the numberofoperand is finite or "+"
        then this attribute is ignored.
    */
    public String getDefaultOperand()
    {
        return this.defaultOperand;
    }


    /**
        Sets the number of operands for this command
        @param numberOfOperands the number of operands
     */
    public void setNumberOfOperands(String numberOfOperands)
    {
        this.numberOfOperands = numberOfOperands;
    }

    /**
        Sets the default operand for this command
        defaultoperand is valid if numberofoperand attribute is
        either * or ?.  If the numberofoperand is finite or "+"
        then this attribute is ignored and set to nil.
        @param defaultOperand
     */
    public void setDefaultOperand(String defaultOperand)
    {
        if (defaultOperand != null &&
            numberOfOperands.matches(DEFAULTOPERAND_QUANTIFIER_REGEXP)) 
        {
            this.defaultOperand = defaultOperand;
        }
        else
            this.defaultOperand = null;
    }


    /**
        Returns the list of valid options for this command
        @return the list of valid options
     */
    public Vector getValidOptions()
    {
        return validOptions;
    }

    /**
        Sets the list of valid options for this command
        @param validOptions the valid options to set
     */
    public void setValidOptions(Vector validOptions)
    {
        this.validOptions = validOptions;
    }

    /**
        Gets the list of required options for this command
        @return list of required options
     */
    public Vector getRequiredOptions()
    {
        return requiredOptions;
    }

    /**
        Sets the list of Required options for this command
        @param requiredOptions the list of required options to set
     */
    public void setRequiredOptions(Vector requiredOptions)
    {
        this.requiredOptions = requiredOptions;
    }

    /**
        Gets the list of deprecated options for this command
        @return list of deprecated options
     */
    public Vector getDeprecatedOptions()
    {
        return deprecatedOptions;
    }

    /**
        Sets the list of Deprecated options for this command
        @param deprecatedOptions the list of deprecated options to set
     */
    public void setDeprecatedOptions(Vector deprecatedOptions)
    {
        this.deprecatedOptions = deprecatedOptions;
    }

    /**
        Returns the class name for this command
        @return the class name
     */
    public String getClassName()
    {
        return className;
    }
    
    /**
        Sets the class name for this command
        @param className the class name to set
     */
    public void setClassName(String className)
    {
        this.className = className;
    }
    
    /**
        Return the usage text
        @return the usage text of the command
     */
    public String getUsageText()
    {
        return usageText;
    }
    
    /*
        set the usage text
        @param usageText the usage text to set
     */
    public void setUsageText(String usageText)
    {
        this.usageText = usageText;
    }

    /**
        Returns the option which matches in the options list
        @param longOptionName the long option name
        @return the ValidOption object, return null if option
	        does not exist.
     */
    public ValidOption getOption(String longOptionName)
    {
        Vector allOptions = this.getOptions();
        if (longOptionName != null) {
            for (int ii=0; ii<allOptions.size(); ii++)
            {
                ValidOption option = (ValidOption)allOptions.get(ii);
                if ((option.getName()).equals(longOptionName))
                    return option;
            }
        }
        return null;
    }
    
    /**
        Returns the alternate deprecated option
        @param optionName the deprecated option name
        @return the ValidOption object, return null if option
	        does not exist.
     */
    public ValidOption getAlternateDeprecatedOption(String optionName)
    {
        Vector allOptions = new Vector(validOptions);
        allOptions.addAll(requiredOptions);
        boolean hasAltDeprecatedOption = false;
        ValidOption altDeprecatedOption = null;
        for (int i = 0; (i < allOptions.size()) && !hasAltDeprecatedOption; i++)
        {
            ValidOption validOption = (ValidOption)allOptions.get(i);
            final String deprecatedOptionStr = validOption.getDeprecatedOption();
            if ((deprecatedOptionStr != null) && 
                (deprecatedOptionStr.equals(optionName)))
            {
                hasAltDeprecatedOption = true;
                altDeprecatedOption = validOption;
            }
        }
        return altDeprecatedOption;
    }
    
    /**
        Gets all the options (required and valid)
        @return the list of valid options
     */
    public Vector getOptions()
    {
        Vector options = new Vector(validOptions);
        options.addAll(requiredOptions);
        options.addAll(deprecatedOptions);
        return options;
    }
    
    /**
      Gets all the properties of the command
      @return the list of properties
     */
    public Hashtable getProperties()
    {
        return properties;
    }
    
    /**
      Searches for the property with the specified key in this properties list
      @return the property
     */
    public Object getProperty(String key)
    {
        return properties.get(key);
    }

    /** 
     Sets the properties of the command
     @param key
     @param value
     */
    public void setProperty(String key, Object value)
    {
        properties.put(key, value);
    }
    
    /**
        Add ValidOption object to the valid option list
        @param option valid option list
     */
    public void addValidOption(ValidOption option)
    {
        if ((option != null) && !hasValidOption(option.getName()))
        {
            validOptions.add(option);
        }
    }

    /**
        Adds the required option to the required options list
        @param option option to add to the required
     */
    public void addRequiredOption(ValidOption option)
    {
        if ((option != null) && !hasRequiredOption(option.getName()))
        {
            requiredOptions.add(option);
        }
    }

    /**
        Adds the deprecated option to the deprecated options list
        @param option option to add to the deprecated
     */
    public void addDeprecatedOption(ValidOption option)
    {
        if ((option != null) && !hasDeprecatedOption(option.getName()))
        {
            deprecatedOptions.add(option);
        }
    }

    /**
        Deletes the option from the options list
        @param option option to delete from the list
     */
    public void deleteOption(ValidOption option)
    {
        if (option != null)
        {
            validOptions.remove(option);
        }
    }

    /**
        Checks if the option exists
        @param optionName The ValidOption name 
        @return true if the option exist
     */
    public boolean hasValidOption(String optionName)
    {
        boolean hasValidOption = false;
        if (optionName != null) {
            for (int i = 0; (i < validOptions.size()) && !hasValidOption; i++)
            {
                if (((ValidOption)validOptions.get(i)).getName().equals(optionName))
                    hasValidOption = true;
            }
        }
    	return hasValidOption;
    }

    /**
     * Checks if the option exists
     * @param option The ValidOption object 
     * @return true if the option exist
     */
    public boolean hasValidOption(ValidOption option)
    {
    	return validOptions.contains(option);
    }
    
    /**
     * Checks if the required option exists
     * @param optionName The required option name
     * @return true if the option exist
     */
    public boolean hasRequiredOption(String optionName)
    {
        boolean hasRequiredOption = false;
        for (int i = 0; (i < requiredOptions.size()) && !hasRequiredOption; i++)
        {
            if (((ValidOption)requiredOptions.get(i)).getName().equals(optionName))
                hasRequiredOption = true;
        }
    	return hasRequiredOption;
    }

    /**
     * Checks if the required option exists
     * @param option The RequiredOption object 
     * @return true if the option exist
     */
    public boolean hasRequiredOption(ValidOption option)
    {
    	return requiredOptions.contains(option);
    }
    
    /**
     * Checks if the deprecated option exists
     * @param optionName The deprecated option name
     * @return true if the option exist
     */
    public boolean hasDeprecatedOption(String optionName)
    {
        boolean hasDeprecatedOption = false;
        for (int i = 0; (i < deprecatedOptions.size()) && !hasDeprecatedOption; i++)
        {
            if (((ValidOption)deprecatedOptions.get(i)).getName().equals(optionName))
                hasDeprecatedOption = true;
        }
    	return hasDeprecatedOption;
    }

    /**
     * Checks if the deprecated option exists
     * @param option The DeprecatedOption object 
     * @return true if the option exist in the deprecatedOptions list
     */
    public boolean hasDeprecatedOption(ValidOption option)
    {
    	return deprecatedOptions.contains(option);
    }
    
    /**
     * Checks if the alternate deprecated option exists
     * @param optionName The deprecated option name
     * @return true if the option exist
     */
    public boolean hasAlternateDeprecatedOption(String optionName)
    {
        Vector allOptions = new Vector(validOptions);
        allOptions.addAll(requiredOptions);
        boolean hasAltDeprecatedOption = false;
        for (int i = 0; (i < allOptions.size()) && !hasAltDeprecatedOption; i++)
        {
            String deprecatedOptionStr = 
                       ((ValidOption)allOptions.get(i)).getDeprecatedOption();
            if ((deprecatedOptionStr != null) && 
                (deprecatedOptionStr.equals(optionName)))
            {
                hasAltDeprecatedOption = true;
            }
        }
    	return hasAltDeprecatedOption;
    }

    
    /**
     Finds if a property by name exists in the list
     @param propertyName name of the property to find in the list
     @return true if the property is found
     */
    public boolean hasProperty(String propertyName)
    {
        return properties.containsKey(propertyName);
    }

    
    /**
     *  Calls ReplaceOptionsList to relace the options with replaceOption
     *  in validOptions, requiredOptions and deprecatedOptions.
     **/
    public void replaceAllOptions(ValidOption replaceOption)
    {
        if (replaceOption != null)
        {
            final String replaceOptionName = replaceOption.getName();
            if (hasValidOption(replaceOptionName))
            {
                replaceOptionsList(validOptions, replaceOption);
            }
            if (hasRequiredOption(replaceOptionName))
            {
                replaceOptionsList(requiredOptions, replaceOption);
            }
            if (hasDeprecatedOption(replaceOptionName))
            {
                replaceOptionsList(deprecatedOptions, replaceOption);
            }
        }
    }

    
    /**
     *  Search in the options list and replace it with replaceOption object. 
     **/
    private void replaceOptionsList(Vector optionsList, ValidOption replaceOption)
    {
        final String replaceOptionName = replaceOption.getName();
        for (int ii = 0; ii<optionsList.size(); ii++)
        {
            try {
                if (((ValidOption)optionsList.get(ii)).getName().equals(replaceOptionName))
                {
                    if (((ValidOption)optionsList.get(ii)).hasDeprecatedOption()) {
                        final String deprecatedOption=((ValidOption)optionsList.get(ii)).getDeprecatedOption();
                        replaceOption.setDeprecatedOption(deprecatedOption);
                    }
                    
                    optionsList.set(ii, replaceOption);
                    break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return;
    }
    
    

    
    /**
     Returns the toString()
     @return String the object in the string format
     */
    public String toString()
    {
        String validOptionsStr = "{";
        for (int i = 0; i < validOptions.size(); i++)
        {
            validOptionsStr += validOptions.get(i).toString() + ",";
        }
        validOptionsStr += "}";
       
        String requiredOptionsStr = "";
        for (int i = 0; i < requiredOptions.size(); i++)
        {
            requiredOptionsStr += requiredOptions.get(i).toString() + ",";
        }
        requiredOptionsStr += "}";

        String deprecatedOptionsStr = "";
        for (int i = 0; i < deprecatedOptions.size(); i++)
        {
            deprecatedOptionsStr += deprecatedOptions.get(i).toString() + ",";
        }
        deprecatedOptionsStr += "}";

        String propertiesStr = "{";
        for (Enumeration propertyNames = properties.keys() ; propertyNames.hasMoreElements() ;) 
        {
            String name = (String) propertyNames.nextElement();
            Vector value = (Vector) properties.get(name);
            propertiesStr += "|" + name + " , " + value;
        }
        propertiesStr += "}";
         
        return (name + " " + numberOfOperands + " | " + validOptionsStr + 
                " | " + requiredOptionsStr + " | " +
                " | " + deprecatedOptionsStr + " | " + usageText + " " + 
                propertiesStr);
    }
}
