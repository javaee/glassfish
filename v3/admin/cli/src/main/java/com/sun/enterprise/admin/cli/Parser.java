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

package com.sun.enterprise.admin.cli;

import java.io.*;
import java.util.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;


/**
 * The <code>Parser</code> object is used to parse the
 * command line and verify that the command line is CLIP compliant.
 */
public class Parser {
    // Map of options and values from command-line
    // XXX - Map doesn't allow for options that repeat
    private Map<String,String> optionsMap = new HashMap<String,String>();
    
    // Array of operands from command-line
    private List<String> operands = new ArrayList<String>();

    // The valid options for the command we're parsing
    private Set<ValidOption> options;

    // Ignore unknown options when parsing?
    private boolean ignoreUnknown;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(Parser.class);

    /*
     * TODO:
     *  option types shouldn't be string literals here
     */

    /**
     * Parse the given command line arguments
     *
     * @param args  command line arguments
     * @param start index in args to start parsing
     * @param options the valid options to consider while parsing
     * @param ignoreUnknown if true, unknown options are considered operands
     *        instead of generating an exception
     * @throws CommandValidationException if command line parsing fails
     */
    public Parser(String[] args, int start,
            Set<ValidOption> options, boolean ignoreUnknown)
            throws CommandValidationException {
        this.options = options;
        this.ignoreUnknown = ignoreUnknown;
        parseCommandLine(args, start);
    }

    /**
     * Parse the command line arguments according to CLIP.
     *
     * @param argv  command line arguments
     * @throws CommandValidationException if command line is invalid
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
                            strings.get("parser.noValueAllowed", arg));
                    name = arg.substring(ns, ne);
                    value = arg.substring(ne + 1);
                }
                opt = lookupLongOption(name);
                if (sawno && optionRequiresOperand(opt))
                    throw new CommandValidationException(
                        strings.get("parser.illegalNo", opt.getName()));
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
                                        strings.get("parser.invalidOption",
                                        Character.toString(arg.charAt(i))));
                                // unknown option, skip all the rest
                                operands.add(arg);
                                break;
                            }
                            if (opt.getType().equals("BOOLEAN"))
                                setOption(opt, "true");
                            else
                                throw new CommandValidationException(
                                    strings.get("parser.nonbooleanNotAllowed",
                                    Character.toString(arg.charAt(i)), arg));
                        }
                        continue;
                    }
                }
            }

            // is it a known option?
            if (opt == null) {
                if (!ignoreUnknown)
                    throw new CommandValidationException(
                        strings.get("parser.invalidOption", arg));
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
                        opt.setType("BOOLEAN"); // fake it
                } else if (optionRequiresOperand(opt)) {
                    if (++si >= argv.length)
                        throw new CommandValidationException(
                            strings.get("parser.missingValue", name));
                    value = argv[si];
                } else if (opt.getType().equals("BOOLEAN")) {
                    /*
                     * If it's a boolean option, the following parameter
                     * might be the value for the option; peek ahead to
                     * see if it looks like a boolean value.
                     */
                    if (si + 1 < argv.length) {
                        String val = argv[si + 1];
                        if (val.equalsIgnoreCase("true") ||
                                val.equalsIgnoreCase("false")) {
                            // yup, it's a boolean value, consume it
                            si++;
                            value = val;
                        }
                    }
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
        return "CLI parser: Options = " + optionsMap +
		"; Operands = " + operands;
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
        return opt != null && !opt.getType().equals("BOOLEAN");
    }

    /**
     * Set the value for the option.
     */
    private void setOption(ValidOption opt, String value)
            throws CommandValidationException {
        String name = opt.getName();
        // VERY basic validation
        if (opt == null)
            throw new NullPointerException("null option name");
        if (value != null)
            value = value.trim();

        if (opt.getType().equals("FILE")) {
            File f = new File(value);
            if (!(f.isFile() || f.canRead())) {
                // get a real exception for why it's no good
                InputStream is = null;
                try {
                    is = new FileInputStream(f);
                } catch (IOException ioex) {
                    throw new CommandValidationException(
                        strings.get("parser.invalidFileEx",
                                    name, ioex.toString()));
                } finally {
                    if (is != null)
                        try {
                            is.close();
                        } catch (IOException cex) { }
                }
                throw new CommandValidationException(
                    strings.get("parser.invalidFile", name, value));
            }
        } else if (opt.getType().equals("BOOLEAN")) {
            if (value == null)
                value = "true";
            else if (!(value.toLowerCase(Locale.ENGLISH).equals("true") ||
                    value.toLowerCase(Locale.ENGLISH).equals("false")))
                throw new CommandValidationException(
                    strings.get("parser.invalidBoolean", name, value));
        } else if (opt.getType().equals("PASSWORD"))
            throw new CommandValidationException(
                strings.get("parser.passwordNotAllowed", opt.getName()));

        if (true /* !Boolean.valueOf(opt.getRepeats().toLowerCase()) */) {
            // repeats not allowed
            if (optionsMap.containsKey(name)) {
                throw new CommandValidationException(
                        strings.get("parser.noRepeats", name));
            }
            // XXX - repeat is going to replace previous value...
        }

        optionsMap.put(name, value);
    }
}
