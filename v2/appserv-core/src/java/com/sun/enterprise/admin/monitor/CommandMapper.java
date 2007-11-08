/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sun.enterprise.admin.common.MalformedNameException;
import com.sun.enterprise.admin.common.Name;
import com.sun.enterprise.admin.common.ObjectNames;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Maps commands to AdminCommand objects. The command objects encapsulate monitoring
 * functionality exposed to the user interface. 
 */
public class CommandMapper {

    /**
     * A map to track instance name and corresponding command mapper
     */
    private static HashMap instanceMap = new HashMap();

    /**
     * Name of the instance
     */
    private String instanceName;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( CommandMapper.class );

    /**
     * Private constructor. Use factory method to get an instance.
     */
    private CommandMapper() {
    }

    /**
     * Map CLI command and dotted name to a command object. 
     * @param command the cli command, valid values are CLI_COMMAND_GET and
     *     CLI_COMMAND_LIST
     * @param dottedName the dotted name to which this command applies
     * @throws IllegalArgumentException if the specified command is unknown
     * @throws MalformedNameException if specified dottedName is incorrect
     * @retun A command object that encapsulates the requested command
     */
    public MonitorCommand mapCliCommand(String command, String dottedName)
            throws MalformedNameException {
        MonitorCommand monitorCommand = null;
        if (CLI_COMMAND_GET.equalsIgnoreCase(command)) {
            monitorCommand = mapGetCommand(dottedName);
        } else if (CLI_COMMAND_LIST.equalsIgnoreCase(command)) {
            monitorCommand = mapListCommand(dottedName);
        } else {
			String msg = localStrings.getString( "admin.monitor.unknown_cli_command", command );
            throw new IllegalArgumentException( msg );
        }
        return monitorCommand;
    }

    /**
     * Map CLI get command to a command object.
     *
     * A dotted name for get command is of the form
     * <code>instanceName([.type[.name])*.(star|attrName)</code> where,
     * instanceName is name of server instance, type is derived from
     * MonitoredObjectType, name is monitored component name, star is
     * the character <code>*</code> (denotes all attributes) and attrName is
     * the name of the attribute to get.
     * @param dottedName dotted name to get.
     * @throws MalformedNameException if the specified dotted name can not be
     *    used for a get command.
     * @retun A command object that encapsulates the requested get command
     */
    public MonitorGetCommand mapGetCommand(String dottedName)
            throws MalformedNameException {
        ParsedDottedName result = parseDottedName(dottedName, CLI_COMMAND_GET);
        MonitorGetCommand command;
        if (result.monitoredObjectType != null) {
            command = new MonitorGetCommand(result.objectName,
                    result.monitoredObjectType, result.attributeName);
        } else {
            command = new MonitorGetCommand(result.objectName,
                    result.attributeName);
        }
        return command;
    }

    /**
     * Map CLI list command to a command object.
     *
     * A dotted name for list command is of the form
     * <code>instanceName([.type[.name])*</code> where, instanceName is name
     * of server instance, type is derived from MonitoredObjectType, name is
     * monitored component name.
     * @param dottedName dotted name to list.
     * @throws MalformedNameException if the specified dotted name can not be
     *    used for a list command.
     * @retun A command object that encapsulates the requested list command
     */
    public MonitorListCommand mapListCommand(String dottedName)
            throws MalformedNameException {
        ParsedDottedName result = parseDottedName(dottedName, CLI_COMMAND_LIST);
        MonitorListCommand command;
        if (result.monitoredObjectType != null) {
            command = new MonitorListCommand(result.objectName,
                    result.monitoredObjectType);
        } else {
            command = new MonitorListCommand(result.objectName);
        }
        return command;
    }

