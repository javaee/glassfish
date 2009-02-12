/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import com.sun.enterprise.v3.admin.CommandRunner;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import org.glassfish.api.ActionReport;
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
 * CommandExecutor ce = server.getCommandExecutor();
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
    CommandExecutor(Server server) throws EmbeddedException {
        try {
            cr = server.getHabitat().getComponent(CommandRunner.class);
        } catch (NullPointerException e) {
            throw new EmbeddedException("not_started", "CommandExecutor(Server server)");
        }
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
             cr.doCommand(commandName, options, report);
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
            if (msg!=null)
                LoggerHelper.info(msg);
        } else if (exitCode.equals(exitCode.FAILURE)) {
            LoggerHelper.severe("command_failed", commandName);
            if (msg!=null) 
                LoggerHelper.severe(msg);

            if (t == null) {
                throw new EmbeddedException("command_failed", commandName);
            } else {
                throw new EmbeddedException(t, "command_failed", commandName);
            }
        }
    }

    /**
     * <code>org.glassfish.api.ActionReport</code> contains information about
     * the execution of the command.  The content of the
     * <code>org.glassfish.api.ActionReport</code> is set by the individual 
     * commands.  This information may include command execution
     * messages and exit codes.  This method is called after {@link execute} to
     * retrieve an <code>ActionReport</code> that has been populated by the command.
     * It will be empty if no command has been executed.
     *
     * Example of how to use <code>org.glassfish.api.ActionReport</code> with a list command.
     *
     * <xmp>
     *  ce.execute("list-jdbc-connection-pools", options);
        ActionReport report = ce.getReport();
        List<org.glassfish.api.ActionReport.MessagePart> list = report.getTopMessagePart().getChildren();
        for (org.glassfish.api.ActionReport.MessagePart mp : list) {
            System.out.println(mp.getMessage());
        }
     * </xmp>
     *
     * @return the {@link org.glassfish.api.ActionReport}
     */
    public ActionReport getReport() {
        return report;
    }

    /**
     * Returns the exit code from the command execution.  This method is called
     * after {@link execute} to retrieve an
     * <code>org.glassfish.api.ActionReport.ExitCode</code> from the command.
     * <ul>
     * <li>SUCCESS</li>
     * <li>FAILURE</li>
     * </ul>
     * @return the exit code from the <code>org.glassfish.api.ActionReport</code>
     */
    public ActionReport.ExitCode getExitCode() {
        return report.getActionExitCode();
    }

    /**
     * Returns the message if any from the command execution.  This method is
     * called after {@link execute} to retrieve a message from the command.
     * If this method returns an empty string, either no command was executed or
     * the command did not set any message on the <code>org.glassfish.api.ActionReport</code>
     *
     * @return the message from the <code>org.glassfish.api.ActionReport</code>
     */
    public String getMessage() {
        String msg = report.getMessage();
        return msg==null ? "" : msg;
    }

    private CommandRunner cr;
    private ActionReport report;
    private ActionReport.ExitCode exitCode;
}
