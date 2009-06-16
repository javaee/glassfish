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
import org.glassfish.enterprise.admin.ncli.comm.TargetServer;

import java.io.*;
import java.util.*;

/**
 * Run an admin command.
 * Commands can be specified in two forms:
 * <ul>
 * <li>asadmin asadmin-options command-name command-options operands</li>
 * <li>asadmin command-name asadmin-or-command-options operands</li>
 * </ul>
 *
 * @author Bill Shannon
 */
public final class CommandRunner {
    private static final ProgramOptionBuilder POB =
        ProgramOptionBuilder.getInstance();
    private final Set<String> slc = new HashSet<String>();
    private final Set<String> uslc = new HashSet<String>();

    private final PrintStream out;
    private final PrintStream err;
    private final CommandMetadataCache cache;   // cache of command metadata

    private String cmdName;                     // name of command to execute
    private TargetServer ts;                    // server on which to execute
    private Set<Option> programOptions;
    private CommandDesc desc;
    private NewCommand command;
    private boolean usesDeprecatedSyntax;
    private String[] cmdArgs;
    private int cmdArgsStart;

    private static final LocalStringsImpl lsm =
        new LocalStringsImpl(CommandRunner.class);

    /**
     * Construct a command runner with the given output streams.
     */
    CommandRunner(PrintStream out, PrintStream err) {
        if (out == null || err == null)
            throw new NullPointerException("null out or err");
        this.out = out;
        this.err = err;
        cache = new CommandMetadataCache();
        initialize();
    }

    /**
     * Parse the meta-options from the given command.
     * Do not parse the command options and do not execute the command.
     * Used by unit tests.
     */
    void parseMetaOptions(String[] argv) throws ParserException {
        Set<OptionDesc> known = POB.getAllOptionMetadata();
        Map<String, String> givenProgramOptions =
            new LinkedHashMap<String, String>();
            // given options as name value pairs
        Map<String, String> metaOptions;        // parsed asadmin options

        if (argv.length == 0)
            throw new ParserException(lsm.get("no.command"));

        // if the first argument is an option, we're using the new form
        if (argv[0].startsWith("-")) {
            /*
             * Parse all the asadmin options, stopping at the first non-option,
             * which is the command name.
             */
            usesDeprecatedSyntax = false;
            Parser p = new Parser(known, false);
            p.parse(argv, 0);
            metaOptions = p.getOptions();
            cmdArgs = p.getOperands();
            if (cmdArgs.length == 0)
                throw new ParserException(lsm.get("no.command"));
            cmdName = cmdArgs[0];
            handleUnsupportedLegacyCommand(cmdName);
            cmdArgsStart = 1;   // skip command name
        } else {        // first arg is not an option, using old form
            /*
             * asadmin options and command options are intermixed.
             * Parse the entire command line for asadmin options,
             * removing them from the command line, and ignoring
             * unknown options.  The remaining command line starts
             * with the command name.
             */
            cmdName = argv[0];
            handleUnsupportedLegacyCommand(cmdName);
            // XXX - meta-options parsing depends on whether command is old?
            Parser p = new Parser(known, true);
            p.parse(argv, 1);
            metaOptions = p.getOptions();
            // warn about deprecated use of meta-options
            if (metaOptions.size() > 0) {
                usesDeprecatedSyntax = true;
                // at least one program option specified after command name
                Set<String> names = metaOptions.keySet();
                String[] nameArray = names.toArray(new String[names.size()]);
                out.println(lsm.get("deprecated.syntax", cmdName,
                    Arrays.toString(nameArray)));
            } else
                usesDeprecatedSyntax = false;
            cmdArgs = p.getOperands();
            cmdArgsStart = 0;
        }

        programOptions = initializeAllProgramOptions(metaOptions);
        ts = initializeTargetServer();
    }

    /**
     * Parse the given command and arguments.
     */
    void parseCommand(String[] argv) throws ParserException {
        /*
         * Parse the meta-options to determine the target server.
         */
        parseMetaOptions(argv);

        /*
         * Find the metadata for the command.
         */
        // XXX - for now we just pre-load the cache
        populate(cache, ts);
        desc = cache.get(cmdName, ts);
        if (desc == null)
            // goes to server
            desc = getCommandMetadata(cmdName, ts);
        if (desc == null)
            throw new ParserException(lsm.get("unknown.command", cmdName));

        /*
         * Now parse the resulting command using the command options.
         */
        // convert List<OptionDesc> to Set<OptionDesc>
        Set<OptionDesc> opts = new HashSet<OptionDesc>(desc.getOptionDesc());
        Parser p = new Parser(opts, false);
        p.parse(cmdArgs, cmdArgsStart);
    }

    /**
     * Parse and execute the given command and arguments.
     */
    public void execute(String[] argv) throws ParserException {
        parseCommand(argv);
        /*
        command = new NewCommand(desc, p.getOptions(),
                                Arrays.asList(p.getOperands()));

        // at this point, there are no syntax errors,
        // server is running and command is fully formed.
        // now, only command execution errors can occur
        assert command != null : "Command is null!";
        CommandExecutionResult er = command.execute(ts);
        */
        cache.put(cmdName, ts, desc);
    }

    /**
     * A main method to allow testing.
     */
    public static void main(String[] argv) throws Exception {
        CommandRunner cr = new CommandRunner(System.out, System.err);
        cr.parseMetaOptions(argv);
        System.out.println("Meta-options:");
        for (Option po : cr.getProgramOptions())
            System.out.println("  " + po);
        System.out.println("Target Server: " + cr.getTargetServer());
        System.out.println("Command: " + cr.getCommandName());
        System.out.println("Arguments: " +
            Arrays.toString(cr.getCommandArguments()));
        cr.parseCommand(argv);
    }

