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
import java.lang.reflect.*;
import com.sun.enterprise.cli.framework.ValidOption;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CLIDescriptorsReader;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.admin.cli.commands.CommandTable; // XXX - temporary
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;

/**
 * Base class for a CLI command.  An instance of a subclass of this
 * class is created using the getCommand method with the name of the
 * command and the information about its environment.
 * <p>
 * A command is executed with a list of arguments using the execute
 * method.  The implementation of the execute method in this class
 * saves the arguments in the protected argv field, then calls the
 * following protected methods in order: prepare, parse, validate,
 * and executeCommand.  A subclass must implement the prepare method
 * to initialize the metadata that specified the valid options for
 * the command, and the executeCommand method to actually perform the
 * command.  The parse and validate method may also be overridden if
 * needed.  Or, the subclass may override the execute method and
 * provide the complete implementation for the command, including
 * option parsing.
 *
 * @author Bill Shannon
 */
public abstract class CLICommand {
    public static final int ERROR = 1;
    public static final int CONNECTION_ERROR = 2;
    public static final int INVALID_COMMAND_ERROR = 3;
    public static final int SUCCESS = 0;

    private static final CLIDescriptorsReader cliDescriptorsReader;

    private static final Set<String> unsupported;
    private static final String UNSUPPORTED_CMD_FILE_NAME =
                                    "unsupported-legacy-command-names";

