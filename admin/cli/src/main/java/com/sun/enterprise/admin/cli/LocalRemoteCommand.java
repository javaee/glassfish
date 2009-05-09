/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.cli.remote.CLIRemoteCommand;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.*;

/**
 * A handy base class for commands that have both a local and remote component.
 *
 * @author bnevins
 */

public abstract class LocalRemoteCommand extends AbstractCommand{
    CLIRemoteCommand runRemoteCommand(String commandName, String... args) throws CommandException {
        try {
            CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            CLIRemoteCommand rc = new CLIRemoteCommand(prepareRemoteCommand(commandName, args));
            rc.runCommand();
            return rc;
        }
        catch(Exception e) {
            throw new CommandException(strings.get("LocalRemoteCommand.errorRemote", e.getMessage()));
        }
        finally {
            CLILogger.getInstance().popAndUnlockLevel();
        }
    }

    private String[] prepareRemoteCommand(String commandName, String... args) {
        List<String> list = new ArrayList<String>();
        list.add(commandName);
        addMetaArgs(list);

        for(String arg : args) {
            list.add(arg);
        }

        return list.toArray(new String[list.size()]);
    }


    private void addMetaArgs(List<String> list) {
        port         = getOption(PORT);
        host         = getOption(HOST);
        user         = getOption(USER);
        passwordFile = getOption(PASSWORDFILE);

        if(ok(port)) {
            list.add("--port");
            list.add(port);
        }
        if(ok(host)) {
            list.add("--host");
            list.add(host);
        }
        if(ok(user)) {
            list.add("--user");
            list.add(user);
        }
        if(ok(passwordFile)) {
            list.add("--passwordfile");
            list.add(passwordFile);
        }
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    private String port;
    private String host;
    private String user;
    private String passwordFile;
    final static LocalStringsImpl strings = new LocalStringsImpl(LocalRemoteCommand.class);
    final static CLILogger logger = CLILogger.getInstance();
}
