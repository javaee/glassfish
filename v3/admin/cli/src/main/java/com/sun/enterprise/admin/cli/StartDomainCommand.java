package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.glassfish.bootstrap.Main;
import java.io.File;
import java.lang.reflect.Method;

public class StartDomainCommand extends Command
{
    private static final String GLASSFISH_V3_JAR = "glassfish-10.0-SNAPSHOT.jar";
    private static final String VERBOSE = "verbose";
    
    public boolean validateOptions() throws CommandValidationException
    {
        return true;
    }

    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
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
                                                              new Object[] {name}), e);
            }
        }
    }


    /**
     *  defines the command to start the domain
     */
    private String[] startDomainCmd() throws Exception
    {
        final String root = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        return new String [] {
            System.getProperty("java.home")+File.separator+"bin"+File.separator+"java",
            "-jar",
            root+File.separator+"lib"+File.separator+GLASSFISH_V3_JAR
            };
    }
}


