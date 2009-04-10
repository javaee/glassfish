/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import java.util.Properties;

/**
 * Used in <code>Server execute(String commandName, CommandParameters params)</code>
 * to pass the <code>asadmin</code> command parameters.
 *
 * @author Jennifer
 * @see <a href="http://docs.sun.com/app/docs/doc/820-4495/gcode?a=view">asadmin commands</a>
 */
public class CommandParameters {
    /**
     * Sets the operand of an <code>asadmin</code> command to be executed on
     * <code>Server</code>.
     *
     * <xmp>
     * CommandParameters cp = new CommandParameters();
     * File myWar = new File("c:\samples\hello.war");
     * cp.setOperand(myWar);
     * CommandExecution ce = server.execute("deploy", cp);
     * </xmp>
     *
     * The command is <code>deploy</code>. The operand is <code>myWar</code>
     *
     * <code>asadmin deploy</code> command:
     * <xmp>
     *  asadmin deploy c:\samples\hello.war
     * </xmp>
     * 
     * @param operand operand of an <code>asadmin</code> command
     */
    public void setOperand(String operand) {
        params.setProperty(DEFAULT_OPERAND, operand);
    }

    /**
     * Set an option of an asadmin command to be executed on <code>Server</code>.
     *
     * <xmp>
     * CommandParameters cp = new CommandParameters();
     * cp.setOperand("jdbc/__default");
     * cp.setOption("connectionpoolid", "DerbyPool");
     * ce = myGF.execute("create-jdbc-resource", cp);
     * </xmp>
     *
     * An option of the <code>asadmin</code> command <code>create-jdbc-connection-pool</code>
     * is <code>datasourceclassname</code>.  Call <code>setOption("datasourceclassname", "myDataSourceClass")</code>
     * to set the <code>datasourceclassname</code> option.
     *
     * @param optionName option name of an <code>asadmin</code> command
     * @param optionValue option value of an <code>asadmin</code> command
     */
    public void setOption(String optionName, String optionValue) {
        params.setProperty(optionName, optionValue);
    }

    Properties getParams() {
        return params;
    }

    private String DEFAULT_OPERAND = "DEFAULT";
    private Properties params = new Properties();
}
