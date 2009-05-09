/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import com.sun.enterprise.v3.common.PropsFileActionReporter;
import org.glassfish.api.ActionReport;

/**
 * Holds information about the command execution, such as the exit code and
 * message.
 *
 * <xmp>
 * CommandExecution ce = server.execute("deploy", myWar);
 * ActionReport.ExitCode exitCode = ce.getExitCode();
 * String msg = ce.getMessage();
 * </xmp>
 * @author Jennifer
 */
public class CommandExecution {

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
    public ActionReport getActionReport() {
        return report;
    }

    /**
     * Returns the exit code from the command execution.  This method is called
     * after <code>Server execute(String commandName, CommandParameters params)</code>
     * to retrieve an <code>org.glassfish.api.ActionReport.ExitCode</code>
     * from the command.
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
     * called after <code>Server execute(String commandName, CommandParameters params)</code>
     * to retrieve a message from the command.
     * If this method returns an empty string, either no command was executed or
     * the command did not set any message on the <code>org.glassfish.api.ActionReport</code>
     *
     * @return the message from the <code>org.glassfish.api.ActionReport</code>
     */
    public String getMessage() {
        String msg = report.getMessage();
        return msg==null ? "" : msg;
    }

    void setActionReport(ActionReport report) {
        this.report = report;
    }

    void setExitCode(ActionReport.ExitCode exitCode) {
        this.report.setActionExitCode(exitCode);
    }

    void setMessage(String msg) {
        this.report.setMessage(msg);
    }

    private ActionReport report = new PropsFileActionReporter();
}
