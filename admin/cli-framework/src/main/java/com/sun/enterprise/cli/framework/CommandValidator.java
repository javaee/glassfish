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

import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

/**
 *    The <code>CommandValidator</code> object validates the command line
 *    against the CLI Specification
 *    @version  $Revision: 1.5 $
 */
public class CommandValidator {
    // Regular expression for command name
    private static final String COMMAND_NAME_REGEXP = "^[a-z\\-][a-z0-9\\-\\_\\ ]*$";
    
    // Regular expression for option name
    private static final String OPTION_NAME_REGEXP = "^\\w[-\\w]*";
    
    // Regular expression for number of operands in quantifier
    private static final String NUMBER_OF_OPERANDS_QUANTIFIER_REGEXP = "^[\\*\\?\\+]";
    
    // Regular expression for number of operands in numbers
    private static final String NUMBER_OF_OPERANDS_REGEXP = "^\\+?\\d+";
    
    // Regular expression for number of operands in inclusion
    private static final String NUMBER_OF_OPERANDS_INCLUSION_REGEXP = "^\\d+\\-\\d+";
    
    // BOOLEAN STRING
    private static final String BOOLEAN = "boolean";
    
    // TRUE STRING
    private static final String TRUE = "true";
    
    // FALSE STRING
    private static final String FALSE = "false";
    
    //PLUS STRING
    private static final String PLUS = "+";
    
    //QUESTION_MARK STRING
    private static final String QUESTION_MARK = "?";
    
    //DASH STRING
    private static final String DASH = "-";
    