    // Following methods only used for testing.

    Set<Option> getProgramOptions() {
        return programOptions;
    }

    String getCommandName() {
        return cmdName;
    }

    boolean usesDeprecatedSyntax() {
        return usesDeprecatedSyntax;
    }

    String[] getCommandArguments() {
        if (cmdArgsStart == 0)
            return cmdArgs;
        int len = cmdArgs.length - cmdArgsStart;
        String[] args = new String[len];
        System.arraycopy(cmdArgs, cmdArgsStart, args, 0, len);
        return args;
    }

    TargetServer getTargetServer() {
        return ts;
    }

    // ALL Private ...
    // Private instance methods

    private void initialize() {
        file2Set(Constants.SUPPORTED_CMD_FILE_NAME, slc);
        file2Set(Constants.UNSUPPORTED_CMD_FILE_NAME, uslc);
    }

    /**
     * If this is an unsupported command, throw an exception.
     */
    private void handleUnsupportedLegacyCommand(String cmd)
            throws ParserException {
        for (String c : uslc) {
            if (c.equals(cmd)) {
                throw new ParserException(
                    lsm.get("unsupported.legacy.command", cmd));
            }
        }
        // it is a supported command; do nothing
    }

    private CommandDesc getCommandMetadata(String cmdName, TargetServer from) {
        // TODO
        return null;
    }

    // ALL Private ...

    /**
     * Given the meta-options found on the command line, return
     * a set with all possible options and their corresponding values.
     */
    private Set<Option> initializeAllProgramOptions(
            Map<String, String> metaOptions) {
        Set<OptionDesc> validOnes = POB.getAllOptionMetadata();
        Set<Option> options = new HashSet<Option>();
        for (OptionDesc opt : validOnes) {
            String value = metaOptions.get(opt.getName());
            boolean added = options.add(new Option(opt, value));
            assert added :
                "Programming Error: " +
                "This option could not be added to the set: " + opt.getName();
        }
        return Collections.unmodifiableSet(options);
    }

    /**
     * Based on the full set of program options (meta-options),
     * create a TargetServer.
     */
    private TargetServer initializeTargetServer() {
        // this method assumes that basic validation is already done.
        String host = null;
        int port = -1;
        String user = null;
        String password = null;
        boolean secure = false;
        for (Option po : programOptions) {
            String name = po.getName();
            if (ProgramOptionBuilder.HOST.equals(name))
                host = po.getEffectiveValue();
            if (ProgramOptionBuilder.PORT.equals(name))
                port = Integer.parseInt(po.getEffectiveValue()); // no check
            if (ProgramOptionBuilder.USER.equals(name))
                user = po.getEffectiveValue();
            if (ProgramOptionBuilder.PASSWORD.equals(name))
                password = po.getEffectiveValue();
            if (ProgramOptionBuilder.SECURE.equals(name))
                secure = Boolean.valueOf(po.getEffectiveValue());
        }
        return new TargetServer(host, port, user, password, secure);
    }

    /**
     * Read the named resource file and add the first token on each line
     * to the set.  Skip comment lines.
     */
    static void file2Set(String file, Set<String> set) {
        BufferedReader reader = null;
        try {
            InputStream is =
                CommandRunner.class.getClassLoader().getResourceAsStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#"))
                    continue; // # indicates comment
                StringTokenizer tok = new StringTokenizer(line, " ");
                // handles with or without space, rudimendary as of now
                String cmd = tok.nextToken();
                set.add(cmd);
            }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ee) {
                    // ignore
                }

            }
        }
    }

    /*
     * Following stubs for testing.
     */
    private static void populate(CommandMetadataCache cache, TargetServer ts) {
        CommandDesc desc;

        desc = new CommandDesc();
        desc.setName("list-commands");
        addOpt(desc, "localonly", '\0', false, false, OptionType.BOOLEAN);
        addOpt(desc, "remoteonly", '\0', false, false, OptionType.BOOLEAN);
        cache.put(desc.getName(), ts, desc);

        desc = new CommandDesc();
        desc.setName("monitor");
        addOpt(desc, "interval", '\0', false, false, OptionType.STRING);
        addOpt(desc, "filter", '\0', false, false, OptionType.STRING);
        addOpt(desc, "filename", '\0', false, false, OptionType.FILE);
        cache.put(desc.getName(), ts, desc);

        desc = new CommandDesc();
        desc.setName("create-jdbc-resource");
        addOpt(desc, "connectionpoolid", '\0', true, false, OptionType.STRING);
        addOpt(desc, "enabled", '\0', false, false, OptionType.BOOLEAN);
        addOpt(desc, "description", '\0', false, false, OptionType.STRING);
        addOpt(desc, "target", '\0', false, false, OptionType.STRING);
        addOpt(desc, "property", '\0', false, false, OptionType.PROPERTY);
        cache.put(desc.getName(), ts, desc);
    }

    private static void addOpt(CommandDesc desc, String name, char sym,
            boolean req, boolean rep, OptionType type) {
        OptionDesc opt = new OptionDesc();
        opt.setName(name);
        if (sym != '\0') opt.setSymbol(Character.toString(sym));
        opt.setRequired(Boolean.toString(req));
        opt.setRepeats(Boolean.toString(rep));
        opt.setType(type.name());
        desc.getOptionDesc().add(opt);
    }
}
