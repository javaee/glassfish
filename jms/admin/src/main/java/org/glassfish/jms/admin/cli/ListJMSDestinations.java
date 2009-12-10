/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.jms.admin.cli;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.internal.api.ServerContext;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.*;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.logging.LogDomains;

/**
 * Create JMS Destination
 *
 */
@Service(name="list-jmsdest")
@Scoped(PerLookup.class)
@I18n("list.jms.dests")

public class ListJMSDestinations extends JMSDestination implements AdminCommand {

        static Logger logger = LogDomains.getLogger(ListJMSDestinations.class,LogDomains.ADMIN_LOGGER);
        final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJMSDestination.class);

        @Param(name="desttype", optional=true)
        String destType;

        @Param(name="property", optional=true, separator=':')
        Properties props;

        @Param(primary=true, optional=true)
        String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

        @Inject
        com.sun.appserv.connectors.internal.api.ConnectorRuntime connectorRuntime;

        @Inject
        Domain domain;

        @Inject
        Configs configs;

        @Inject
        ServerContext serverContext;


     public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

          if(destType != null && !destType.equals(JMS_DEST_TYPE_QUEUE) &&
                 !destType.equals(JMS_DEST_TYPE_TOPIC))
          {
            report.setMessage(localStrings.getLocalString("admin.mbeans.rmb.invalid_jms_desttype",destType));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            List<JMSDestinationInfo> list= listJMSDestinations(target, destType);

            if (list.isEmpty()) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(localStrings.getLocalString("nothingToList",
                    "Nothing to list."));
            } else {
                for (JMSDestinationInfo destInfo : list) {
                    final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(destInfo.getDestinationName());
                }
            }

        } catch (Exception e) {
            logger.throwing(getClass().getName(), "ListJMSDestination", e);
            e.printStackTrace();//handleException(e);
            report.setMessage(localStrings.getLocalString("list.jms.dest.fail",
                    "Unable to list JMS Destinations") + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
     }

// list-jmsdest
    public List listJMSDestinations(String tgtName, String destType)
        throws Exception {

        logger.log(Level.FINE, "listJMSDestination ...");
                MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(target, configs, serverContext, domain, connectorRuntime);

        //MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);
        try {
                        MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
            ObjectName on = new ObjectName(
                DESTINATION_MANAGER_CONFIG_MBEAN_NAME);
            String [] signature = null;
            Object [] params = null;

            ObjectName [] dests = (ObjectName [])mbsc.invoke(on, "getDestinations", params, signature);
            if ((dests != null) && (dests.length > 0)) {
                List<JMSDestinationInfo> jmsdi = new ArrayList<JMSDestinationInfo>();
                for (int i=0; i<dests.length; i++) {
                    on = dests[i];

                    String jdiType = toStringLabel(on.getKeyProperty("desttype"));
                    String jdiName = on.getKeyProperty("name");

                    // check if the destination name has double quotes at the beginning
                    // and end, if yes strip them
                    if ((jdiName != null) && (jdiName.length() > 1)) {
                        if (jdiName.indexOf('"') == 0) {
                            jdiName = jdiName.substring(1);
                        }
                        if (jdiName.lastIndexOf('"') == (jdiName.length() - 1)) {
                            jdiName = jdiName.substring(0, jdiName.lastIndexOf('"'));
                        }
                    }

                    JMSDestinationInfo jdi = new JMSDestinationInfo(jdiName, jdiType);

                    if(destType == null) {
                        jmsdi.add(jdi);
                    } else if (destType.equals(JMS_DEST_TYPE_TOPIC)
                            || destType.equals(JMS_DEST_TYPE_QUEUE)) {
                        //Physical Destination Type specific listing
                        if (jdiType.equalsIgnoreCase(destType)) {
                            jmsdi.add(jdi);
                        }
                    }
                }
                return jmsdi;
                //(JMSDestinationInfo[]) jmsdi.toArray(new JMSDestinationInfo[]{});
            }
        } catch (Exception e) {
                    //log JMX Exception trace as WARNING
                    logAndHandleException(e, "admin.mbeans.rmb.error_listing_jms_dest");
                } finally {
                    try {
                        if(mqInfo != null) {
                            mqInfo.closeMQMBeanServerConnection();
                        }
                    } catch (Exception e) {
                      handleException(e);
                    }
                }

        return null;
    }
    private String toStringLabel(String type)  {

	    if (type.equals(DESTINATION_TYPE_QUEUE))  {
	        return("queue");
	    } else if (type.equals(DESTINATION_TYPE_TOPIC))  {
	        return("topic");
	    } else  {
	        return("unknown");
	    }
    }

    }
