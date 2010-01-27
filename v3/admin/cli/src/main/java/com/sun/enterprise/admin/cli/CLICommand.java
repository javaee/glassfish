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

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;

import com.sun.enterprise.admin.cli.util.*;
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
@Contract
@Scoped(PerLookup.class)
public abstract class CLICommand implements PostConstruct {
    public static final int ERROR = 1;
    public static final int CONNECTION_ERROR = 2;
    public static final int INVALID_COMMAND_ERROR = 3;
    public static final int SUCCESS = 0;

    private static final Set<String> unsupported;
    private static final String UNSUPPORTED_CMD_FILE_NAME =
                                    "unsupported-legacy-command-names";

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(CLICommand.class);

    private static final Map<String,String> systemProps = 
            Collections.unmodifiableMap(new ASenvPropertyReader().getProps());

    protected static final CLILogger logger = CLILogger.getInstance();

    /**
     * The name of the command.
     * Initialized in the constructor.
     */
    protected String name;

    /**
     * The program options for the command.
     * Initialized in the constructor.
     */
    @Inject
    protected ProgramOptions programOpts;

    /**
     * The environment for the command.
     * Initialized in the constructor.
     */
    @Inject
    protected Environment env;

    /**
     * The command line arguments for this execution.
     * Initialized in the execute method.
     */
    protected String[] argv;

    /**
     * The metadata describing the command's options and operands.
     * XXX - should be collected together into a CommandModel object
     */
    protected Set<ValidOption> commandOpts;
    protected StringBuilder metadataErrors;
    protected String operandName = "";
    protected String operandType;
    protected int operandMin;
    protected int operandMax;
    protected boolean unknownOptionsAreOperands = false;

    /**
     * The options parsed from the command line.
     * Initialized by the parse method.
     */
    protected Map<String, String> options;

    /**
     * The operands parsed from the command line.
     * Initialized by the parse method.
     */
    protected List<String> operands;

    /**
     * The passwords read from the password file.
     * Initialized by the initializeCommandPassword method.
     */
    protected Map<String, String> passwords;

    static {
        Set<String> unsup = new HashSet<String>();
        file2Set(UNSUPPORTED_CMD_FILE_NAME, unsup);
        unsupported = Collections.unmodifiableSet(unsup);
    }

    /**
     * Get a CLICommand object representing the named command.
     */
    public static CLICommand getCommand(Habitat habitat, String name)
            throws CommandException {

        // first, check if it's a known unsupported command
        checkUnsupportedLegacyCommand(name);

        // next, try to load our own implementation of the command
        CLICommand cmd = habitat.getComponent(CLICommand.class, name);
        if (cmd != null)
            return cmd;

        // nope, must be a remote command
        logger.printDebugMessage("Assuming it's a remote command: " + name);
        return new RemoteCommand(name,
            habitat.getComponent(ProgramOptions.class),
            habitat.getComponent(Environment.class));
    }

    /**
     * Constructor used by subclasses when instantiated by HK2.
     * ProgramOptions and Environment are injected.  name is set here.
     */
    protected CLICommand() {
        Service service = this.getClass().getAnnotation(Service.class);

        if (service == null)
            name = "unknown-command";   // should never happen
        else
            name = service.name();
    }

