/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.cli.remote;

import java.io.*;
import java.util.*;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.ValidOption;


/**
 * The <code>Parser</code> object is used to parse the
 * command line and verify that the command line is CLIP compliant.
 */
public class Parser {
    // Name of Command
    private String commandName = null;

    // Map of options and values from command-line
    // XXX - Map doesn't allow for options that repeat
    private Map<String,String> optionsMap = new HashMap<String,String>();
    
    // Array of operands from command-line
    private List<String> operands = new ArrayList<String>();

    // The valid options for the command we're parsing
    private Set<ValidOption> options;

    // Ignore unknown options when parsing?
    private boolean ignoreUnknown;

    /*
     * TODO:
     *	option types shouldn't be string literals here
     */

    /**
     * Creates new CLIRemoteCommand with the given argument.
     *
     * @param args  command line arguments
     * @throws CommandValidationException if command name is invalid
     */
    public Parser(String[] args, int start,
	    Set<ValidOption> options, boolean ignoreUnknown)
	    throws CommandValidationException {
	this.options = options;
	this.ignoreUnknown = ignoreUnknown;
	parseCommandLine(args, start);
    }

    /**
     * Parse the command line arguments accordingly to CLIP.
     *
     * @param args  command line arguments
     * @throws CommandValidationException if command name is invalid
     */
    private void parseCommandLine(final String[] argv, final int start)
        throws CommandValidationException {

        for (int si = start; si < argv.length; si++) {
            String arg = argv[si];
            if (arg.equals("--")) {             // end of options
		// if we're ignoring unknown options, we include this
		// delimiter as an operand, it will be eliminated later
		// when we process all remaining options
		if (!ignoreUnknown)
		    si++;
                while (si < argv.length)
                    operands.add(argv[si++]);
                break;
            }

            // is it an operand or option value?
            if (!arg.startsWith("-") || arg.length() <= 1) {
                operands.add(arg);
                if (ignoreUnknown)
                    continue;
                si++;
                while (si < argv.length)
                    operands.add(argv[si++]);
                break;
            }

            // at this point it's got to be an option of some sort
            ValidOption opt = null;
            String name = null;
            String value = null;
            if (arg.charAt(1) == '-') { // long option
                int ns = 2;
		boolean sawno = false;
                if (arg.startsWith("--no-")) {
		    sawno = true;
                    value = "false";
                    ns = 5;             // skip prefix
                }
                // if of the form "--option=value", extract value
                int ne = arg.indexOf('=');
                if (ne < 0)
                    name = arg.substring(ns);
                else {
                    if (value != null)
                        throw new CommandValidationException(
                            "Option may not have value: " + arg);
                    name = arg.substring(ns, ne);
                    value = arg.substring(ne + 1);
                }
                opt = lookupLongOption(name);
		if (sawno && optionRequiresOperand(opt))
		    throw new CommandValidationException(
			"\"--no\" illegal with non-boolean option: " +
			opt.getName());
            } else {                            // short option
		/*
                 * possibilities are:
                 *      -f
                 *      -f value
                 *      -f=value
                 *      -fxyz   (multiple single letter boolean options
                 *              with no arguments)
		 */
                if (arg.length() <= 2) { // one of the first two cases
                    opt = lookupShortOption(arg.charAt(1));
                    name = arg.substring(1);
                } else {                        // one of the last two cases
                    if (arg.charAt(2) == '=') { // -f=value case
                        opt = lookupShortOption(arg.charAt(1));
                        value = arg.substring(3);
                    } else {                            // -fxyz case
                        for (int i = 1; i < arg.length(); i++) {
                            opt = lookupShortOption(arg.charAt(i));
                            if (opt == null) {
                                if (!ignoreUnknown)
                                    throw new CommandValidationException(
                                        "Invalid option: " +
                                        Character.toString(arg.charAt(i)));
                                // unknown option, skip all the rest
                                operands.add(arg);
                                break;
                            }
                            setOption(opt, "true");
                        }
                        continue;
                    }
                }
            }

            // is it a known option?
            if (opt == null) {
                if (!ignoreUnknown)
                    throw new CommandValidationException(
                        "Invalid option: " + arg);
                // unknown option, skip it
                operands.add(arg);
                continue;
            }

            // find option value, if needed
            if (value == null) {
		// if no valid options were specified, we use the next argument
		// as an option as long as it doesn't look like an option
		if (options == null) {
		    if (si + 1 < argv.length && !argv[si + 1].startsWith("-"))
			value = argv[++si];
		    else
			opt.setType("BOOLEAN");	// fake it
		} else if (optionRequiresOperand(opt)) {
		    if (++si >= argv.length)
			throw new CommandValidationException(
			    "Missing value for option: " + name);
		    value = argv[si];
		}
            }
            setOption(opt, value);
        }
    }

