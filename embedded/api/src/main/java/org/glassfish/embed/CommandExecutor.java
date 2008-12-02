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
 *
 * @author Jennifer
 */
public class CommandExecutor {

    public CommandExecutor(Server server) throws EmbeddedException {
        cr = server.getHabitat().getComponent(CommandRunner.class);
        this.serverName = server.getServerName();
    }

   /**
    * Executes the provided command.  If the command fails, EmbeddedException is thrown.
    *
    * Example:
    *   String commandName = "create-jdbc-connection-pool"
    *   Properties options = new Properties();
    *   options.setProperty("datasourceclassname", "org.apache.derby.jdbc.ClientDataSource");
    *   options.setProperty("isisolationguaranteed", "false");
    *   options.setProperty("restype", "javax.sql.DataSource");
    *   options.setProperty("property", "PortNumber=1527:Password=APP:User=APP:serverName=localhost:DatabaseName=sun-appserv-samples:connectionAttributes=\\;create\\\\=true");
    *   options.setProperty("DEFAULT", "DerbyPool");
    *
    * @param commandName name of the command (e.g. "create-jdbc-resource")
    * @param options name/value pairs of the command options (e.g. connectionpoolid=DerbyPool)
    *  For operand use "DEFAULT" as the key name. (e.g. name of the JDBC resource, DEFAULT=jdbcA)
    */
    public void execute(String commandName, Properties options) throws EmbeddedException {
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

    public ActionReport getReport() {
        return report;
    }

    public ActionReport.ExitCode getExitCode() {
        return report.getActionExitCode();
    }

    public String getMessage() {
        String msg = report.getMessage();
        return msg==null ? "" : msg;
    }

    public String getServerName() {
        return serverName;
    }

    private CommandRunner cr;
    private ActionReport report = new PropsFileActionReporter();
    private ActionReport.ExitCode exitCode;
    private String serverName;
}
