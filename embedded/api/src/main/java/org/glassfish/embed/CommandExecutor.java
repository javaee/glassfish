/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import com.sun.enterprise.v3.admin.CommandRunner;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import org.glassfish.api.ActionReport;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Generic API to execute asadmin CLI commands.
 * <p/>
 * <p/>
 * The {@link Server} must be started before constructing the <code>CommandExecutor</code>
 * and executing commands.
 * <xmp>
 * EmbeddedInfo ei = new EmbeddedInfo();
 * Server server = new Server(ei);
 * server.start();
 * CommandExecutor ce = new CommandExecutor(server);
 * </xmp>
 * @author Jennifer
 * @see <a href="http://docs.sun.com/app/docs/doc/820-4495/gcode?a=view">CLI commands</a>
 */
public class CommandExecutor {

    /**
     * Create <code>CommandExecutor</code> object using a {@link Server} object
     * that has already been started.
     *
     * @param server the server to execute commands on
     * @throws org.glassfish.embed.EmbeddedException
     */
    public CommandExecutor(Server server) throws EmbeddedException {
        try {
            cr = server.getHabitat().getComponent(CommandRunner.class);
        } catch (NullPointerException e) {
            throw new EmbeddedException("not_started", "CommandExecutor(Server server)");
        }
        this.serverName = server.getServerName();
    }

   /**
    * Executes the provided command.  If the command fails, EmbeddedException is thrown.
    *
    * <xmp>
    *   String commandName = "create-jdbc-connection-pool"
    *   Properties options = new Properties();
    *   options.setProperty("datasourceclassname", "org.apache.derby.jdbc.ClientDataSource");
    *   options.setProperty("isisolationguaranteed", "false");
    *   options.setProperty("restype", "javax.sql.DataSource");
    *   options.setProperty("property", "PortNumber=1527:Password=APP:User=APP:serverName=localhost:DatabaseName=sun-appserv-samples:connectionAttributes=\\;create\\\\=true");
    *   options.setProperty("DEFAULT", "DerbyPool");
    *
    *   ce.execute(commandName, options);
    *</xmp>
    *
    * @param commandName name of the command (e.g. "create-jdbc-resource")
    * @param options name/value pairs of the command options (e.g. connectionpoolid=DerbyPool)
    *  For operand use "DEFAULT" as the key name. (e.g. name of the JDBC resource, DEFAULT=jdbcA)
    * @throws EmbeddedException
    */
    public void execute(String commandName, Properties options) throws EmbeddedException {
        report = new PropsFileActionReporter();
        try {
            if (commandName != null && commandName.equals("deploy")) {
                Object path = options.get("DEFAULT");
                if (path != null) {
                    File f = new File((String)path);
                    if (f.exists()) {
                        ArrayList<File> list = new ArrayList();
                        list.add(f);
                        options.put("serverName", this.serverName);
                        cr.doCommand(commandName, options, report, list);
                    } else {
                        throw new EmbeddedException("no_such_file", f);
                    }
                } else {
                    throw new EmbeddedException("nothing_to_do");
                }
            } else {
                cr.doCommand(commandName, options, report);
            }
        } catch (Throwable t) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(t);
            report.setMessage(t.getLocalizedMessage());
            report.setActionDescription("Last-chance CommandExecutor exception handler");
        }

        exitCode = report.getActionExitCode();
        String msg = report.getMessage();
        Throwable t  = report.getFailureCause();
        if (exitCode.equals(exitCode.SUCCESS)) {
            LoggerHelper.info("command_successful", commandName);
            if (msg!=null) LoggerHelper.info(msg);
        } else if (exitCode.equals(exitCode.FAILURE)) {
            LoggerHelper.severe("command_failure", commandName);
            if (msg!=null) LoggerHelper.severe(msg);
            throw new EmbeddedException("command_failure", commandName, t);
        }
    }

    /**
     * <code>ActionReport</code> contains information about the execution of the
     * command.  This information includes command execution messages and exit
     * codes.
     *
     * @return the {@link ActionReport}
     */
    public ActionReport getReport() {
        return report;
    }

    /**
     * Returns the exit code from the command execution
     * <ul>
     * <li>0 = success</li>
     * <li>1 = failure</li>
     * </ul>
     * @return the exit code from command execution
     */
    public ActionReport.ExitCode getExitCode() {
        return report.getActionExitCode();
    }

    /**
     *
     * @return the message from the <code>ActionReport</code>
     */
    public String getMessage() {
        String msg = report.getMessage();
        return msg==null ? "" : msg;
    }

    /**
     *
     * @return the name of the server commands are being executed on
     */
    public String getServerName() {
        return serverName;
    }

    private CommandRunner cr;
    private ActionReport report;
    private ActionReport.ExitCode exitCode;
    private String serverName;
}
