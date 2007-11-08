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

package com.sun.enterprise.diagnostics.collect;

import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.Defaults;
import com.sun.enterprise.diagnostics.Constants;
import com.sun.enterprise.admin.server.core.jmx.AppServerMBeanServerFactory;
import com.sun.logging.LogDomains;


import javax.management.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;

/**
 * To collect monitoring information of application server components,
 * applications deployed, jvm etc.,
 *
 * @author Jagadish Ramu
 */
public class MonitoringInfoCollector extends InterruptableCollector {

    Set<String> restrictedProperties = null;    // Properties that need
    //  to be discarded

    public static final String DOTTED_NAME_REGISTRY_OPERATION_NAME =
            "dottedNameToObjectName";
    public static final String KEY_NOT_FOUND = "key not found";
    public static final String LIST_COMMAND = "list";
    public static final String GET_COMMAND = "get";
    public static final String MONITOR_OPTION = "monitor";

    private String instanceName;
    private String nodeAgentName;

    private PrintStream out = System.out;

    private String fileName;
    private String destFolder;

    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    /**
     * Creates a new instance of MonitoringInfoCollector
     *
     * @param instanceName - name of the instacne
     * @param destFolder   - Destination folder in which generated
     *  report is stored
     */
    public MonitoringInfoCollector(String nodeAgentName, String instanceName, String destFolder) {
        if (instanceName != null) {
            this.instanceName = instanceName;
        }
        if(nodeAgentName !=null){
            this.nodeAgentName = nodeAgentName;
        }
        this.destFolder = destFolder;

        initializeRestrictedPropertiesLookUp();
    }

    /**
     * Initialize the list of properties that need to be ignored.
     */
    private void initializeRestrictedPropertiesLookUp() {
        restrictedProperties = new HashSet<String>();
        restrictedProperties.add("-description");
        restrictedProperties.add("-name");
        restrictedProperties.add("-lowerbound");
        restrictedProperties.add("-starttime");
        restrictedProperties.add("-upperbound");
    }

    /**
     * To check whether a particular property is in restricted list
     *
     * @param dottedName - property name
     * @return boolean
     */
    private boolean isRestircted(String dottedName) {
        boolean restricted = false;

        if (dottedName != null) {
            int index = dottedName.lastIndexOf("-");
            if (index >= 0) {
                String property = dottedName.substring(index);
                if (restrictedProperties != null) {
                    restricted = restrictedProperties.contains(property);
                }
            }
        }
        return restricted;
    }