    /**
     * Returns a Map with all the options.
     * The Map is indexed by the long name of the option.
     *
     * @return options
     */
    public Map<String,String> getOptions() {
        return optionsMap;
    }

    /**
     * Returns the list of operands.
     *
     * @return list of operands
     */
    public List<String> getOperands() {
        return operands;
    }

    public String toString() {
        return "\n**********\nname = " + commandName +
        "\nOptions = " + optionsMap +
        "\nOperands = " + operands + "\n**********\n";
    }
    
    /**
     * Get ValidOption for long option name.
     */
    private ValidOption lookupLongOption(String s) {
	// XXX - for now, fake it if no options
	if (options == null) {
	    // no valid options specified so everything is valid
	    return new ValidOption(s, "STRING", ValidOption.OPTIONAL, null);
	}
        for (ValidOption od : options) {
            if (od.getName().equals(s))
                return od;
        }
        return null;
    }

    /**
     * Get ValidOption for short option name.
     */
    private ValidOption lookupShortOption(char c) {
	// XXX - for now, fake it if no options
	if (options == null)
	    return null;
        String sc = Character.toString(c);
        for (ValidOption od : options) {
            if (od.getShortNames().contains(sc))
                return od;
        }
        return null;
    }

    /**
     * Does this option require an operand?
     */
    private static boolean optionRequiresOperand(ValidOption opt) {
        return !opt.getType().equals("BOOLEAN");
    }

    /**
     * Check whether the given value is valid for the option.
     */
    private static boolean nonNullValueValidFor(ValidOption po, String value) {
        if (value == null)
                return true;
        // VERY basic validation only if given value is non-null
        if (po == null)
            throw new NullPointerException("null option name");
        value = value.trim();

        if (po.getType().equals("FILE")) {
            File f = new File(value);
            return f.isFile() || f.canRead();
        }
        if (po.getType().equals("BOOLEAN")) {
            return value.toLowerCase(Locale.ENGLISH).equals("true") ||
                value.toLowerCase(Locale.ENGLISH).equals("false");
        }
        // non-null value for any remaining option is valid
        return true;
    }

    /**
     * Set the value for the option.
     */
    private void setOption(ValidOption opt, String value)
            throws CommandValidationException {
        String name = opt.getName();
        if (!nonNullValueValidFor(opt, value)) {
            // name exists, but value is invalid!
            throw new CommandValidationException(
                "Invalid value for option: " + name + ", Value: " + value);
        }
        if (true /* !Boolean.valueOf(opt.getRepeats().toLowerCase()) */) {
            // repeats not allowed
            if (optionsMap.containsKey(name)) {
                throw new CommandValidationException(
			"Repeats not allowed for option: " + name);
            }
            // XXX - repeat is going to replace previous value...
        }

        if (opt.getType().equals("PASSWORD"))
            throw new CommandValidationException(
                "Password not allowed on command line: " + opt.getName());
        if (opt.getType().equals("BOOLEAN") && value == null)
            value = "true";
        optionsMap.put(name, value);
    }
}