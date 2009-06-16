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

package org.glassfish.enterprise.admin.ncli;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import org.glassfish.api.admin.cli.OptionType;
import org.glassfish.enterprise.admin.ncli.metadata.CommandDesc;
import org.glassfish.enterprise.admin.ncli.metadata.OptionDesc;

import java.io.File;
import java.util.*;

/**
 * Parse a command line, separating out options and operands.
 *
 * @author Bill Shannon
 */
final class Parser {
    private final Set<OptionDesc> options;
    private final boolean ignoreUnknown;
    private Map<String, String> optionMap;
    private String[] operands;
    
    private static final LocalStringsImpl lsm =
        new LocalStringsImpl(Parser.class);

    /**
     * Construct a parser to parse the specified options.
     * If ignoreUnknown is set, unknown options are considered
     * operands.  Otherwise, unknown options are errors and
     * parsing stops when an operand is reached.
     */
    Parser(Set<OptionDesc> options, boolean ignoreUnknown) {
        this.ignoreUnknown = ignoreUnknown;
        this.options = options;
    }

    /**
     * Returned the parsed options.
     */
    public Map<String, String> getOptions() {
        return optionMap;
    }

    /**
     * Return the operands.
     */
    public String[] getOperands() {
        return operands;
    }

    /**
     * Parse the given command line.
     */
    void parse(String[] argv, int start) throws ParserException {
        // operates on argv and splits it into program options and
        // command options + operands based on given metadata
        optionMap = new HashMap<String, String>();
        operands = null;
        List<String> remainingArgs = new ArrayList<String>();
        for (int si = start; si < argv.length; si++) {
            String arg = argv[si];
            if (arg.equals("--")) {             // end of options
                // command line ran out of options;
                // return the arguments after "--" verbatim *and stop*
                while (si < argv.length)
                    remainingArgs.add(argv[si++]);
                break;
            }

            // is it an operand or option value?
            if (!arg.startsWith("-") || arg.length() <= 1) {
                remainingArgs.add(arg);
                si++;
                if (ignoreUnknown)
                    continue;
                while (si < argv.length)
                    remainingArgs.add(argv[si++]);
                break;
            }

            // at this point it's got to be an option of some sort
            OptionDesc opt = null;
            String name = null;
            String value = null;
            if (arg.charAt(1) == '-') { // long option
                int ns = 2;
                if (arg.startsWith("--no-")) {
                    value = "false";
                    ns = 5;             // skip prefix
                }
                // if of the form "--option=value", extract value
                int ne = arg.indexOf('=');
                if (ne < 0)
                    name = arg.substring(ns);
                else {
                    if (value != null)
                        throw new ParserException(
                            lsm.get("no.with.value", arg));
                    name = arg.substring(ns, ne);
                    value = arg.substring(ne + 1);
                }
                opt = lookupLongOption(name);
            } else {                            // short option
                // possibilities are:
                //      -f
                //      -f value
                //      -f=value
                //      -fxyz   (multiple single letter boolean options
                //              with no arguments)
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
                                    throw new ParserException(
                                        lsm.get("invalid.option",
                                        Character.toString(arg.charAt(i))));
                                // unknown option, skip all the rest
                                remainingArgs.add(arg);
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
                    throw new ParserException(
                        lsm.get("invalid.option", arg));
                // unknown option, skip it
                remainingArgs.add(arg);
                continue;
            }

            // find option value, if needed
            if (value == null && optionRequiresOperand(opt)) {
                if (++si >= argv.length)
                    throw new ParserException(
                        lsm.get("missing.value.for.known.option", name));
                value = argv[si];
            }
            setOption(opt, value);
        }
        operands = remainingArgs.toArray(new String[remainingArgs.size()]);
    }

    /**
     * Get OptionDesc for long option name.
     */
    private OptionDesc lookupLongOption(String s) {
        for (OptionDesc od : options) {
            if (od.getName().equals(s))
                return od;
        }
        return null;
    }

    /**
     * Get OptionDesc for short option name.
     */
    private OptionDesc lookupShortOption(char c) {
        String sc = Character.toString(c);
        for (OptionDesc od : options) {
            if (od.getSymbol().equals(sc))
                return od;
        }
        return null;
    }

    /**
     * Does this option require an operand?
     */
    private static boolean optionRequiresOperand(OptionDesc opt) {
        return !opt.getType().equals("BOOLEAN");
    }

    /**
     * Check whether the given value is valid for the option.
     */
    private static boolean nonNullValueValidFor(OptionDesc po, String value) {
        if (value == null)
                return true;
        // VERY basic validation only if given value is non-null
        if (po == null)
            throw new IllegalArgumentException ("null arg");
        value = value.trim();

        if (OptionType.FILE.name().equals(po.getType())) {
            File f = new File(value);
            return f.isFile() || f.canRead();
        }
        if (OptionType.BOOLEAN.name().equals(po.getType())) {
            return value.toLowerCase(Locale.US).equals("true") ||
                value.toLowerCase().toLowerCase(Locale.US).equals("false");
        }
        // non-null value for any remaining option is valid
        return true;
    }

    /**
     * Set the value for the option.
     */
    private void setOption(OptionDesc opt, String value)
            throws ParserException {
        String name = opt.getName();
        if (!nonNullValueValidFor(opt, value)) {
            // name exists, but value is invalid!
            throw new ParserException(
                lsm.get("invalid.value.for.known.option", name, value));
        }
        if (!Boolean.valueOf(opt.getRepeats().toLowerCase())) {
            // repeats not allowed
            if (optionMap.containsKey(name)) {
                throw new ParserException(lsm.get("repeats.not.allowed", name));
            }
            // XXX - repeat is going to replace previous value...
        }

        if (OptionType.PASSWORD.name().equals(opt.getType()))
            throw new ParserException(
                lsm.get("password.not.allowed.on.command.line", opt.getName()));
        if (OptionType.BOOLEAN.name().equals(opt.getType()) && value == null)
            value = "true";
        optionMap.put(name, value);
    }
}
