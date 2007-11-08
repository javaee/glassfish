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

/*
 * $Id: JMSDestination.java,v 1.17 2007/03/27 10:11:07 rampsarathy Exp $
 * author sreenivas.munnangi@sun.com
 */

package com.sun.enterprise.admin.mbeans;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.sun.enterprise.admin.common.JMSDestinationInfo;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.constant.JMSAdminConstants;
import com.sun.enterprise.admin.common.exception.JMSAdminException;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.system.MQJMXConnectorInfo;
import com.sun.enterprise.connectors.system.ActiveJmsResourceAdapter;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.messaging.jms.management.server.DestinationType;
import com.sun.messaging.jms.management.server.MQObjectName;

/**
 * A helper class used for performing MQ related administration 
 * operations using MQ's JMX API.
 *  
 * @author Sreenivas Munnangi
 * @author Sivakumar Thyagarajan
 * @since SJSAS9.0
 */
public class JMSDestination {

	// flag to enable the use of JMX for JMS destination commands
	// if false uses the old behavior
	// The value for DONT_USE_MQ_JMX can be set thru sysproperty
	private static final boolean USE_JMX = 
		!(Boolean.getBoolean("DONT_USE_MQ_JMX"));

	private static final Logger sLogger = 
		Logger.getLogger(AdminConstants.kLoggerName);
	private static final StringManager localStrings = 
		StringManager.getManager( JMSDestination.class);


	// default constructor
	public JMSDestination () {
	}

