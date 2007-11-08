/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer;

import java.io.PrintWriter;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


/**
 * Represents a set of options a program may support.
 *
 * @author Martin Zaun
 */
public class OptionSet
    extends LogSupport
{
    // return values of parse/check methods
    static public final int OK = 0;
    static public final int USAGE_ERROR = -1;

    // command-line option prefixes
    static public final String prefix = "-";
    static public final String lprefix = "--";

    // ----------------------------------------------------------------------

    /**
     * The base class of all option types.
     */
    static public abstract class Option
    {
        /**
         * The set the option is registered with.
         */
        protected OptionSet set;

        /**
         * The long form name of this option.
         */
        public final String name;

        /**
         * The short form name of this option.
         */
        public final String abbrev;

        /**
         * A description of this option.
         */
        public final String descr;

        /**
         * Creates an instance.
         */
        public Option(String name,
                      String abbrev,
                      String descr)
        {
            affirm(name != null);
            this.name = name;
            this.abbrev = abbrev;
            this.descr = descr;            
        }

        /**
         * Parse this option for arguments it may require.
         */
        abstract public int parse(Iterator i);

        /**
         * Returns a <code>String</code> representation of this option's
         * value for printing.
         */
        abstract public String asNameValue();

        /**
         * Returns a usage description of this option.
         */
        public String asUsageHelp()
        {
            String abbr = (abbrev == null ? "   " : prefix + abbrev + "|");
            return (abbr + lprefix + name + " " + descr);
        }
    }

    /**
     * An option that always causes a USAGE_ERROR when parsed (used for
     * '-h|--help' kind of options).
     */
    static public class HelpOption extends Option
    {
        /**
         * Creates an instance.
         */
        public HelpOption(String name,
                          String abbrev,
                          String descr)
        {
            super(name, abbrev, descr);
        }
        
        public int parse(Iterator i) 
        {
            return USAGE_ERROR;
        }

        public String asNameValue()
        {
            return ("help = false");
        }
    }

    /**
     * An option representing a boolean flag.
     */
    static public class FlagOption extends Option
    {
        /**
         * The default value for this option.
         */
        public final boolean deflt;

        /**
         * The value of this option.
         */
        public boolean value;

        /**
         * Creates an instance.
         */
        public FlagOption(String name,
                          String abbrev,
                          String descr)
        {
            this(name, abbrev, descr, false);
        }

        /**
         * Creates an instance.
         */
        public FlagOption(String name,
                          String abbrev,
                          String descr,
                          boolean deflt)
        {
            super(name, abbrev, descr);
            this.deflt = deflt;
            this.value = deflt;
        }

        public int parse(Iterator i) 
        {
            if (value != deflt) {
                set.printUsageError("Repeated option: "
                                    + prefix + abbrev + "/" + lprefix + name);
                return USAGE_ERROR;
            }
            value = true;
            return OK;
        }

        public String asNameValue()
        {
            return (name + " = " + String.valueOf(value));
        }
    }

    /**
     * An option representing a <code>int</code> value.
     */
    static public class IntOption extends Option
    {
        /**
         * The default value for this option.
         */
        public final int deflt;

        /**
         * The value of this option.
         */
        public int value;

        /**
         * Creates an instance.
         */
        public IntOption(String name,
                         String abbrev,
                         String descr)
        {
            this(name, abbrev, descr, 0);
        }

        /**
         * Creates an instance.
         */
        public IntOption(String name,
                         String abbrev,
                         String descr,
                         int deflt)
        {
            super(name, abbrev, descr);
            this.deflt = deflt;
            this.value = deflt;
        }

        public int parse(Iterator i) 
        {
            if (value != deflt) {
                set.printUsageError("Repeated option: "
                                    + prefix + abbrev + "/" + lprefix + name);
                return USAGE_ERROR;
            }
            if (!i.hasNext()) {
                set.printUsageError("Missing argument to option: "
                                    + prefix + abbrev + "/" + lprefix + name);
                return USAGE_ERROR;
            }
            try {
                value = Integer.valueOf((String)i.next()).intValue();
            } catch (NumberFormatException ex) {
                set.printUsageError("Illegal argument to option: "
                                    + prefix + abbrev + "/" + lprefix + name);
                return USAGE_ERROR;
            }
            return OK;
        }

        public String asNameValue()
        {
            return (name + " = " + String.valueOf(value));
        }
    }

    /**
     * An option representing a <code>String</code> value.
     */
    static public class StringOption extends Option
    {
        /**
         * The default value for this option.
         */
        public final String deflt;

        /**
         * The value of this option.
         */
        public String value;

        /**
         * Creates an instance.
         */
        public StringOption(String name,
                            String abbrev,
                            String descr)
        {
            this(name, abbrev, descr, null);
        }

        /**
         * Creates an instance.
         */
        public StringOption(String name,
                            String abbrev,
                            String descr,
                            String deflt)
        {
            super(name, abbrev, descr);
            this.deflt = deflt;
            this.value = deflt;
        }

        public int parse(Iterator i) 
        {
            if (value != deflt) {
                set.printUsageError("Repeated option: "
                                    + prefix + abbrev + "/" + lprefix + name);
                return USAGE_ERROR;
            }
            if (!i.hasNext()) {
                set.printUsageError("Missing argument to option: "
                                    + prefix + abbrev + "/" + lprefix + name);
                return USAGE_ERROR;
            }
            value = (String)i.next();
            if (value.startsWith(prefix)) {
                set.printUsageError("Missing argument to option: "
                                    + prefix + abbrev + "/" + lprefix + name);
                return USAGE_ERROR;
            }
            return OK;
        }

        public String asNameValue()
        {
            return (name + " = " + String.valueOf(value));
        }
    }

    // ----------------------------------------------------------------------

    /**
     * The list of registered options.
     */
    protected final List options = new ArrayList();

    /**
     * Maps the option's long form against option instances.
     */
    protected final Map names = new HashMap();

    /**
     * Maps the option's short form against option instances.
     */
    protected final Map abbrevs = new HashMap();

    /**
     * The collected arguments.
     */
    protected final List arguments = new ArrayList();

    /**
     * Usage printout.
     */
    public String usageHeader
        = "Usage: <options>.. <arguments>..";

    /**
     * Usage printout.
     */
    public String optionsHeader
        = "Options:";

    /**
     * Usage printout.
     */
    public String argumentsHeader
        = "Arguments:";

    /**
     * Usage printout.
     */
    public String returnHeader
        = "Returns: A non-zero value in case of errors.";

    /**
     * Usage printout.
     */
    public String indent
        = "    ";

    /**
     * Creates an instance.
     */
    public OptionSet(PrintWriter out,
                     PrintWriter err) 
    {
        super(out, err);
    }

    /**
     * Creates an instance.
     */
    public OptionSet(PrintWriter out,
                     PrintWriter err,
                     String usageHeader,
                     String optionsHeader,
                     String argumentsHeader,
                     String returnHeader,
                     String indent)
    {
        this(out, err);
        this.usageHeader = usageHeader;
        this.optionsHeader = optionsHeader;
        this.argumentsHeader = argumentsHeader;
        this.returnHeader = returnHeader;
        this.indent = indent;
    }

    // ----------------------------------------------------------------------

    /**
     * Registers an option with the set.
     */
    public void register(Option option) 
    {
        affirm(option != null);
        option.set = this;
        options.add(option);

        affirm(option.name != null);
        Object obj = names.put(lprefix + option.name, option);
        affirm(obj == null, "Option already registered: " + option.name);

        if (option.abbrev != null) {
            obj = abbrevs.put(prefix + option.abbrev, option);
            affirm(obj == null, "Option already registered: " + option.name);
        }
    }

    /**
     * Creates and registers an option representing a usage-help request.
     */
    public HelpOption createHelpOption(String name,
                                       String abbrev,
                                       String descr)
    {
        final HelpOption opt = new HelpOption(name, abbrev, descr);
        register(opt);
        return opt;
    }

    /**
     * Creates and registers an option representing a boolean flag.
     */
    public FlagOption createFlagOption(String name,
                                       String abbrev,
                                       String descr)
    {
        final FlagOption opt = new FlagOption(name, abbrev, descr);
        register(opt);
        return opt;
    }

    /**
     * Creates and registers an option representing a boolean flag.
     */
    public FlagOption createFlagOption(String name,
                                       String abbrev,
                                       String descr,
                                       boolean deflt)
    {
        final FlagOption opt = new FlagOption(name, abbrev, descr, deflt);
        register(opt);
        return opt;
    }

    /**
     * Creates and registers an option representing a <code>int</code>
     * value.
     */
    public IntOption createIntOption(String name,
                                     String abbrev,
                                     String descr)
    {
        final IntOption opt = new IntOption(name, abbrev, descr);
        register(opt);
        return opt;
    }

    /**
     * Creates and registers an option representing a <code>int</code>
     * value.
     */
    public IntOption createIntOption(String name,
                                     String abbrev,
                                     String descr,
                                     int deflt)
    {
        final IntOption opt = new IntOption(name, abbrev, descr, deflt);
        register(opt);
        return opt;
    }

    /**
     * Creates and registers an option representing a <code>String</code>
     * value.
     */
    public StringOption createStringOption(String name,
                                           String abbrev,
                                           String descr)
    {
        final StringOption opt = new StringOption(name, abbrev, descr);
        register(opt);
        return opt;
    }

    /**
     * Creates and registers an option representing a <code>String</code>
     * value.
     */
    public StringOption createStringOption(String name,
                                           String abbrev,
                                           String descr,
                                           String deflt)
    {
        final StringOption opt
            = new StringOption(name, abbrev, descr, deflt);
        register(opt);
        return opt;
    }

    // ----------------------------------------------------------------------

    /**
     * Parses options and arguments.
     */
    public int parse(String[] argv)
    {
        affirm(argv != null);
        for (Iterator i = Arrays.asList(argv).iterator(); i.hasNext();) {
            final String arg = (String)i.next();

            // ignore empty arguments
            if (arg == null || arg.length() == 0) {
                //println("Ignoring empty command line argument.");
                continue;
            }

            // collect as argument if not option
            if (!arg.startsWith(prefix)) {
                arguments.add(arg);
                continue;                
            }

            // lookup option by short and long form
            Option option = (Option)abbrevs.get(arg);
            if (option == null) {
                option = (Option)names.get(arg);
            }

            // return if option still not recognized
            if (option == null) {
                printlnErr("Unrecognized option: " + arg);
                return USAGE_ERROR;
            }

            // parse option for arguments
            int res = option.parse(i);
            if (res != OK) {
                return res;
            }
        }
        return OK;
    }

    /**
     * Checks options and arguments.
     */
    public int check()
    {
        return OK;
    }

    /**
     * Parse and check options and arguments.
     */
    public int process(String[] args)
    {
        int res = OK;
        if ((res = parse(args)) != OK) {
            printUsage();
            return res;
        }
        if ((res = check()) != OK) {
            printUsage();
            return res;
        }
        return res;
    }

    // ----------------------------------------------------------------------

    /**
     * Print a usage error message to System.err.
     */
    public void printUsageError(String msg)
    {
        printlnErr("USAGE ERROR: " + msg);
    }
    
    /**
     * Print a usage message to System.err.
     */
    public void printUsage()
    {
        println();
        printUsageHeader();
        printOptionHeader();
        printOptionUsage();
        printArgumentHeader();
        printArgumentUsage();
        printReturnHeader();
        printReturnUsage();
    }

    /**
     * Print a usage message to System.err.
     */
    public void printUsageHeader()
    {
        printlnErr(usageHeader);
    }

    /**
     * Print a usage message to System.err.
     */
    public void printOptionHeader()
    {
        printlnErr();
        printlnErr(optionsHeader);
    }

    /**
     * Print a usage message to System.err.
     */
    public void printOptionUsage()
    {
        for (Iterator i = options.iterator(); i.hasNext();) {
            printlnErr(indent + ((Option)i.next()).asUsageHelp());
        }
    }

    /**
     * Print a usage message to System.err.
     */
    public void printArgumentHeader()
    {
        printlnErr();
        printlnErr(argumentsHeader);
    }

    /**
     * Print a usage message to System.err.
     */
    public void printArgumentUsage()
    {}

    /**
     * Print a usage message to System.err.
     */
    public void printReturnHeader()
    {
        printlnErr();
        printlnErr(returnHeader);
    }

    /**
     * Print a usage message to System.err.
     */
    public void printReturnUsage()
    {}

    // ----------------------------------------------------------------------

    /**
     * Print options and arguments.
     */
    public void printAll()
    {
        printOptions();
        printArguments();
    }

    /**
     * Print options.
     */
    public void printOptions()
    {
        println();
        println(optionsHeader);
        for (Iterator i = options.iterator(); i.hasNext();) {
            println(indent + ((Option)i.next()).asNameValue());
        }
    }
    
    /**
     * Print arguments.
     */
    public void printArguments()
    {
        println();
        println(argumentsHeader);
        print(indent);
        for (Iterator i = arguments.iterator(); i.hasNext();) {
            print(" " + i.next());
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Tests the class.
     */
    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> OptionSet.main()");
        final OptionSet options = new OptionSet(out, out);
        out.println("    options.process() ...");
        int res = options.process(args);
        out.println("    return value: " + res);
        out.println("<-- OptionSet.main()");
    }
}
