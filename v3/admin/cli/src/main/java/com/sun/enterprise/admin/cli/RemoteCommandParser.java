/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.NoSuchElementException;
import com.sun.enterprise.cli.framework.CommandValidationException;


/**
 * The <code>RemoteCommandParser</code> object is used to parse the
 * command line and verify that the command line is CLIP compliant.
 */
public class RemoteCommandParser 
{
    
    // Name of Command
    private String commandName = null;

    // List of Short Options from command-line argument
    private Map<String,String> optionsMap = new HashMap<String,String>();
    
    // Array of Operands from command-line argument
    private Vector<String> Operands = new Vector<String>();
    
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

    
    /** Creates new RemoteCommandParser */
    public RemoteCommandParser() {
    }
    
    
    /** Creates new RemoteCommandLine with the given argument
     *  @param args - command line arguments
     *  @throws CommandValidationException if command name is invalid
     */
    public RemoteCommandParser(String[] args)
        throws CommandValidationException {
            parseCommandLine(args);
    }
    
    
    
    /** Parse the command line arguments accordingly to CLIP
     *  @param args - command line arguments
     *  @throws CommandValidationException if command name is invalid
     */
    private void parseCommandLine(final String[] args)
        throws CommandValidationException {

        commandName = args[0];

        //get all options
        for (int ii=1; ii<args.length; ii++) {
            //verify and get short options
            if (args[ii].matches(SHORT_OPTION_REGEXP)  ||
                args[ii].matches(SHORT_OPTION_ARGUMENT_REGEXP)) {
                ii = insertShortOption(args, ii);
            } else if (args[ii].matches(LONG_OPTION_REGEXP)) {
                    //get long options
                    //long option can be of the following:
                    // --<alphanumeric chars>
                    // --<alphanumeric chars>-<alphnumeric chars>
                    // --<alphanumeric chars>=<option_argument>
                ii = insertLongOption(args, ii);
            } else {
                    //get operands
                ii = insertOperands(Arrays.asList(args).listIterator(ii));
            }
        }
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
        throws CommandValidationException {
        int index;
        if (args[ii].matches(SHORT_OPTION_REGEXP) ||
            args[ii].matches(SHORT_OPTION_ARGUMENT_REGEXP)) {
            index = insertShortOption(args, ii);
        } else {
            throw new CommandValidationException("NoSuchOption " + args[ii]);
        }
        return index;
    }
    
    
    /** Insert short options group to the shot options list
     *  @param sOption - group of short options
     *
     */
    private void insertShortOptionsGroup(final String sOptions)
        throws CommandValidationException {
        for (int jj=1; jj<sOptions.length(); jj++) {
            optionsMap.put(String.valueOf(sOptions.charAt(jj)),"");
        }
    }
    
    /** Insert short option to the short options list
     *  @params args - arguments of options
     *  @params ii   - index in the arguments
     *  @return the index of the next argument
     *  @throws CommandValidationException
     */
    private int insertShortOption(final String[] args, int ii)
        throws CommandValidationException {

        //if short option length is greater than 2 then it is a
        //short options group
        if (args[ii].length() > 2) {
            final int index = args[ii].indexOf('=');
            if (index == -1) {
                insertShortOptionsGroup(args[ii]);
            } else {
                optionsMap.put(String.valueOf(args[ii].charAt(1)), 
                                          args[ii].substring(index+1));
            }
        } else {
            //make sure that the next argument is valid
            if (ii+1 < args.length) {
                //if the next argument starts with "-" then it's not
                //an option argument
                //if argument is a boolean option then next argument
                //is not an option argument
                if (args[ii+1].startsWith("-")) {
                    optionsMap.put(String.valueOf(args[ii].charAt(1)),"");
                } else {
                    optionsMap.put(String.valueOf(args[ii].charAt(1)), args[ii+1]);
                    ii++;
                }
            } else {
                optionsMap.put(args[ii].substring(1),"");
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
        throws CommandValidationException {
        final int index = args[ii].indexOf('=');
        //if the long option and option argument is delimited by a space
        if (index == -1) {
            //boolean option with "--no-" always means false
            if (args[ii].startsWith(BOOLEAN_NO_OPTION) ) {
                if (args[ii+1].startsWith("-")) {
                    throw new CommandValidationException("Invalid command syntax");
                }
                optionsMap.put(args[ii].substring(BOOLEAN_NO_OPTION.length()), FALSE);
            } else if (ii+1<args.length) {
                //if the next argument starts with "-" then it's missing an option value
                if (args[ii+1].startsWith("-") ) {
                    optionsMap.put(args[ii].substring(2), "");
                } else {
                    optionsMap.put(args[ii].substring(2), args[ii+1]);
                    ii++;
                }
            } else {
                optionsMap.put(args[ii].substring(2), "");
            }
        } else {
            //if the long option and option argument is delimited by '='
            optionsMap.put(args[ii].substring(2, index),
                           args[ii].substring(index+1));
        }
        return ii;
    }


    /** Insert the list of operand and the Operand variable
     *  @param operandIter - list of operand
     *  @return last index of the list
     */
    private int insertOperands(final ListIterator operandIter) 
        throws CommandValidationException {
        try {
            //check if the first element is a "--"
            if (!((String)operandIter.next()).equals("--")) {
                Operands.add((String)operandIter.previous());
                //call next to advance to the next element
                operandIter.next();
            }
            
            while (operandIter.hasNext()) {
                Operands.add((String)operandIter.next());
            }
        } catch (NoSuchElementException nsee) {
            throw new CommandValidationException(nsee);
        }
        return operandIter.nextIndex();
    }

    public String getCommandName() {
        return commandName;
    }

    
    /**
     *  returns a Map with all the options in optionsMap
     *  @return options
     */
    public Map getOptions() {
        return optionsMap;
    }

    
    /** gets the operandsList
     *  @return operands
     */
    public Vector getOperands() {
        return Operands;
    }


    public String toString() {
        return "\n**********\nname = " + commandName +
        "\nOptions = " + optionsMap +
        "\nOperands = " + Operands + "\n**********\n";
    }
    
}
