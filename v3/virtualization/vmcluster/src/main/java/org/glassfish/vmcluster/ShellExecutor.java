package org.glassfish.vmcluster;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.vmcluster.config.Action;
import org.glassfish.vmcluster.config.Virtualization;
import org.glassfish.vmcluster.config.Virtualizations;
import org.glassfish.vmcluster.util.RuntimeContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Sep 14, 2010
 * Time: 2:18:35 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class ShellExecutor {

    final boolean debug = false;

    @Inject
    CommandRunner commandRunner;

    @Inject
    Virtualizations virtualizations;

    @Inject
    ServerEnvironment env;

    final Logger logger = RuntimeContext.logger;

    public String output(Process pr) throws IOException {
        // dirty hack for the lazy
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line=buf.readLine())!=null) {
            if (debug) System.out.println(line);
            sb.append(line);
        }
        return sb.toString();
    }

    public String error(Process pr) throws IOException {
        // dirty hack for the lazy
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line=buf.readLine())!=null) {
            if (debug) System.out.println(line);
            sb.append(line);
        }
        return sb.toString();
    }

    public Process execute(File dir, ShellCommand command) throws IOException, InterruptedException {
        String commandString = command.build();
        return executeAndWait(dir, commandString);
    }

    public Process executeAndWait(File dir, String command) throws IOException, InterruptedException {
        Process pr = execute(dir, command);
        pr.waitFor();
        return pr;
    }

    public Process execute(File dir, String command) throws IOException, InterruptedException {
        Runtime run = Runtime.getRuntime();
        dir = dir==null?new File(System.getProperty("user.dir")):dir;
        return  run.exec(dir.getAbsolutePath() + "/" + command);
    }

    public void executionActions(ActionReport report, String provider, Action.Timing timing, ParameterResolver resolver) {

        // now we configure it with all cluster creation commands
        // cluster is now created, let's run the cluster creation related actions...
        for (Virtualization virtualization : virtualizations.getVirtualizations()) {
            if (provider.equals(virtualization.getName())) {
                for (Action action : virtualization.getActions()) {

                    if (action.getTiming().equals(timing.name())) {
                        String path = virtualization.getScriptsLocation();
                        if (path==null) {
                            path = (new File(env.getConfigDirPath(), provider)).getAbsolutePath();
                        }
                        // the script location might be an absolute path
                        File script = new File(action.getCommand());
                        if (!script.exists()) {
                            // not an absolute path, must be in our config provider directory
                            script = new File(path, action.getCommand());
                            if (!script.exists()) {
                                logger.log(Level.SEVERE, "Cannot find script " + action.getCommand() + " in " + path);
                                return;
                            }
                        }
                        ShellCommand command = new ShellCommand(path, action, resolver);
                        logger.info("Running " + command.build());
                        try {
                            Process result = execute(null, command);
                            if (result.exitValue()!=0) {
                                logger.info("Command failed with exit code " + result.exitValue());
                                String output = output(result);
                                if (output!=null)
                                    logger.severe(output);
                            }
                        } catch (InterruptedException e) {
                            report.failure(logger, "Interrupted exception while running " + command.build(), e);
                        } catch (IOException ioe) {
                            report.failure(logger, "IOException while running " + command.build(), ioe);
                        }
                    }
                }
            }
        }
    }

    public boolean executeAdminCommand(ActionReport report, String commandName, String operand, String... parameters) {

        ParameterMap params = new ParameterMap();
        if (operand!=null) {
            params.add("DEFAULT", operand);
        }
        for (int i=0;i<parameters.length;) {
            String key = parameters[i++];
            String value=null;
            if (i<parameters.length) {
                value = parameters[i++];
            }
            params.add(key, value);
        }
        CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation(commandName, report);
        inv.parameters(params);
        inv.execute();
        return (report.getActionExitCode()==ActionReport.ExitCode.SUCCESS);
    }

    public void installScript(File destDir, String provider, String scriptName) throws IOException {

        URL url = getClass().getClassLoader().getResource(provider+"/"+scriptName);
        if (url==null) {
            return;
        }
        InputStream is=null;
        OutputStream os=null;
        try {
            is = url.openStream();
            File outDir = new File(destDir, provider);
            os = new BufferedOutputStream(new FileOutputStream(new File(outDir, scriptName)));
            byte[] mem = new byte[2048];
            int read=0;
            do {
                read = is.read(mem);
                if (read>0)
                    os.write(mem, 0, read);
            } while (read>0);
        } finally {
            if (is!=null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Cannot close input file", e);
                }
            }
            if (os!=null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Cannot close output file", e);
                }
            }
        }

    }
}