    public MonitorSetCommand mapSetCommand(String dottedName, Object args)
            throws MalformedNameException {
        ParsedDottedName result = parseDottedName(dottedName, CLI_COMMAND_SET);                
        MonitorSetCommand command = null;
        String argsStr = (String)args;
        StringTokenizer st = new StringTokenizer(argsStr, ",");
        String[] commandArgs = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()){
            commandArgs[i] = st.nextToken();
            i++;
        }
        command = new MonitorSetCommand(result.objectName,
                    result.monitoredObjectType, result.operationName,
                    commandArgs);    
        return command;
    }
    
    /**
     * Get command mapper for specified server instance. This is the factory
     * method to be used to obtain instances of command mapper.
     * @param instanceName name of the instance
     * @return a command mapper for specified instance
     */
    public static synchronized CommandMapper getInstance(String instanceName) {
        CommandMapper cm = (CommandMapper)instanceMap.get(instanceName);
        if (cm == null) {
            cm = new CommandMapper();
            cm.instanceName = instanceName;
            instanceMap.put(instanceName, cm);
        }
        return cm;
    }

    /**
     * Parse dotted name for the specified command. This method splits the
     * dotted string into tokens and then invokes parseTokens().
     * @param dottedString the dotted name
     * @param command the CLI command
     * @return parsed dotted name as an object
     */
    private ParsedDottedName parseDottedName(String dottedString, String command)
           throws MalformedNameException {
        ArrayList tokenList = new ArrayList();
        Name dottedName = new Name(dottedString);
        int  nTokens = dottedName.getNumParts();
        if (nTokens < 1) {
			String msg = localStrings.getString( "admin.monitor.name_does_not_contain_any_tokens", dottedString );
            throw new MalformedNameException( msg );
        }
        for (int j = 0; j < nTokens; j++) {
            tokenList.add(dottedName.getNamePart(j).toString());
        }
        return parseTokens(tokenList, command, dottedString);
    }

    /**
     * Parse tokens derived from the dotted name for specified CLI command. This
     * method tries to derive MBean object name and other parameters for the
     * specified command.
     * @param tokenList list of tokens
     * @param command the CLI command
     * @param dottedName the dotted name, this is used only for generating
     *     the message for MalformedNameException
     * @return parsed dotted name as an object
     * @throws MalformedNameException if any of the type specified in CLI dotted
     *    name is invalid, or if JMX object name can not be created using
     *    specified dotted name (for example - if the dotted name contains a
     *    comma).
     */
    private ParsedDottedName parseTokens(ArrayList tokenList, String command,
            String dottedName) throws MalformedNameException {
        Properties props = new Properties();
        props.put(ObjectNames.kTypeKeyName, ObjectNames.kMonitoringType);
        props.put(ObjectNames.kNameKeyName, ObjectNames.kMonitoringRootClass);
        props.put(ObjectNames.kMonitoringClassName,
                ObjectNames.kMonitoringRootClass);
        props.put(ObjectNames.kServerInstanceKeyName, instanceName);
        int count = tokenList.size();
        MonitoredObjectType type = null;
        // 1st token is name of the instance, ignore.
        // Process 2nd token onwards - either singleton-type or type.name. The
        // last token is a special case because it can be a wildcard or
        // attribute name for get command, a type or name for list command
        int tokenCount = count;
        if (command.equals(CLI_COMMAND_GET) || command.equals(CLI_COMMAND_SET)) {
            tokenCount = count - 1;
        }
        boolean processType = true;
        for (int i = 1; i < tokenCount; i++) {
            String token = (String)tokenList.get(i);
            if (processType) {
                type = MonitoredObjectType.getMonitoredObjectTypeOrNull(token);
                if (type == null) {
					String msg = localStrings.getString( "admin.monitor.invalid_entry", dottedName, token );
                    throw new MalformedNameException( msg );
                }
                String typeName = type.getTypeName();
                if (type.isSingleton()) {
                    swapNameType(props, typeName, typeName);
                    processType = true;
                    type = null;
                } else {
                    processType = false;
                }
            } else {
                swapNameType(props, type.getTypeName(), token);
                processType = true;
                type = null;
            }
        }
        ParsedDottedName result = new ParsedDottedName();
        try {
            result.objectName = new ObjectName(ObjectNames.kDefaultIASDomainName,
                    props);
        } catch (MalformedObjectNameException ione) {
            throw new MalformedNameException(ione.getMessage());
        }
        // type != null -- Means that a name was expected in parsing. For
        // LIST command implies list of objects of this type. For GET command
        // implies all specified attribute(or wildcard) of on specified type of
        // objects.
        if (type != null) {
            result.monitoredObjectType = type;
        }
        if (command.equals(CLI_COMMAND_GET)) {
            result.attributeName = (String)tokenList.get(count -1);
        } else if (command.equals(CLI_COMMAND_SET))
            result.operationName = (String)tokenList.get(count -1);
        
        return result;
    }

    /**
     * Apply a transform to specified properties. The transform is to extract
     * properties "name" and "type" and put the value of type as key and
     * value of name as correponding value and then replacing values of "name"
     * and "type" by specified new values (newName and newType).
     * @param props the property list
     * @param newType new value for property "type"
     * @param newName new value for property "name"
     */
    private void swapNameType(Properties props, String newType, String newName) {
        String oldType = props.getProperty(ObjectNames.kMonitoringClassName);
        String oldName = props.getProperty(ObjectNames.kNameKeyName);
        props.put(ObjectNames.kMonitoringClassName, newType);
        props.put(ObjectNames.kNameKeyName, newName);
        props.put(oldType, oldName);
    }

    /**
     * Constant to denote CLI get command
     */
    public static final String CLI_COMMAND_GET = "GET";

    /**
     * Constant to denote CLI list command
     */
    public static final String CLI_COMMAND_LIST = "LIST";

    /**
     * Constant to denote CLI set command
     */
    public static final String CLI_COMMAND_SET = "SET"; 
}

/**
 * Parsed dotted name. A parsed dotted name has three components - JMX object
 * name, sub monitored object type (if any) and attribute name (if any).
 */
class ParsedDottedName {
    ObjectName objectName;
    MonitoredObjectType monitoredObjectType;
    String attributeName;
    String operationName;
}