    /** Creates a new Command Validator */
    public CommandValidator() 
    {
    }
    
    
    /** Validates the options and operands entered in the command line.
     *  @param validCommand - the valid command from command specification
     *  @param optionsList - the list of options in the command line
     *  @param operandsList - the list of operands in the command
     *  @throws CommandValidationException if there is an error during
     *          validation
     */
    public void validateCommandAndOptions(final ValidCommand validCommand,
                                          final Map optionsList,
                                          final Vector operands)
        throws CommandValidationException, InvalidCommandException 
    {
        if (validCommand == null || optionsList == null || operands == null) 
        {
            throw new CommandValidationException(getLocalizedString("CouldNotValidateCommand",
                                                                    null));
        }
        
        verifyCommandName(validCommand.getName());

        verifyOptionsAreValid(validCommand, optionsList);
        
        verifyRequiredOptions(validCommand.getRequiredOptions(), optionsList, 
                              validCommand.getName());
        
        verifyDeprecatedOptions(validCommand.getDeprecatedOptions(), 
                                optionsList, validCommand.getName());
        
        //check for boolean required options
        verifyBooleanOptions(validCommand.getRequiredOptions(), optionsList);

        //check for the number of operands
        verifyNumberOfOperands(validCommand.getNumberOfOperands(),
                               operands.size());
    }
    
    
    /** Verify that the command name must be in lower-cose and that it
     *  must be alpha-numeric.
     *  @param commandName - name of command
     *  @return true if command name is valid
     *  @throw CommandValidationException if command name is invalid
     */
    private boolean verifyCommandName(final String commandName)
        throws InvalidCommandException 
    {
        if (!commandName.matches(COMMAND_NAME_REGEXP))
        {
            throw new InvalidCommandException(commandName);
        }
        return true;
    }
    
    
    /** Verify that the option name is valid.  Option name must be
     *  alpha-numeric with either a "-" between words.
     *  @param optionName - name of option
     *  @return true if option name is valid otherwise return false.
     */
    private boolean verifyOptionName(final String optionName) 
    {
        return optionName.matches(OPTION_NAME_REGEXP);
    }
    
    
    /** verify if the deprecated options are entered in the command line
     *  @param deprecatedOptionsList - list of deprecated options
     *  @param optionsList - list of options enterd in command line
     *  @return true if all deprecated options are entered
     *  @throws CommandValidationException if deprecated option is not
     *          in command line.
     */
    private boolean verifyDeprecatedOptions(final Vector deprecatedOptionsList,
                                            final Map optionsList, 
                                            final String commandName)
        throws CommandValidationException 
    {
        for (int ii=0; ii<deprecatedOptionsList.size(); ii++) 
        {
            final String optionName = 
            ((ValidOption)deprecatedOptionsList.get(ii)).getName();
            if (!verifyOptionName(optionName))
            {
                throw new CommandValidationException(getLocalizedString("InvalidOption", 
                                                     new Object[] {optionName,
                                                     commandName}));
            }
            
            //check if deprecated option name is in the optionsList
            if (optionsList.containsKey(optionName)) 
            {
                CLILogger.getInstance().printWarning(getLocalizedString("OptionDeprecated",
                                                     new Object[] {optionName}));
            }
        }
        return true;
    }
    
    
    /** verify if the required options are entered in the command line
     *  @param requiredOptionsList - list of required options
     *  @param optionsList - list of options enterd in command line
     *  @return true if all required options are entered
     *  @throws CommandValidationException if required option is not
     *          in command line.
     */
    private boolean verifyRequiredOptions(final Vector requiredOptionsList,
                                          final Map optionsList, 
                                          final String commandName)
        throws CommandValidationException 
    {
        for (int ii=0; ii<requiredOptionsList.size(); ii++) 
        {
            final String optionName = 
            ((ValidOption)requiredOptionsList.get(ii)).getName();
            if (!verifyOptionName(optionName))
            {
                throw new CommandValidationException(getLocalizedString("InvalidOption", 
                                                     new Object[] {optionName,
                                                     commandName}));
            }
            
            //check if required option name is in the optionsList and if it has a
                //default value
            if (!optionsList.containsKey(optionName) &&
                !((ValidOption)requiredOptionsList.get(ii)).hasDefaultValue())
            {
                throw new CommandValidationException(getLocalizedString("OptionIsRequired",
                                                     new Object[] {optionName}));
            }
        }
        return true;
    }
    
    
    /** verify options from the command line are valid
     *  also verifies that option values is not null.   
     *  @param validCommand - the valid command object to check for valid options
     *  @param optionsList - list of options entered in command line
     *  @return true if all options are valid
     *  @throws CommandValidationException if options is not valid
     */
    private boolean verifyOptionsAreValid(final ValidCommand validCommand,
                                          final Map optionsList)
        throws CommandValidationException {

        final Iterator iter = (optionsList.keySet()).iterator();
        while (iter.hasNext()) {
            final String optionName = (String)iter.next();
            if (!(validCommand.hasValidOption(optionName) ||
                  validCommand.hasRequiredOption(optionName) ||
                  validCommand.hasDeprecatedOption(optionName))) {
                throw new CommandValidationException(getLocalizedString("InvalidOption",
                                                     new Object[] {optionName,
                                                     validCommand.getName()}));
            }
            //verify that the option value is not null
            //if null then throw an CommandValidationException
            if (optionsList.get(optionName) == null)
                throw new CommandValidationException(getLocalizedString("OptionValueNotSpecified",
                          new Object[] {optionName} ));
        }
        return true;
    }


