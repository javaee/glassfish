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
import com.sun.enterprise.cli.framework.*;
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
 * are still supported for comaptibility).
 */
public class ProgramOptions {

    private static final Set<ValidOption> programOptions;
    private static final Map<String, String> emptyOptions;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ProgramOptions.class);

    private Map<String, String>             options;
    private String                          host;
    private int                             port;
    private String                          user;
    private String                          password;
    private String                          passwordFile;
    private boolean                         secure = false;
    private boolean                         terse = false;
    private boolean                         echo = false;
    private boolean                         interactive = false;
    private boolean                         help = false;

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
        addMetaOption(opts, "host", 'H', "STRING", false,
                CLIConstants.DEFAULT_HOSTNAME);
        addMetaOption(opts, "port", 'p', "STRING", false,
                "" + CLIConstants.DEFAULT_ADMIN_PORT);
        addMetaOption(opts, "user", 'u', "STRING", false, "anonymous");
        addMetaOption(opts, "password", 'w', "STRING", false, null);
        addMetaOption(opts, "passwordfile", 'W', "FILE", false, null);
        addMetaOption(opts, "secure", 's', "BOOLEAN", false, "false");
        addMetaOption(opts, "terse", 't', "BOOLEAN", false, "false");
        addMetaOption(opts, "echo", 'e', "BOOLEAN", false, "false");
        addMetaOption(opts, "interactive", 'I', "BOOLEAN", false, "false");
        addMetaOption(opts, "help", '?', "BOOLEAN", false, "false");
        programOptions = Collections.unmodifiableSet(opts);
        emptyOptions = Collections.emptyMap();
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
        this(emptyOptions, env);
    }

    /**
     * Initialize the programoptions based on parameters parsed
     * from the command line, with defaults supplied by the
     * environment.
     */
    public ProgramOptions(Map<String, String> options, Environment env)
            throws CommandException {
        this.options = options;
        if (options.containsKey("echo")) {
            String value = options.get("echo");
            if (ok(value))
                echo = Boolean.parseBoolean(value);
            else
                echo = true;
        } else
            echo = env.getBooleanOption("echo");
        if (options.containsKey("terse")) {
            String value = options.get("terse");
            if (ok(value))
                terse = Boolean.parseBoolean(value);
            else
                terse = true;
        } else
            terse = env.getBooleanOption("terse");
        if (options.containsKey("interactive")) {
            String value = options.get("interactive");
            if (ok(value))
                interactive = Boolean.parseBoolean(value);
            else
                interactive = true;
        } else if (env.hasOption("interactive")) {
            interactive = env.getBooleanOption("interactive");
        } else
            interactive = System.console() != null;

        if (options.containsKey("help"))
            help = true;    // don't care about the value

        host = options.get("host");
        if (!ok(host))
            host = env.getStringOption("host");

        if (host == null || host.length() == 0)
            host = CLIConstants.DEFAULT_HOSTNAME;

        String sport = options.get("port");
        if (!ok(sport))
            sport = env.getStringOption("port");
        if (ok(sport)) {
            String badPortMsg = strings.get("badport", sport);
            try {
                port = Integer.parseInt(sport);
                if (port < 1 || port > 65535)
                    throw new CommandException(badPortMsg);
            } catch (NumberFormatException e) {
                throw new CommandException(badPortMsg);
            }
        } else
            port = CLIConstants.DEFAULT_ADMIN_PORT; // the default port

        if (options.containsKey("secure")) {
            String value = options.get("secure");
            if (ok(value))
                secure = Boolean.parseBoolean(value);
            else
                secure = true;
        } else
            secure = env.getBooleanOption("secure");

        if (options.containsKey("user")) {
            String value = options.get("user");
            if (ok(value))
                user = value;
        } else
            user = env.getStringOption("user");

        if (options.containsKey("passwordfile")) {
            String value = options.get("passwordfile");
            if (ok(value))
                passwordFile = value;
        } else
            passwordFile = env.getStringOption("passwordfile");
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
        putEnv(env, "echo");
        putEnv(env, "terse");
        putEnv(env, "interactive");
        putEnv(env, "host");
        putEnv(env, "port");
        putEnv(env, "secure");
        putEnv(env, "user");
        // XXX - "help"?
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
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the passwordFile
     */
    public String getPasswordFile() {
        return passwordFile;
    }

    /**
     * @param passwordFile the passwordFile to set
     */
    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    /**
     * @return the secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * @param secure the secure to set
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * @return the terse
     */
    public boolean isTerse() {
        return terse;
    }

    /**
     * @param terse the terse to set
     */
    public void setTerse(boolean terse) {
        this.terse = terse;
    }

    /**
     * @return the echo
     */
    public boolean isEcho() {
        return echo;
    }

    /**
     * @param echo the echo to set
     */
    public void setEcho(boolean echo) {
        this.echo = echo;
    }

    /**
     * @return the interactive
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * @param interactive the interactive to set
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * @return the help
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * @param help the help to set
     */
    public void setHelp(boolean help) {
        this.help = help;
    }

    /**
     * @return were options set on the command line?
     */
    public boolean isOptionsSet() {
        return options != emptyOptions;
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
}