    /**
     * Capture information
     *
     * @ throw DiagnosticException
     */
    public Data capture() throws DiagnosticException {
        FileData data = null;

        if (destFolder != null) {
            File destFolderObj = new File(destFolder);
            fileName = destFolder + File.separator + 
                    Defaults.MONITORING_INFO_FILE;

            if (!destFolderObj.exists()) {
                destFolderObj.mkdirs();
            }

            try {
                out = new PrintStream(
                        new BufferedOutputStream(
                                new FileOutputStream(fileName)), true);


                    File reportFile = new File(fileName);
                    if(this.instanceName.equalsIgnoreCase(Constants.SERVER))  {
                        data = new FileData( reportFile.getName(), DataType.MONITORING_INFO);
                    }else{
                        data = new FileData(nodeAgentName + File.separator + instanceName + File.separator  + reportFile.getName(), DataType.MONITORING_INFO);
                    }

                ArrayList<String> cmdOutput = new ArrayList<String>();
                MonitoringInfoHelper cmd = new MonitoringInfoHelper();
                cmd.setName(LIST_COMMAND);

                ArrayList<String> dottedNames = new ArrayList<String>();
                if (instanceName != null) {
                    dottedNames.add(instanceName.trim() + ".*");

                    cmd.setOperands(dottedNames);

                    cmd.setOption(MonitoringInfoHelper.SECURE, "true");
                    cmd.setOption(MONITOR_OPTION, "true");

                    cmd.runCommand(cmdOutput);

                    if (checkInterrupted()) {
                        logger.log(Level.WARNING, "diagnostic-service." +
                                "monitoring_info_collector_timeout",
                                new Object[]{Thread.currentThread().getName(),
                                        this.getClass().getName()});

                        if(out!=null){
                            out.print("Monitoring Info Collector Timeout");
                            out.close();
                        }
                        return data;
                    }

                    ArrayList<String> list = getIndividualProperties(cmdOutput);

                    MBeanServer mbs = AppServerMBeanServerFactory.
                            getMBeanServerInstance();

                    final String[] types = new String[]{String.class.getName()};

                    for (String value : list) {

                        if (checkInterrupted()) {
                            logger.log(Level.WARNING, "diagnostic-service." +
                                    "monitoring_info_collector_timeout",
                                    new Object[]{Thread.currentThread().
                                            getName(), this.getClass().
                                            getName()});

                            if(out!=null){
                                out.print("Monitoring Info Collector Timeout");
                                out.close();
                            }
                            return data;
                        }

                        Object[] params = new Object[]{value};
                        ObjectName dottedNameRegistry = (ObjectName) mbs.invoke(
                                new ObjectName("com.sun.appserv:name=" +
                                        "dotted-name-monitoring-registry," +
                                        "type=dotted-name-support"),
                                DOTTED_NAME_REGISTRY_OPERATION_NAME, params,
                                types);

                        Set set = getAllAttributeNames(mbs, dottedNameRegistry);

                        Iterator attributesIterator = set.iterator();

                        ArrayList<String> properties = new ArrayList<String>();
                        while (attributesIterator.hasNext()) {
                            String attr = (String) attributesIterator.next();
                            if (!isRestircted(attr)) {
                                properties.add(value + "." + attr);
                            }
                        }

                        if (properties.size() > 0) {
                            cmd.setName(GET_COMMAND);
                            cmd.setOperands(properties);

                            ArrayList<String> result = new ArrayList<String>();
                            cmd.runCommand(result);

                            for (String attributeValue : result) {
                                if (!(attributeValue.toLowerCase().
                                        indexOf(KEY_NOT_FOUND) >= 0)) {

                                    out.println(attributeValue);

                                }
                            }
                        }
                        if (checkInterrupted()) {
                            logger.log(Level.WARNING, "diagnostic-service." +
                                    "monitoring_info_collector_timeout",
                                    new Object[]{Thread.currentThread().
                                            getName(),
                                            this.getClass().getName()});
                            if(out!=null){
                                out.print("Monitoring Info Collector Timeout");
                                out.close();
                            }
                            return data;
                        }
                    }
                    out.close();
                }
                return data;
            }
            catch (FileNotFoundException fnfe) {
                logger.log(Level.WARNING, "File Not Found exception occurred " +
                        "while collecting Monitoring information", fnfe);
            }
            catch (IOException ioe) {
                logger.log(Level.WARNING, "IO Exception occurred while " +
                        "collecting Monitoring information", ioe);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, "Exception occurred while collecting"+
                        " Monitoring information", e);
            }
        }
        return data;
    }


    /*
		Return a Set of String of the names of all attributes within the MBean
	 */
    public static Set<String>
            getAllAttributeNames(final MBeanServer server,
                                 final ObjectName objectName)
            throws ReflectionException, InstanceNotFoundException,
            IntrospectionException {
        final Set<String> allNames = new HashSet<String>();

        // add the Attribute names
        final MBeanInfo info = server.getMBeanInfo(objectName);
        final MBeanAttributeInfo[] attrsInfo = info.getAttributes();
        if (attrsInfo != null) {
            for (MBeanAttributeInfo aAttrsInfo : attrsInfo) {
                allNames.add(aAttrsInfo.getName());
            }
        }
        return (allNames);
    }

    /**
     * To remove the non-properties.<br>
     * eg:  Ignores, Server.transaction-service and accepts server.
     * transaction-service.commitedcount
     *
     * @param list representing the monitorable properties
     * @return List representing the unique properties
     * @throws IOException
     */
    public ArrayList<String> getIndividualProperties(ArrayList<String> list)
            throws IOException {

        ArrayList<String> modifiedList = new ArrayList<String>();

        String current = null;

        Collections.reverse(list);
        for (String next : list) {
            if (current != null) {
                if (current.indexOf(next) != 0) {
                    modifiedList.add(next);
                    current = next;
                }
            } else {
                modifiedList.add(next);
                current = next;
            }
        }
        return modifiedList;
    }
    /**
     * To cleanup the resources before exiting
     */
    public void cleanUp() {
        out.close();
    }
}