    /** verify that options with type boolean contains the value true or false
     *  @param baseOptionsList - base options list to check if options is boolean
     *  @param optionsList - list of options entered in command line
     *  @return true if options with type boolean contains true or false
     *  @throws CommandValidationException if boolean options contains contains
     *          values other than true or false.
     */
    private boolean verifyBooleanOptions(final Vector baseOptionsList,
                                         final Map optionsList)
        throws CommandValidationException 
    {
        for (int ii=0; ii<baseOptionsList.size(); ii++) 
        {
            final String optionType = ((ValidOption)baseOptionsList.get(ii)).getType();
            final String optionName = ((ValidOption)baseOptionsList.get(ii)).getName();
            if (optionType.compareToIgnoreCase(BOOLEAN) == 0 &&
                optionsList.containsKey(optionName)) 
            {
                //if the option type is boolean, the value should be
                //either true or false
                final String optionValue = 
                ((String)optionsList.get(optionName)).trim();
                if (optionValue == null ||
                    !(optionValue.compareToIgnoreCase(TRUE) == 0 ||
                      optionValue.compareToIgnoreCase(FALSE) == 0 ) ) 
                {
                    throw new CommandValidationException(getLocalizedString(
                                                             "OptionIsBoolean", 
                                                             new Object[] {optionName}));
                }
                
            }
        }
        return true;
    }
    
    
    /** verify the number of operands is compliant with CLI specification
     *  the number of operands should follow the following convention:
     *      * - 0 or more
     *      ? - 0 or 1
     *      + - 1 or more
     *      number - the number of operand
     *      +number - the number of operand should be equal to or
     *                greater than the number
     *      number1-number2 - the number of operand should be greater than
     *                        or equal to number1 and less than or equal to
     *                        number2
     *  @param numberOfOperand - the number of operand specified in CLI spec
     *  @param operandSize - the exact number of operands
     *  @return true if the operand follow the convention
     *  @throws CommandValidationException if the operand does not follow
     *          the convention
     */
    private boolean verifyNumberOfOperands(final String numberOfOperands,
                                           final int operandSize)
        throws CommandValidationException 
    {
        if (numberOfOperands.matches(NUMBER_OF_OPERANDS_QUANTIFIER_REGEXP)) 
        {
            //if numberofoperand is ?, then the operand size should be 0 or 1
            if (numberOfOperands.equals(QUESTION_MARK) &&
                (operandSize < 0 || operandSize > 1))
            {
                throw new CommandValidationException(getLocalizedString("ZeroOrOneOperand", null));
            }
            else if (numberOfOperands.equals(PLUS) && !(operandSize >= 1) )
            {
                throw new CommandValidationException(getLocalizedString("GreaterThanOneOperand", null));
            }
        }
        else if (numberOfOperands.matches(NUMBER_OF_OPERANDS_REGEXP)) 
        {
            if (numberOfOperands.startsWith(PLUS) &&
                Integer.parseInt(numberOfOperands.substring(1)) > operandSize)
            {
                throw new CommandValidationException(getLocalizedString(
                                                     "GreaterOrEqualToOperand",
                                                     new Object[] {
                                                     numberOfOperands.substring(1)}));
            }
            else if (!numberOfOperands.startsWith(PLUS) && 
                     Integer.parseInt(numberOfOperands) == 1 &&
                     operandSize < 1)
            {
                throw new CommandValidationException(getLocalizedString("OperandIsRequired", null));
            }
            else if (!numberOfOperands.startsWith(PLUS) &&
                     Integer.parseInt(numberOfOperands) != operandSize)
            {
                throw new CommandValidationException(getLocalizedString("EqualToOperand",
                                                     new Object[] {numberOfOperands}));
            }
        }
        else if (numberOfOperands.matches(NUMBER_OF_OPERANDS_INCLUSION_REGEXP)) 
        {
            final int index = numberOfOperands.indexOf(DASH);
            final int min = Integer.parseInt(numberOfOperands.substring(0,index));
            final int max = Integer.parseInt(numberOfOperands.substring(index+1));
            if (min > operandSize || max < operandSize)
            {
                throw new CommandValidationException(getLocalizedString(
                                                     "BetweenNumberOperand",
                                                     new Object[] {
                                                     String.valueOf(min), 
                                                     String.valueOf(max)}));
            }
        }
        else
        {
            throw new CommandValidationException(getLocalizedString(
                                                 "InvalidSytanxForNumberOfOperands",
                                                 new Object[] {numberOfOperands})); 
        }
        return true;
    }


    /** 
     *  returns the localized string from framework's LocalStrings.properties.
     *  Calls the LocalStringsManagerFactory.getFrameworkLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     *  @param toInsert, the strings to be inserted in the placeholders
     */
    private String getLocalizedString(String key, Object[] toInsert)
    {
        try
        {
            final LocalStringsManager lsm = 
            LocalStringsManagerFactory.getFrameworkLocalStringsManager();
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

    
}