	// create-jmsdest
	public void createJMSDestination(String destName, String destType, 
		Properties destProps, String tgtName) throws JMSAdminException {

		sLogger.log(Level.FINE, "createJMSDestination ...");
                MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(tgtName);

		//MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);
		try {
            MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
			ObjectName on = new ObjectName(
				MQObjectName.DESTINATION_MANAGER_CONFIG_MBEAN_NAME);
			String [] signature = null;
			AttributeList destAttrs = null;
			Object [] params = null;

			if (destProps != null) {
				destAttrs = convertProp2Attrs(destProps);
			}
                        
            // setAppserverDefaults(destAttrs, mqInfo);

			if (destType.equalsIgnoreCase(JMSAdminConstants.JMS_DEST_TYPE_TOPIC)) {
				destType = DestinationType.TOPIC;
			} else if (destType.equalsIgnoreCase(JMSAdminConstants.JMS_DEST_TYPE_QUEUE)) {
				destType = DestinationType.QUEUE;
			} 
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
                    logAndHandleException(e, "admin.mbeans.rmb.error_creating_jms_dest");
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

	// delete-jmsdest
	public void deleteJMSDestination(String destName, String destType, 
		String tgtName) 
		throws JMSAdminException {

		sLogger.log(Level.FINE, "deleteJMSDestination ...");
                MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(tgtName);

		//MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);

		try {
			MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
			ObjectName on = new ObjectName(
				MQObjectName.DESTINATION_MANAGER_CONFIG_MBEAN_NAME);
			String [] signature = null;
			Object [] params = null;

			signature = new String [] {
				"java.lang.String",
				"java.lang.String"};

			if (destType.equalsIgnoreCase("topic")) {
				destType = DestinationType.TOPIC;
			} else if (destType.equalsIgnoreCase("queue")) {
				destType = DestinationType.QUEUE;
			} 
			params = new Object [] {destType, destName};
			mbsc.invoke(on, "destroy", params, signature);
		} catch (Exception e) {
                   //log JMX Exception trace as WARNING
                   logAndHandleException(e, "admin.mbeans.rmb.error_deleting_jms_dest");
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

	// list-jmsdest
	public JMSDestinationInfo [] listJMSDestinations(String tgtName, String destType) 
		throws JMSAdminException {

		sLogger.log(Level.FINE, "listJMSDestination ...");
                MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(tgtName);

		//MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);
		try {
                        MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
			ObjectName on = new ObjectName(
				MQObjectName.DESTINATION_MANAGER_CONFIG_MBEAN_NAME);
			String [] signature = null;
			Object [] params = null;

			ObjectName [] dests = (ObjectName [])mbsc.invoke(on, "getDestinations", params, signature);
			if ((dests != null) && (dests.length > 0)) {
                List<JMSDestinationInfo> jmsdi = new ArrayList<JMSDestinationInfo>();
				for (int i=0; i<dests.length; i++) {
					on = dests[i];

                    String jdiType = DestinationType.toStringLabel(on.getKeyProperty("desttype"));
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
                    } else if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_TOPIC) 
                            || destType.equals(JMSAdminConstants.JMS_DEST_TYPE_QUEUE)) {
                        //Physical Destination Type specific listing
                        if (jdiType.equalsIgnoreCase(destType)) {
                            jmsdi.add(jdi);
                        }
                    }
				}
				return (JMSDestinationInfo[]) jmsdi.toArray(new JMSDestinationInfo[]{});
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


	// JMSPing
	// String status of jms ping RUNNING or exception
	public String JMSPing(String tgtName) 
		throws JMSAdminException {

		sLogger.log(Level.FINE, "JMSPing ...");
                MQJMXConnectorInfo mqInfo = null;
		try {
			MQJMXConnectorInfo [] cInfo =
				ConnectorRuntime.getRuntime().getMQJMXConnectorInfo(tgtName);
			if ((cInfo == null) || (cInfo.length < 1)) {
				throw new JMSAdminException(
                        localStrings.getString("admin.mbeans.rmb.error_obtaining_jms"));
			}
			int k = -1;
			for (int i=0; i<cInfo.length; i++) {
				if (tgtName.equals(cInfo[i].getASInstanceName())) {
					k = i;
					break;
				}
			}
			if (k == -1) {
				throw new JMSAdminException(
				localStrings.getString("admin.mbeans.rmb.invalid_server_instance", tgtName));
			}
                        mqInfo = cInfo[k];

			MBeanServerConnection mbsc = cInfo[k].getMQMBeanServerConnection();
                        //perform some work on the connection to check for connection health.
                        mbsc.getMBeanCount(); 

		} catch (Exception e) {
                    //log JMX Exception trace as WARNING
                    logAndHandleException(e, "admin.mbeans.rmb.error_pinging_jms");
                } finally {
                    try {
                        if(mqInfo != null) {
                            mqInfo.closeMQMBeanServerConnection();
                        }
                    } catch (Exception e) {
                      handleException(e);
                    }
                }
		return JMSAdminConstants.JMS_HOST_RUNNING;
	}


	// purge-jmsdest
	public void purgeJMSDestination(String destName, String destType, String tgtName) 
		throws JMSAdminException {

		sLogger.log(Level.FINE, "purgeJMSDestination ...");
                MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(tgtName);

		//MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);
		try {

                        MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
			if (destType.equalsIgnoreCase("topic")) {
				destType = DestinationType.TOPIC;
			} else if (destType.equalsIgnoreCase("queue")) {
				destType = DestinationType.QUEUE;
			} 
			ObjectName on = 
				MQObjectName.createDestinationConfig(destType, destName);
			String [] signature = null;
			Object [] params = null;

			mbsc.invoke(on, "purge", params, signature);
		} catch (Exception e) {
                    //log JMX Exception trace as WARNING
                    logAndHandleException(e, "admin.mbeans.rmb.error_purging_jms_dest");
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


        private MQJMXConnectorInfo getMQJMXConnectorInfo(String tgtName) 
                                                    throws JMSAdminException {
                sLogger.log(Level.FINE, "getMQJMXConnectorInfo for " + tgtName);
                MQJMXConnectorInfo mcInfo = null;
                                                                                                                                              
                try {
                        MQJMXConnectorInfo [] cInfo =
                                ConnectorRuntime.getRuntime().getMQJMXConnectorInfo(tgtName);
                        if ((cInfo == null) || (cInfo.length < 1)) {
                                throw new JMSAdminException(
                        localStrings.getString("admin.mbeans.rmb.error_obtaining_jms"));
                        }
                        mcInfo = cInfo[0];
                                                                                                                                              
                } catch (Exception e) {
                    handleException(e);
                }
                return mcInfo;
        }

/*
	private MBeanServerConnection  getMBeanServerConnection(String tgtName)
		throws JMSAdminException {

		sLogger.log(Level.FINE, "getMBeanServerConnection for " + tgtName);
		MBeanServerConnection mbsc = null;

		try {
			MQJMXConnectorInfo [] cInfo =
				ConnectorRuntime.getRuntime().getMQJMXConnectorInfo(tgtName);
			if ((cInfo == null) || (cInfo.length < 1)) {
				throw new JMSAdminException(
                        localStrings.getString("admin.mbeans.rmb.error_obtaining_jms"));
			}
			mbsc = cInfo[0].getMQMBeanServerConnection();
			sLogger.log(Level.FINE, "MBeanServerConnection = " + mbsc);

		} catch (Exception e) {
                    handleException(e);
                }

		return mbsc;
	}
*/

        private void setAppserverDefaults(AttributeList destAttrs, 
                                             MQJMXConnectorInfo info) {

	     if (destAttrs == null) {
		destAttrs = new AttributeList();
	     }

             if (info.getBrokerType().equalsIgnoreCase(ActiveJmsResourceAdapter.LOCAL)) {
                 String localDelivery = "LocalDeliveryPreferred";
                 boolean notPresent = true;
                 for (Object obj : destAttrs) {
                     Attribute attrib = (Attribute) obj;
                     if (attrib.getName().equals(localDelivery)) {
                         notPresent  = false;
                     }
                 }
                 if (notPresent) {
                    Attribute attrib = new Attribute (localDelivery,
                                       Boolean.valueOf("true"));
                    destAttrs.add(attrib);
                 }
             }
        }

	//XXX: To refactor into a Generic attribute type mapper, so that it is extensible later.
        private AttributeList convertProp2Attrs(Properties destProps) {

		AttributeList destAttrs = new AttributeList();

		String propName = null;
		String propValue = null;

		for (Enumeration e = destProps.propertyNames(); e.hasMoreElements();) {
                     propName = (String) e.nextElement();
                     if (propName.equals("AutoCreateQueueMaxNumActiveConsumers")) {
                         destAttrs.add(new Attribute("AutoCreateQueueMaxNumActiveConsumers",
                                                     Integer.valueOf(destProps.getProperty("AutoCreateQueueMaxNumActiveConsumers"))));
                     } else if (propName.equals("maxNumActiveConsumers")) {
                         destAttrs.add(new Attribute("MaxNumActiveConsumers",
                                                     Integer.valueOf(destProps.getProperty("maxNumActiveConsumers"))));
                     } else if (propName.equals("MaxNumActiveConsumers")) {
                         destAttrs.add(new Attribute("MaxNumActiveConsumers",
                                                     Integer.valueOf(destProps.getProperty("MaxNumActiveConsumers"))));
                     } else if (propName.equals("AutoCreateQueueMaxNumBackupConsumers")) {
                         destAttrs.add(new Attribute("AutoCreateQueueMaxNumBackupConsumers",
                                                     Integer.valueOf(destProps.getProperty("AutoCreateQueueMaxNumBackupConsumers"))));
                     } else if (propName.equals("AutoCreateQueues")) {
                         boolean b = false;

                         propValue = destProps.getProperty("AutoCreateQueues");
                         if (propValue.equalsIgnoreCase("true")) {
                             b = true;
                         }
                         destAttrs.add(new Attribute("AutoCreateQueues",
                                                     Boolean.valueOf(b)));
                     } else if (propName.equals("AutoCreateTopics")) {
                         boolean b = false;

                         propValue = destProps.getProperty("AutoCreateTopics");
                         if (propValue.equalsIgnoreCase("true")) {
                             b = true;
                         }
                         destAttrs.add(new Attribute("AutoCreateTopics",
                                                     Boolean.valueOf(b)));
                     } else if (propName.equals("DMQTruncateBody")) {
                         boolean b = false;

                         propValue = destProps.getProperty("DMQTruncateBody");
                         if (propValue.equalsIgnoreCase("true")) {
                             b = true;
                         }
                         destAttrs.add(new Attribute("DMQTruncateBody",
                                                     Boolean.valueOf(b)));
                     } else if (propName.equals("LogDeadMsgs")) {
                         boolean b = false;

                         propValue = destProps.getProperty("LogDeadMsgs");
                         if (propValue.equalsIgnoreCase("true")) {
                             b = true;
                         }
                         destAttrs.add(new Attribute("LogDeadMsgs",
                                                     Boolean.valueOf(b)));
                     } else if (propName.equals("MaxBytesPerMsg")) {
                         destAttrs.add(new Attribute("MaxBytesPerMsg",
                                                     Long.valueOf(destProps.getProperty("MaxBytesPerMsg"))));
                     } else if (propName.equals("MaxNumMsgs")) {
                         destAttrs.add(new Attribute("MaxNumMsgs",
                                                     Long.valueOf(destProps.getProperty("MaxNumMsgs"))));
                     } else if (propName.equals("MaxTotalMsgBytes")) {
                         destAttrs.add(new Attribute("MaxTotalMsgBytes",
                                                     Long.valueOf(destProps.getProperty("MaxTotalMsgBytes"))));
                     } else if (propName.equals("NumDestinations")) {
                         destAttrs.add(new Attribute("NumDestinations",
                                                     Integer.valueOf(destProps.getProperty("NumDestinations"))));
                     }
                 }
		return destAttrs;
	}
    
    /**
     * Logs an exception via the logger and throws a JMSAdminException.
     * 
     * This method exists as exceptions that occur while invoke 
     * MQ JMX operations currently have "nested" exception messages
     * and it is very difficult for a user to understand the real
     * cause from the exception message. We now throw a generic message 
     * and log the actual exception in the server log so that users could
     * refer to the server log for more information. 
     */
    private void logAndHandleException(Exception e, String errorMsg)
                                        throws JMSAdminException {
        //log JMX Exception trace as WARNING
        StringWriter s = new StringWriter();
        e.getCause().printStackTrace(new PrintWriter(s));
        sLogger.log(Level.WARNING, s.toString());
        JMSAdminException je = new JMSAdminException(localStrings.getString(errorMsg));
	/* Cause will be InvocationTargetException, cause of that
 	 * wil be  MBeanException and cause of that will be the
	 * real exception we need
	 */
	if ((e.getCause() != null) && 
	    (e.getCause().getCause() != null)) {
  		je.initCause(e.getCause().getCause().getCause());
	}
        handleException(je);
    }
        
	
    private void handleException(Exception e)
                                throws JMSAdminException {

        if (e instanceof JMSAdminException)  {
            throw ((JMSAdminException)e);
        }

        String msg = e.getMessage();

        JMSAdminException jae;
        if (msg == null)  {
            jae = new JMSAdminException();
        } else  {
            jae = new JMSAdminException(msg);
        }

        /*
         * Don't do this for now because the CLI does not include jms.jar
         * (at least not yet) in the classpath. Sending over a JMSException
         * will cause a class not found exception to be thrown.
         */
        //jae.setLinkedException(e);

        throw jae;
    }
    
    /**
     * Determines whether MQ JMX connector needs to be used for MQ related 
     * administrative operations.
     *  
     * @param target Target on which a commands needs to be executed
     */
    public static boolean useJMX(Target target) {
        /*
        if(JMSDestination.USE_JMX) {
            if ((target.getType() == TargetType.DAS) || (target.getType() == TargetType.SERVER)) {
                return true; 
            }
        } 
       return false; 
       */
	   return USE_JMX;
    }

}
