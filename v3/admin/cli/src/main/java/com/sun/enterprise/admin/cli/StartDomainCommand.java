package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.glassfish.bootstrap.Main;
import java.io.File;
import java.util.logging.*;

public class StartDomainCommand extends Command {

    private static final String GLASSFISH_V3_JAR = "glassfish-10.0-SNAPSHOT.jar";
    private static final String VERBOSE = "verbose";

    public boolean validateOptions() throws CommandValidationException {
        return true;
    }

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    @Deprecated
    public void runCommand() throws CommandException, CommandValidationException {
        if (getBooleanOption(VERBOSE)) {
            //not sure what arguments are needed
            //for now, just send an empty String array
            (new Main()).run(new String[]{});
        }
        else {
            try {
                new CLIProcessExecutor().execute(startDomainCmd(), false);
                //exit the daemon thread
                System.exit(0);
            }
            catch (Exception e) {
                throw new CommandException(getLocalizedString("CommandUnSuccessful",
                        new Object[]{name}), e);
            }
        }
    }

/*
    public void runCommandnew() throws CommandException, CommandValidationException {
        try {
            // temporary TODO TODO
            // temporary TODO TODO
            // temporary TODO TODO

            // Launcher currently supports embedded only WBN 3/5/08
            if (getBooleanOption("embedded") == false && getBooleanOption(VERBOSE) == false) {
                runCommandOld();
            }

            // temporary TODO TODO
            // temporary TODO TODO
            // temporary TODO TODO
            // temporary TODO TODO
            // temporary TODO TODO


            GFLauncher launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.domain);
            GFLauncherInfo info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName((String) operands.firstElement());
            }

            String parent = getOption("domaindir");

            if (parent != null) {
                info.setDomainParentDir(parent);
            }

            info.setVerbose(getBooleanOption("verbose"));
            info.setDebug(getBooleanOption("debug"));
            info.setEmbedded(getBooleanOption("embedded"));
            launcher.launch();
        }
        catch (GFLauncherException ex) {
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
                    new Object[]{name, ex}), ex);
        }
    }
	*/

    /**
     *  defines the command to start the domain
     */
    @Deprecated
    private String[] startDomainCmd() throws Exception {
        final String root = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        return new String[]{
            System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
            "-jar",
            root + File.separator + "modules" + File.separator + GLASSFISH_V3_JAR
        };
    }
}


