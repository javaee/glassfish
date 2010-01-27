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

import java.util.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * Representation of the options known to the asadmin program.
 * These options control the overall behavior of asadmin, e.g.,
 * the server to contact, and aren't specific to any of the
 * commands supported by asadmin.
 * <p>
 * In GlassFish v3, asadmin program options are normally specified
 * before the asadmin command name, with command options after the
 * command name (although intermixed program and command options
 * are still supported for compatibility).
 */
public class ProgramOptions {

    public enum PasswordLocation {
        DEFAULT, USER, PASSWORD_FILE, LOGIN_FILE, LOCAL_PASSWORD
    };

    private static final Set<ValidOption> programOptions;

    // the known program option names
    public static final String HOST             = "host";
    public static final String PORT             = "port";
    public static final String USER             = "user";
    public static final String PASSWORDFILE     = "passwordfile";
    public static final String TERSE            = "terse";
    public static final String ECHO             = "echo";
    public static final String INTERACTIVE      = "interactive";
    public static final String SECURE           = "secure";
    public static final String HELP             = "help";

    private static final CLILogger logger = CLILogger.getInstance();

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ProgramOptions.class);

    private Map<String, String>             options;
    private Environment                     env;
    private boolean                         optionsSet;
    private String                          password;
    private PasswordLocation                location;

    /*
     * Information passed in from AsadminMain and used by start-domain.
     * XXX - this is somewhat of a kludge but this seems the best place
     * to put it for now
     */
    private String[]                        programArguments;
    private String                          classPath;
    private String                          className;

    /*
     * Define the meta-options known by the asadmin command.
     */
    static {
        Set<ValidOption> opts = new HashSet<ValidOption>();
        addMetaOption(opts, HOST, 'H', "STRING", false,
                CLIConstants.DEFAULT_HOSTNAME);
        addMetaOption(opts, PORT, 'p', "STRING", false,
                "" + CLIConstants.DEFAULT_ADMIN_PORT);
        addMetaOption(opts, USER, 'u', "STRING", false, null);
        addMetaOption(opts, PASSWORDFILE, 'W', "FILE", false, null);
        addMetaOption(opts, SECURE, 's', "BOOLEAN", false, "false");
        addMetaOption(opts, TERSE, 't', "BOOLEAN", false, "false");
        addMetaOption(opts, ECHO, 'e', "BOOLEAN", false, "false");
        addMetaOption(opts, INTERACTIVE, 'I', "BOOLEAN", false, "false");
        addMetaOption(opts, HELP, '?', "BOOLEAN", false, "false");
        programOptions = Collections.unmodifiableSet(opts);
    }

    /**
     * Helper method to define a meta-option.
     *
     * @param name  long option name
     * @param sname short option name
     * @param type  option type (STRING, BOOLEAN, etc.)
     * @param req   is option required?
     * @param def   default value for option
     */
    private static void addMetaOption(Set<ValidOption> opts, String name,
            char sname, String type, boolean req, String def) {
        ValidOption opt = new ValidOption(name, type,
                req ? ValidOption.REQUIRED : ValidOption.OPTIONAL, def);
        String abbr = Character.toString(sname);
        opt.setShortName(abbr);
        opts.add(opt);
    }

    /**
     * Initialize program options based only on environment defaults,
     * with no options from the command line.
     */
    public ProgramOptions(Environment env) throws CommandException {
        this(new HashMap<String, String>(), env);
        optionsSet = false;
    }

    /**
     * Initialize the programoptions based on parameters parsed
     * from the command line, with defaults supplied by the
     * environment.
     */
    public ProgramOptions(Map<String, String> options, Environment env)
            throws CommandException {
        this.env = env;
        updateOptions(options);
    }

    /**
     * Copy constructor.  Create a new ProgramOptions with the same
     * options as the specified ProgramOptions.
     */
    public ProgramOptions(ProgramOptions other) {
        this.options = new HashMap<String, String>(other.options);
        this.env = other.env;
        this.password = other.password;
        this.programArguments = other.programArguments;
        this.classPath = other.classPath;
        this.className = other.className;
    }

    /**
     * Update the program options based on the specified
     * options from the command line.
     */
    public void updateOptions(Map<String, String> newOptions)
            throws CommandException {
        if (options == null)
            options = newOptions;
        else
            options.putAll(newOptions); // merge in the new options
        optionsSet = true;

        // have to verify port value now
        String sport = options.get(PORT);
        if (ok(sport)) {
            String badPortMsg = strings.get("InvalidPortNumber", sport);
            try {
                int port = Integer.parseInt(sport);
                if (port < 1 || port > 65535)
                    throw new CommandException(badPortMsg);
            } catch (NumberFormatException e) {
                throw new CommandException(badPortMsg);
            }
        }
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    /**
     * Return a set of all the valid program options.
     *
     * @return the valid program options
     */
    public static Set<ValidOption> getValidOptions() {
        return programOptions;
    }

    /**
     * Copy the program options that were specified on the
     * command line into the corresponding environment variables.
     */
    public void toEnvironment(Environment env) {
        // copy all the parameters into corresponding environment variables
        putEnv(env, ECHO);
        putEnv(env, TERSE);
        putEnv(env, INTERACTIVE);
        putEnv(env, HOST);
        putEnv(env, PORT);
        putEnv(env, SECURE);
        putEnv(env, USER);
        putEnv(env, PASSWORDFILE);
        // XXX - HELP?
    }

    private void putEnv(Environment env, String name) {
        String value = options.get(name);
        if (value != null)
            env.putOption(name, value);
    }

    /**
     * @return the host
     */
    public String getHost() {
        String host = options.get(HOST);
        if (!ok(host))
            host = env.getStringOption(HOST);
        if (!ok(host))
            host = CLIConstants.DEFAULT_HOSTNAME;
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        options.put(HOST, host);
    }

    /**
     * @return the port
     */
    public int getPort() {
        int port;
        String sport = options.get(PORT);
        if (!ok(sport))
            sport = env.getStringOption(PORT);
        if (ok(sport)) {
            try {
                port = Integer.parseInt(sport);
                if (port < 1 || port > 65535)
                    port = -1;  // should've been verified in constructor
            } catch (NumberFormatException e) {
                port = -1;  // should've been verified in constructor
            }
        } else
            port = CLIConstants.DEFAULT_ADMIN_PORT; // the default port
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        options.put(PORT, Integer.toString(port));
    }

    /**
     * @return the user
     */
    public String getUser() {
        String user = options.get(USER);
        if (!ok(user))
            user = env.getStringOption(USER);
        if (!ok(user))
            user = null; // distinguish between specify the default explicitly
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        logger.printDebugMessage("Setting user to: " + user);
        options.put(USER, user);
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the password location
     */
    public PasswordLocation getPasswordLocation() {
        return location;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password, PasswordLocation location) {
        logger.printDebugMessage("Setting password to: " +
                                    (ok(password) ? "<non-null>" : "<null>"));
        this.password = password;
        this.location = location;
    }

    /**
     * @return the passwordFile
     */
    public String getPasswordFile() {
        String passwordFile = options.get(PASSWORDFILE);
        if (!ok(passwordFile))
            passwordFile = env.getStringOption(PASSWORDFILE);
	if (!ok(passwordFile))
	    passwordFile = null;	// no default
        return passwordFile;
    }

    /**
     * @param passwordFile the passwordFile to set
     */
    public void setPasswordFile(String passwordFile) {
        options.put(PASSWORDFILE, passwordFile);
    }

    /**
     * @return the secure
     */
    public boolean isSecure() {
        boolean secure;
        if (options.containsKey(SECURE)) {
            String value = options.get(SECURE);
            if (ok(value))
                secure = Boolean.parseBoolean(value);
            else
                secure = true;
        } else
            secure = env.getBooleanOption(SECURE);
        return secure;
    }

    /**
     * @param secure the secure to set
     */
    public void setSecure(boolean secure) {
        options.put(SECURE, Boolean.toString(secure));
    }

    /**
     * @return the terse
     */
    public boolean isTerse() {
        boolean terse;
        if (options.containsKey(TERSE)) {
            String value = options.get(TERSE);
            if (ok(value))
                terse = Boolean.parseBoolean(value);
            else
                terse = true;
        } else
            terse = env.getBooleanOption(TERSE);
        return terse;
    }

    /**
     * @param terse the terse to set
     */
    public void setTerse(boolean terse) {
        options.put(TERSE, Boolean.toString(terse));
    }

    /**
     * @return the echo
     */
    public boolean isEcho() {
        boolean echo;
        if (options.containsKey(ECHO)) {
            String value = options.get(ECHO);
            if (ok(value))
                echo = Boolean.parseBoolean(value);
            else
                echo = true;
        } else
            echo = env.getBooleanOption(ECHO);
        return echo;
    }

    /**
     * @param echo the echo to set
     */
    public void setEcho(boolean echo) {
        options.put(ECHO, Boolean.toString(echo));
    }

    /**
     * @return the interactive
     */
    public boolean isInteractive() {
        boolean interactive;
        if (options.containsKey(INTERACTIVE)) {
            String value = options.get(INTERACTIVE);
            if (ok(value))
                interactive = Boolean.parseBoolean(value);
            else
                interactive = true;
        } else if (env.hasOption(INTERACTIVE)) {
            interactive = env.getBooleanOption(INTERACTIVE);
        } else
            interactive = System.console() != null;
        return interactive;
    }

    /**
     * @param interactive the interactive to set
     */
    public void setInteractive(boolean interactive) {
        options.put(INTERACTIVE, Boolean.toString(interactive));
    }

    /**
     * @return the help
     */
    public boolean isHelp() {
        boolean help = false;
        if (options.containsKey(HELP)) {
            String value = options.get(HELP);
            if (ok(value))
                help = Boolean.parseBoolean(value);
            else
                help = true;
        } else
            help = env.getBooleanOption(HELP);
        return help;
    }

    /**
     * @param help the help to set
     */
    public void setHelp(boolean help) {
        options.put(HELP, Boolean.toString(help));
    }

    /**
     * @return were options set on the command line?
     */
    public boolean isOptionsSet() {
        return optionsSet;
    }

    /**
     * Set whether the program options have already been set.
     */
    public void setOptionsSet(boolean optionsSet) {
        this.optionsSet = optionsSet;
    }

    /**
     * @return the programArguments
     */
    public String[] getProgramArguments() {
        return programArguments;
    }

    /**
     * @param programArguments the programArguments to set
     */
    public void setProgramArguments(String[] programArguments) {
        this.programArguments = programArguments;
    }

    /**
     * @return the classPath
     */
    public String getClassPath() {
        return classPath;
    }

    /**
     * @param classPath the classPath to set
     */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * String representation of the asadmin program options.
     * Included in the --echo output.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ok(getHost()))
            sb.append("--host ").append(getHost()).append(' ');
        if (getPort() > 0)
            sb.append("--port ").append(getPort()).append(' ');
        if (ok(getUser()))
            sb.append("--user ").append(getUser()).append(' ');
        if (ok(getPasswordFile()))
            sb.append("--passwordfile ").
                append(getPasswordFile()).append(' ');
        if (isSecure())
            sb.append("--secure ");
        sb.append("--interactive=").
            append(Boolean.toString(isInteractive())).append(' ');
        sb.append("--echo=").
            append(Boolean.toString(isEcho())).append(' ');
        sb.append("--terse=").
            append(Boolean.toString(isTerse())).append(' ');
        sb.setLength(sb.length() - 1);  // strip trailing space
        return sb.toString();
    }
}