    /**
     * Initialize the logger after being instantiated by HK2.
     */
    public void postConstruct() {
        initializeLogger();
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
     * @return the ValidOption created for the option
     */
    protected static ValidOption addOption(Set<ValidOption> opts, String name,
            char sname, String type, boolean req, String def) {
        ValidOption opt = new ValidOption(name, type,
                req ? ValidOption.REQUIRED : ValidOption.OPTIONAL, def);
        if (sname != '\0') {
            String abbr = Character.toString(sname);
            opt.setShortName(abbr);
        }
        opts.add(opt);
        return opt;
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
        if (checkHelp())
            return 0;
        validate();
        if (programOpts.isEcho()) {
            logger.printMessage(toString());
            // In order to avoid echoing commands used intenally to the
            // implementation of *this* command, we turn off echo after
            // having echoed this command.
            programOpts.setEcho(false);
        } else if (logger.isDebug())
            logger.printDebugMessage(toString());
        return executeCommand();
    }

    /**
     * Return the name of this command.
     */
    public String getName() {
        return name;
    }

    /**
     * Return a Reader for the man page for this command,
     * or null if not found.
     */
    public Reader getManPage() {
        return CLIManFileFinder.getCommandManFile(this);
    }

    /**
     * Get the usage text.
     *
     * @return usage text
     */
    public String getUsage() {
        StringBuilder usageText = new StringBuilder();
        usageText.append(strings.get("Usage", strings.get("Usage.asadmin")));
        usageText.append(" ");
        usageText.append(getName());
        int len = usageText.length();
        StringBuilder optText = new StringBuilder();
        String lsep = System.getProperty("line.separator");
        for (ValidOption opt : usageOptions()) {
            optText.setLength(0);
            final String optName = opt.getName();
            // do not want to display password as an option
            if (opt.getType().equals("PASSWORD"))
                continue;
            boolean optional = opt.isValueRequired() != ValidOption.REQUIRED;
            String defValue = opt.getDefaultValue();
            if (optional)
                optText.append("[");
            if (opt.hasShortName()) {
                Vector<String> sn = opt.getShortNames(); // XXX - why Vector?
                optText.append('-').append(sn.get(0)).append('|');
            }
            optText.append("--").append(optName);

            if (opt.getType().equals("BOOLEAN")) {
                // canonicalize default value
                if (ok(defValue) && Boolean.parseBoolean(defValue))
                    defValue = "true";
                else
                    defValue = "false";
                optText.append("[=<").append(optName);
                optText.append(strings.get("Usage.default", defValue));
                optText.append(">]");
            } else {    // STRING or FILE
                if (ok(defValue)) {
                    optText.append(" <").append(optName);
                    optText.append(strings.get("Usage.default", defValue));
                    optText.append('>');
                } else
                    optText.append(" <").append(optName).append('>');
            }
            if (optional)
                optText.append("]");

            if (len + 1 + optText.length() > 80) {
                usageText.append(lsep).append('\t');
                len = 8;
            } else {
                usageText.append(' ');
                len++;
            }
            usageText.append(optText);
            len += optText.length();
        }

        optText.setLength(0);
        String opname = operandName;
        if (!ok(opname))
            opname = "operand";
        if (operandMax > 0) {
            if (operandMin == 0) {
                optText.append("[").append(opname);
                if (operandMax > 1)
                    optText.append(" ...");
                optText.append("]");
            } else {
                optText.append(opname);
                if (operandMax > 1)
                    optText.append(" ...");
            }
        }
        if (len + 1 + optText.length() > 80) {
            usageText.append(lsep).append('\t');
            len = 8;
        } else {
            usageText.append(' ');
            len++;
        }
        usageText.append(optText);
        return usageText.toString();
    }

    /**
     * Subclasses can override this method to supply additional
     * or different options that should be part of the usage text.
     * Most commands will never need to do this, but the create-domain
     * command uses it to include the --user option as a required option.
     */
    protected Set<ValidOption> usageOptions() {
        return commandOpts;
    }

    /**
     * Return a string representing the command line used with this command.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // first, the program options
        sb.append("asadmin ");
        sb.append(programOpts.toString()).append(' ');

        // now the subcommand options and operands
        sb.append(name).append(' ');

        // have we parsed any options yet?
        if (options != null && operands != null) {
            for (ValidOption opt : commandOpts) {
                if (opt.getType().equals("PASSWORD"))
                    continue;       // don't print passwords
                // include every option that was specified on the command line
                // and every option that has a default value
                String value = getOption(opt.getName());
                if (value == null)
                    value = opt.getDefaultValue();
                if (value != null) {
                    sb.append("--").append(opt.getName());
                    if (opt.getType().equals("BOOLEAN")) {
                        if (Boolean.parseBoolean(value))
                            sb.append("=").append("true");
                        else
                            sb.append("=").append("false");
                    } else {    // STRING or FILE
                        sb.append(" ").append(value);
                    }
                    sb.append(' ');
                }
            }
            for (Object o : operands)
                sb.append(o).append(' ');
        } else if (argv != null) {
            // haven't parsed any options, include raw arguments, if any
            for (String arg : argv)
                sb.append(arg).append(' ');
        }

        sb.setLength(sb.length() - 1);  // strip trailing space
        return sb.toString();
    }

    /**
     * If the program options haven't already been set, parse them
     * on the command line and remove them from the command line.
     * Subclasses should call this method in their prepare method
     * after initializing commandOpts (so usage is available on failure)
     * if they want to allow program options after the command name.
     * Currently RemoteCommand does this, as well as the local commands
     * that also need to talk to the server.
     */
    protected void processProgramOptions()
            throws CommandException, CommandValidationException  {
        if (!programOpts.isOptionsSet()) {
            logger.printDebugMessage("Parsing program options");
            /*
             * asadmin options and command options are intermixed.
             * Parse the entire command line for asadmin options,
             * removing them from the command line, and ignoring
             * unknown options.
             */
            Parser rcp = new Parser(argv, 0,
                            ProgramOptions.getValidOptions(), true);
            Map<String, String> params = rcp.getOptions();
            List<String> operands = rcp.getOperands();
            argv = operands.toArray(new String[operands.size()]);
            if (params.size() > 0) {
                // at least one program option specified after command name
                logger.printDebugMessage("Update program options");
                programOpts.updateOptions(params);
                initializeLogger();
                initializePasswords();
                if (!programOpts.isTerse() &&
                        !(params.size() == 1 && params.get("help") != null)) {
                    // warn about deprecated use of program options
                    // (except --help)
                    // XXX - a lot of work for a nice message...
                    Set<ValidOption> programOptions =
                            ProgramOptions.getValidOptions();
                    StringBuilder sb = new StringBuilder();
                    sb.append("asadmin");
                    for (Map.Entry<String, String> p : params.entrySet()) {
                        // find the corresponding ValidOption
                        ValidOption opt = null;
                        for (ValidOption vo : programOptions) {
                            if (vo.getName().equals(p.getKey())) {
                                opt = vo;
                                break;
                            }
                        }
                        if (opt == null)        // should never happen
                            continue;

                        // format the option appropriately
                        sb.append(" --").append(p.getKey());
                        if (opt.getType().equals("BOOLEAN")) {
                            if (!p.getValue().equalsIgnoreCase("true"))
                                sb.append("=false");
                        } else {
                            if (ok(p.getValue()))
                                sb.append(" ").append(p.getValue());
                        }
                    }
                    sb.append(" ").append(name).append(" [options] ...");
                    logger.printMessage(strings.get("DeprecatedSyntax"));
                    logger.printMessage(sb.toString());
                }
            }
        }
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
            if (password != null && programOpts.getPassword() == null)
                programOpts.setPassword(password,
                    ProgramOptions.PasswordLocation.PASSWORD_FILE);
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
            Parser rcp =
                new Parser(argv, 1, commandOpts, unknownOptionsAreOperands);
            options = rcp.getOptions();
            operands = rcp.getOperands();

            /*
             * In the case where we're accepting unknown options as
             * operands, the special "--" delimiter will also be
             * accepted as an operand.  We eliminate it here.
             */
            if (unknownOptionsAreOperands &&
                    operands.size() > 0 && operands.get(0).equals("--"))
                operands.remove(0);
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
            if (opt.getType().equals("PASSWORD"))
                continue;       // passwords are handled later
            if (opt.isValueRequired() != ValidOption.REQUIRED)
                continue;
            // if option isn't set, prompt for it (if interactive)
            if (getOption(opt.getName()) == null && cons != null &&
                    !missingOption) {
                cons.printf("%s",
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
            cons.printf("%s",
                strings.get("operandPrompt", operandName));
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
     * Check if the current request is a help request, either because
     * --help was specified as a programoption or a command option.
     * If so, get the man page using the getManPage method, copy the
     * content to System.out, and return true.  Otherwise return false.
     * Subclasses may override this method to perform a different check
     * or to use a different method to display the man page.
     * If this method returns true, the validate and executeCommand methods
     * won't be called.
     */
    protected boolean checkHelp()
            throws CommandException, CommandValidationException {
        if (programOpts.isHelp() || getBooleanOption("help")) {
            Reader r = getManPage();
            if (r == null)
                throw new CommandException(strings.get("ManpageMissing", name));
            BufferedReader br = new BufferedReader(r);
            String line;
            try {
            while ((line = br.readLine()) != null)
                System.out.println(line);
            } catch (IOException ioex) {
                throw new CommandException(
                            strings.get("ManpageMissing", name), ioex);
            }
            return true;
        } else
            return false;
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
            String pwd = getPassword(opt, null, true);
            // XXX - hack alert!  the description is stored in the default value
            String description = opt.getDefaultValue();
            if (!ok(description))
                description = pwdname;
            if (pwd == null) {
                if (opt.isValueRequired() != ValidOption.REQUIRED)
                    continue;       // not required, skip it
                throw new CommandValidationException(
                            strings.get("missingPassword", name, description));
            }
            options.put(pwdname, pwd);
        }
    }

    /**
     * Get a password for the given option.
     * First, look in the passwords map.  If found, return it.
     * If not found, and not required, return null;
     * If not interactive, return null.  Otherwise, prompt for the
     * password.  If create is true, prompt twice and compare the two values
     * to make sure they're the same.  If the password meets other validity
     * criteria (i.e., length) returns the password.  If defaultPassword is
     * not null, "Enter" selects this default password, which is returned.
     */
    protected String getPassword(ValidOption opt, String defaultPassword,
            boolean create) throws CommandValidationException {

        String passwordName = opt.getName();
        String password = passwords.get(passwordName);
        if (password != null)
            return password;

        if (opt.isValueRequired() != ValidOption.REQUIRED)
            return null;        // not required

        if (!programOpts.isInteractive())
            return null;        // can't prompt for it

        // XXX - hack alert!  the description is stored in the default value
        String description = opt.getDefaultValue();
        String newprompt;
        if (ok(description)) {
            if (defaultPassword != null) {
                if (defaultPassword.length() == 0)
                    newprompt =
                        strings.get("NewPasswordDescriptionDefaultEmptyPrompt",
                                            description);
                else
                    newprompt =
                        strings.get("NewPasswordDescriptionDefaultPrompt",
                                            description, defaultPassword);
            } else
                newprompt =
                    strings.get("NewPasswordDescriptionPrompt", description);
        } else {
            if (defaultPassword != null) {
                if (defaultPassword.length() == 0)
                    newprompt =
                        strings.get("NewPasswordDefaultEmptyPrompt",
                                            passwordName);
                else
                    newprompt =
                        strings.get("NewPasswordDefaultPrompt",
                                            passwordName, defaultPassword);
            } else
                newprompt = strings.get("NewPasswordPrompt", passwordName);
        }

        String newpassword = readPassword(newprompt);

        /*
         * If we allow for a default password, and the user just hit "Enter",
         * return the default password.  No need to prompt twice or check
         * for validity.
         */
        if (defaultPassword != null) {
            if (newpassword == null)
                newpassword = "";
            if (newpassword.length() == 0) {
                newpassword = defaultPassword;
                passwords.put(passwordName, newpassword);
                return newpassword;
            }
        }

        /*
         * If not creating a new password, don't need to verify that
         * the user typed it correctly by making them type it twice,
         * and don't need to check it for validity.  Just return what
         * we have.
         */
        if (!create) {
            passwords.put(passwordName, newpassword);
            return newpassword;
        }

        String confirmationPrompt;
        if (ok(description)) {
            confirmationPrompt =
                strings.get("NewPasswordDescriptionConfirmationPrompt",
                            description);
        } else {
            confirmationPrompt =
                strings.get("NewPasswordConfirmationPrompt", passwordName);
        }
        String newpasswordAgain = readPassword(confirmationPrompt);
        if (!newpassword.equals(newpasswordAgain)) {
            throw new CommandValidationException(
                strings.get("OptionsDoNotMatch",
                            ok(description) ? description : passwordName));
        }
        passwords.put(passwordName, newpassword);
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
            char[] pc = cons.readPassword("%s", prompt);
            // yes, yes, yes, it would be safer to not keep it in a String
            password = new String(pc);
        }
        return password;
    }

    /**
     * Get an option value, that might come from the command line
     * or from the environment.  Return the default value for the
     * option if not otherwise specified.
     */
    protected String getOption(String name) {
        String val = options.get(name);
        if (val == null)
            val = env.getStringOption(name);
        if (val == null) {
            // no value, find the default
            for (ValidOption opt : commandOpts) {
                // XXX - hack alert!  the description is stored in the default
                // value for passwords
                if (opt.getType().equals("PASSWORD"))
                    continue;
                if (opt.getName().equals(name)) {
                    // if no value was specified and there's a default value,
                    // return it
                    if (opt.getDefaultValue() != null) {
                        val = opt.getDefaultValue();
                        break;
                    }
                }
            }
        }
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
                            strings.get("UnsupportedLegacyCommand", cmd));
            }
        }
        // it is a supported command; do nothing
    }

    protected static boolean ok(String s) {
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