    private static final CommandTable localCommands = new CommandTable();

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CLICommand.class);

    private static final Map<String,String> systemProps = 
            Collections.unmodifiableMap(new ASenvPropertyReader().getProps());

    protected static final CLILogger logger = CLILogger.getInstance();

    protected String name;
    protected ProgramOptions programOpts;
    protected Environment env;
    protected String[] argv;
    protected Set<ValidOption> commandOpts;
    protected String operandType;
    protected int operandMin;
    protected int operandMax;
    protected Map<String, String> options;
    protected List<String> operands;
    protected Map<String, String> passwords;

    static {
        cliDescriptorsReader = CLIDescriptorsReader.getInstance();
        // XXX - does this matter?
        cliDescriptorsReader.setSerializeDescriptorsProperty(
                CLIDescriptorsReader.SERIALIZE_COMMANDS_TO_FILES);

        Set<String> unsup = new HashSet<String>();
        file2Set(UNSUPPORTED_CMD_FILE_NAME, unsup);
        unsupported = Collections.unmodifiableSet(unsup);
    }

    /**
     * Get a CLICommand object representing the named command.
     */
    public static CLICommand getCommand(String name, ProgramOptions programOpts,
            Environment env) throws CommandException {

        // first, check if it's a known unsupported command
        checkUnsupportedLegacyCommand(name);

        // next, try to load out own implementation of the command
        CLICommand cmd = getCommandClass(name, programOpts, env);
        if (cmd != null)
            return cmd;

        // see if it's a local command
        try {
            if (cliDescriptorsReader.getCommand(name) != null) {
                logger.printMessage("WARNING: Using old command: " + name);
                return new LocalCommand(name, programOpts, env);
            }
        } catch (CommandValidationException ex) {
            // ignore it
        }

        // nope, must be a remote command
        return new RemoteCommand(name, programOpts, env);

    }

    /**
     * Try to load a local implementation of the command by converting
     * the command name to a class name.
     * XXX - this is just temporary
     */
    private static CLICommand getCommandClass(String name,
            ProgramOptions programOpts, Environment env) {
        try {
            Class cls = localCommands.get(name);
            if (cls == null)    // XXX - for optional commands
                cls = Class.forName(nameToClass(name));
            Constructor cons = cls.getConstructor(new Class[] {
                                String.class,
                                ProgramOptions.class,
                                Environment.class
                            });
            return (CLICommand)cons.newInstance(name, programOpts, env);
        } catch (Exception ex) {
            logger.printDebugMessage("Failed to load command class: " + ex);
            return null;
        }
    }

    private static String nameToClass(String name) {
        StringBuilder sb = new StringBuilder(
                            "com.sun.enterprise.admin.cli.optional.commands.");
        boolean makeUpper = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (makeUpper && Character.isLowerCase(c))
                c = Character.toUpperCase(c);
            if (c == '-') {
                makeUpper = true;
            } else {
                makeUpper = false;
                sb.append(c);
            }
        }
        sb.append("Command");
        logger.printDebugMessage("Command class: " + sb.toString());
        return sb.toString();
    }

    /**
     * Constructor used by subclasses to save the name, program options,
     * and environment information into corresponding protected fields.
     * Finally, this constructor calls the initializeLogger method.
     */
    protected CLICommand(String name, ProgramOptions programOpts,
            Environment env) {
        this.name = name;
        this.programOpts = programOpts;
        this.env = env;
        initializeLogger();
    }

    /**
     * Helper method to define an option.
     *
     * @param name  long option name
     * @param sname short option name
     * @param type  option type (STRING, BOOLEAN, etc.)
     * @param req   is option required?
     * @param def   default value for option
     */
    protected static void addOption(Set<ValidOption> opts, String name,
            char sname, String type, boolean req, String def) {
        ValidOption opt = new ValidOption(name, type,
                req ? ValidOption.REQUIRED : ValidOption.OPTIONAL, def);
        if (sname != '\0') {
            String abbr = Character.toString(sname);
            opt.setShortName(abbr);
        }
        opts.add(opt);
    }

    /**
     * Execute this command with the given arguemnts.
     * The implementation in this class saves the passed arguments in
     * the argv field and calls the initializePasswords method.
     * Then it calls the prepare, parse, and validate methods, finally
     * returning the result of calling the executeCommand method.
     * Note that argv[0] is the command name.
     *
     * @throws CommandException if execution of the command fails
     * @throws CommandValidationException if there's something wrong
     *          with the options or arguments
     */
    public int execute(String... argv)
            throws CommandException, CommandValidationException  {
        this.argv = argv;
        initializePasswords();
        prepare();
        parse();
        validate();
        if (programOpts.isEcho())
            logger.printMessage(toString());
        else if (logger.isDebug())
            logger.printDebugMessage(toString());
        return executeCommand();
    }

    /**
     * Return a string representing the command line used with this command.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(' ');
        if (ok(programOpts.getHost()))
            sb.append("--host ").append(programOpts.getHost()).append(' ');
        if (programOpts.getPort() > 0)
            sb.append("--port ").append(programOpts.getPort()).append(' ');
        if (ok(programOpts.getUser()))
            sb.append("--user ").append(programOpts.getUser()).append(' ');
        if (ok(programOpts.getPasswordFile()))
            sb.append("--passwordfile ").
                append(programOpts.getPasswordFile()).append(' ');
        if (programOpts.isSecure())
            sb.append("--secure ");
        sb.append("--interactive=").
            append(Boolean.toString(programOpts.isInteractive())).append(' ');
        sb.append("--echo=").
            append(Boolean.toString(programOpts.isEcho())).append(' ');
        sb.append("--terse=").
            append(Boolean.toString(programOpts.isTerse())).append(' ');

        if (options != null && operands != null) {
            Set<String> optionKeys = options.keySet();
            for (String key : optionKeys) {
                String value = options.get(key);
                sb.append("--").append(key);
                if (ok(value)) {
                    sb.append('=').append(value);
                }
                sb.append(' ');
            }
            for (Object o : operands)
                sb.append(o).append(' ');
        } else if (argv != null) {
            for (String arg : argv)
                sb.append(arg).append(' ');
        }

        return sb.toString();
    }

    /**
     * Initialize the state of the logger based on any program options.
     */
    protected void initializeLogger() {
        if (programOpts.isTerse())
            logger.setOutputLevel(java.util.logging.Level.INFO);
        else
            logger.setOutputLevel(java.util.logging.Level.FINE);
    }

    /**
     * Initialize the passwords field based on the password
     * file specified in the program options, and initialize the
     * program option's password if available in the password file.
     */
    protected void initializePasswords() throws CommandException {
        passwords = new HashMap<String, String>();
        String pwfile = programOpts.getPasswordFile();

        if (ok(pwfile)) {
            passwords = CLIUtil.readPasswordFileOptions(pwfile, true);
            logger.printDebugMessage("Passwords from password file " +
                                        passwords);
            String password = passwords.get(
                    Environment.AS_ADMIN_ENV_PREFIX + "PASSWORD");
            if (ok(password))
                programOpts.setPassword(password);
        }
    }

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    protected abstract void prepare()
            throws CommandException, CommandValidationException;

    /**
     * The parse method sets the options and operands fields
     * based on the content of the command line arguments.
     * If the program options say this is a help request,
     * we set options and operands as if "--help" had been specified.
     *
     * @throws CommandException if execution of the command fails
     * @throws CommandValidationException if there's something wrong
     *          with the options or arguments
     */
    protected void parse()
            throws CommandException, CommandValidationException  {
        /*
         * If this is a help request, we don't need the command
         * metadata and we throw away all the other options and
         * fake everything else.
         */
        if (programOpts.isHelp()) {
            options = new HashMap<String, String>();
            options.put("help", "true");
            operands = Collections.emptyList();
        } else {
            Parser rcp = new Parser(argv, 1, commandOpts, false);
            options = rcp.getOptions();
            operands = rcp.getOperands();
        }
        logger.printDebugMessage("params: " + options);
        logger.printDebugMessage("operands: " + operands);
    }

    /**
     * The validate method validates that the type and quantity of
     * parameters and operands matches the requirements for this
     * command.  The validate method supplies missing options from
     * the environment.  It also supplies passwords from the password
     * file or prompts for them if interactive.
     *
     * @throws CommandException if execution of the command fails
     * @throws CommandValidationException if there's something wrong
     *          with the options or arguments
     */
    protected void validate()
            throws CommandException, CommandValidationException  {
        /*
         * Check for missing options and operands.
         */
        Console cons = programOpts.isInteractive() ? System.console() : null;

        boolean missingOption = false;
        for (ValidOption opt : commandOpts) {
            if (opt.isValueRequired() != ValidOption.REQUIRED)
                continue;
            if (opt.getType().equals("PASSWORD"))
                continue;       // passwords are handled later
            // if option isn't set, prompt for it (if interactive)
            if (getOption(opt.getName()) == null && cons != null &&
                    !missingOption) {
                cons.printf("%s ",
                    strings.get("optionPrompt", opt.getName()));
                String val = cons.readLine();
                if (ok(val))
                    options.put(opt.getName(), val);
            }
            // if it's still not set, that's an error
            if (getOption(opt.getName()) == null) {
                missingOption = true;
                logger.printMessage(
                        strings.get("missingOption", "--" + opt.getName()));
            }
        }
        if (missingOption)
            throw new CommandValidationException(
                    strings.get("missingOptions", name));

        if (operands.size() < operandMin && cons != null) {
            cons.printf("%s ",
                strings.get("operandPrompt", /* XXX - need operand name */0));
            String val = cons.readLine();
            if (ok(val)) {
                operands = new ArrayList<String>();
                operands.add(val);
            }
        }
        if (operands.size() < operandMin)
            throw new CommandValidationException(
                    strings.get("notEnoughOperands", name, operandType));
        if (operands.size() > operandMax) {
            if (operandMax == 0)
                throw new CommandValidationException(
                    strings.get("noOperandsAllowed", name));
            else if (operandMax == 1)
                throw new CommandValidationException(
                    strings.get("tooManyOperands1", name));
            else
                throw new CommandValidationException(
                    strings.get("tooManyOperands", name, operandMax));
        }

        initializeCommandPassword();
    }

    /**
     * Execute the command using the options in options and the
     * operands in operands.
     *
     * @return the exit code
     * @throws CommandException if execution of the command fails
     * @throws CommandValidationException if there's something wrong
     *          with the options or arguments
     */
    protected abstract int executeCommand()
            throws CommandException, CommandValidationException;

    /**
     * Initialize all the passwords required by the command.
     *
     * @throws CommandException
     */
    private void initializeCommandPassword()
            throws CommandException, CommandValidationException {
        /*
         * Go through all the valid options and check for required password
         * options that weren't specified in the password file.  If option
         * is missing and we're interactive, prompt for it.  Store the
         * password as if it was a parameter.
         */
        for (ValidOption opt : commandOpts) {
            if (!opt.getType().equals("PASSWORD"))
                continue;
            String pwdname = opt.getName();
            if (ok(passwords.get(pwdname))) {
                options.put(pwdname, passwords.get(pwdname));
                continue;
            }
            if (opt.isValueRequired() != ValidOption.REQUIRED)
                continue;
            String pwd = getPassword(opt.getName());
            if (pwd == null)
                throw new CommandValidationException(
                            strings.get("missingPassword", name, pwdname));
            passwords.put(pwdname, pwd);
            options.put(pwdname, pwd);
        }
    }

    /**
     * Get a password of the given name.
     * In not interactive, returns null.  Otherwise, prompts for the
     * password twice, compares the two values, and if they're the same
     * and meet other validity criteria (i.e., length) returns the password.
     */
    protected String getPassword(String passwordName)
            throws CommandValidationException {

        if (!programOpts.isInteractive())
            return null;

        final String newprompt = strings.get("NewPasswordPrompt", passwordName);
        final String confirmationPrompt =
            strings.get("NewPasswordConfirmationPrompt", passwordName);

        String newpassword = readPassword(newprompt);
        if (!isPasswordValid(newpassword)) {
            throw new CommandValidationException(
                    strings.get("PasswordLimit", passwordName));
        }

        String newpasswordAgain = readPassword(confirmationPrompt);
        if (!newpassword.equals(newpasswordAgain)) {
            throw new CommandValidationException(
                strings.get("OptionsDoNotMatch", passwordName));
        }
        return newpassword;
    }

    /**
     * Display the given prompt and read a password without echoing it.
     * Returns null if no console available.
     */
    protected String readPassword(String prompt) {
        String password = null;
        Console cons = System.console();
        if (cons != null) {
            char[] pc = cons.readPassword("%s ", prompt);
            // yes, yes, yes, it would be safer to not keep it in a String
            password = new String(pc);
        }
        return password;
    }

    /**
     * Check the password for validity.
     * Currently only verifies that the password is at least 8 characters.
     */
    protected boolean isPasswordValid(String passwd) {
        return (passwd.length() < 8)? false:true;
    }

    /**
     * Get an option value, that might come from the command line
     * or from the environment.
     */
    protected String getOption(String name) {
        String val = options.get(name);
        if (val == null)
            val = env.getStringOption(name);
        return val;
    }

    /**
     * Get a boolean option value, that might come from the command line
     * or from the environment.
     */
    protected boolean getBooleanOption(String name) {
        String val = getOption(name);
        return val != null && Boolean.parseBoolean(val);
    }

    /**
     * Return the named system property, or property
     * set in asenv.conf.
     */
    protected String getSystemProperty(String name) {
        return systemProps.get(name);
    }

    /**
     * If this is an unsupported command, throw an exception.
     */
    private static void checkUnsupportedLegacyCommand(String cmd)
            throws CommandException {
        for (String c : unsupported) {
            if (c.equals(cmd)) {
                throw new CommandException(
                    "Previously supported command: " + cmd +
                    " is not supported for this release.");
            }
        }
        // it is a supported command; do nothing
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    /**
     * Read the named resource file and add the first token on each line
     * to the set.  Skip comment lines.
     */
    private static void file2Set(String file, Set<String> set) {
        BufferedReader reader = null;
        try {
            InputStream is = CLICommand.class.getClassLoader().
                                getResourceAsStream(file);
            if (is == null)
                return;     // in case the resource doesn't exist
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
}
