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

package com.sun.enterprise.connectors.system;

import java.util.Properties;
import java.util.logging.*;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.jms.IASJmsUtil;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ConnectorRuntime;

import com.sun.messaging.jms.management.server.DestinationType;
import com.sun.messaging.jms.management.server.DestinationOperations;
import com.sun.messaging.jms.management.server.DestinationAttributes;
import com.sun.messaging.jms.management.server.MQObjectName;
/** 
 * Represents MQAdministrator of the default-jms-host of the 
 * target.
 *
 * @author Binod P.G
 */
public class MQAdministrator {

    static Logger logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    private static StringManager localStrings =
        StringManager.getManager( ConnectorRuntime.class);

    private String target = null;

    /**
     * Creates JMSAdmin object for the curremt Instance.
     */
    public MQAdministrator() {
    }

    /**
     * Creates JMSAdmin object from the name of the target passed in.
     *
     * @param target Name of the target. Eg: cluster1 or server1
     */
    public MQAdministrator(String target) 
                      throws ConnectorRuntimeException{
        this.target = target;
    }
  
    public boolean destinationExists(String destName, boolean isQueue) {
	MQJMXConnectorInfo mqInfo = null;
	try {
	mqInfo = MQJMXConnectorHelper.getMQJMXConnectorInfo(target) [0];
        MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
 	ObjectName objName
		= new ObjectName(
		  MQObjectName.DESTINATION_MANAGER_MONITOR_MBEAN_NAME);
        ObjectName destinationObjNames[] = 
                (ObjectName[])mbsc.invoke(objName, 
		  DestinationOperations.GET_DESTINATIONS, null, null);
	String destType = DestinationType.TOPIC;
	if (isQueue) {
		destType = DestinationType.QUEUE;
	}
	for (int i = 0; i < destinationObjNames.length; ++i)  {
		ObjectName oneDestObjName = destinationObjNames[i];
		String oneDestName = (String)mbsc.getAttribute(oneDestObjName,
                        DestinationAttributes.NAME);	
		String oneDestType = (String)mbsc.getAttribute(oneDestObjName,
                        DestinationAttributes.TYPE);
		if (oneDestName.equals(destName.trim()) 
			&& oneDestType.equals(destType)) {
				return true;
		}
        }
	}catch (Exception e) {
        	logger.log(Level.WARNING, "Exception occurred when trying to check "
		+ "if destination exists");
	}
	
	return false;
	
    }

    public void createPhysicalDestination(String destName,boolean isQueue )
                       throws ConnectorRuntimeException {

        MQJMXConnectorInfo mqInfo = null;
        try {
            if (this.target == null) {
                this.target = getClusterName();
            }
	    if (destinationExists(destName, isQueue)) {
		logger.log(Level.INFO, "Destination " + destName + "exists in broker");
		return;
	    }		
            mqInfo = MQJMXConnectorHelper.getMQJMXConnectorInfo(target) [0];
            MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
            ObjectName on = new ObjectName(
                 MQObjectName.DESTINATION_MANAGER_CONFIG_MBEAN_NAME);
            String [] signature = null;
            AttributeList destAttrs = null;
            Object [] params = null;
            String destType = DestinationType.TOPIC;

            destAttrs = new AttributeList();
            if (isQueue) {
                destAttrs.add(new Attribute(IASJmsUtil.getMaxActiveConsumersAttribute(),
                          new Integer(IASJmsUtil.getDefaultMaxActiveConsumers())));
                destType = DestinationType.QUEUE;
            } 
	/*
            if (mqInfo.getBrokerType().equalsIgnoreCase(ActiveJmsResourceAdapter.LOCAL)) {
                destAttrs.add( new Attribute ("LocalDeliveryPreferred", 
                          new Boolean("true")));
            }
	*/
            if ((destAttrs == null) || (destAttrs.size() == 0)){
                signature = new String [] {
                     "java.lang.String",
                     "java.lang.String"};
                params = new Object [] {destType, destName};
            } else {
                signature = new String [] {
                     "java.lang.String",
                     "java.lang.String",
                     "javax.management.AttributeList"};
                params = new Object [] {destType, destName, destAttrs};
            }
            mbsc.invoke(on, "create", params, signature);
        } catch (Exception e) {
            handleException(e);
        } finally {
            try {
                if(mqInfo != null) {
                     mqInfo.closeMQMBeanServerConnection();
                }
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    private String getClusterName() throws ConfigException {
        return ClusterHelper.getClusterForInstance(
                        ApplicationServer.getServerContext().getConfigContext(),
                        ApplicationServer.getServerContext().getInstanceName()).getName();
    }


    private ConnectorRuntimeException handleException(Exception e) {
        logger.log(Level.WARNING,""+e.getMessage(), e);
        ConnectorRuntimeException cre = 
             new ConnectorRuntimeException(e.getMessage());
        cre.initCause(e);
        return cre;
    }

}
