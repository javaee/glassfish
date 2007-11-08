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
 * DeadQueueMessageAlertAction.java
 *
 *
 */

package com.sun.enterprise.ee.selfmanagement.actions;

import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServer;
import javax.management.Notification;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.NotificationListener;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import java.io.IOException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.messaging.jms.management.server.*;
import com.sun.enterprise.connectors.system.MQJMXConnectorInfo;
import com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;



/**
 *
 * @author Sun Micro Systems, Inc
 */
public class DeadQueueMessageAlertAction implements MBeanRegistration, DeadQueueMessageAlertActionMBean, NotificationListener {
    
    private ObjectName myObjectName = null;
    private int interval = 180;
    private int offset  = 0;
    private int threshold = 1;
    private boolean continueWorkerThread = false;
    private String alertRef = null;
    private String mailRecipients = null;
    private String mailResource = null;
    static StringManager sm = StringManager.getManager(DeadQueueMessageAlertAction.class);
    static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    
    /** Creates a new instance of DeadQueueMessageAlertAction */
    public DeadQueueMessageAlertAction() {
    }
    
    public void postRegister(Boolean registrationDone) {
        if (registrationDone) {
            if(_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE, " DeadQueueMessageAlertAction : postRegister registration Done " );
        }
    }
    
    public javax.management.ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        if(_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, " DeadQueueMessageAlertAction : preRegister ObjectName " + name);
        myObjectName = name;
        return name;
    }
    
    public void preDeregister() throws Exception {
        continueWorkerThread = false;
    }
    
    public void postDeregister() {
        if(_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, " DeadQueueMessageAlertAction : postDeRegister " );
    }
    
    public void handleNotification(javax.management.Notification notification, Object handback) {
	if (notification.getType().equals("lifecycle.shutdown") ||
	    notification.getType().equals("lifecycle.termination")) {
	    continueWorkerThread = false;
	    return;
	}
        if(_logger.isLoggable(Level.INFO))
            _logger.log(Level.INFO, sm.getString("actions.activated", "Dead Queue Message Alert", notification));
        continueWorkerThread = true;
	try {
	    Thread.sleep(interval * 1000);
	} catch (InterruptedException exc) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),exc);
	}
        new DeadQueueWorkerThread().start();
    }
    
    
    boolean continueProcess() {
        return continueWorkerThread;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    
    public void setInterval(int seconds) {
        interval = seconds;
    }
    
    public void setMailAlertRef(String ref) {
        alertRef = ref;
    }

    public void setMailRecipients(String recipients) {
        mailRecipients = recipients;
    }

    public void setMailResource(String mailRes) {
        mailResource = mailRes;	    
    }
    
class DeadQueueWorkerThread extends Thread {
        private ObjectName mqDestinationMBeanName = null;
        private MBeanServerConnection  mqMsbc = null;
        private MQJMXConnectorInfo mqMi[] = null;

        DeadQueueWorkerThread() {
	    if (_logger.isLoggable(Level.FINE)) {
	        _logger.fine(" DeadQueueMessageAlertAction : Interval " + interval + 
		             " , Threshold " + threshold +
		             " , Offset " + offset +
		             " , Mail Resource " + mailResource +
		             " , Mail Recipients " + mailRecipients +
		             " , Mail Alert Ref " + alertRef);

            }
        }
    
        public void run() {
            String currentServer = ApplicationServer.getServerContext().getInstanceName();
            try {
                mqMi= ConnectorRuntime.getRuntime().getMQJMXConnectorInfo(currentServer) ;
                if (mqMi[0] == null) {
                    _logger.log(Level.WARNING, sm.getString("action.internal_error"));
                    return;
                }
            mqMsbc = mqMi[0].getMQMBeanServerConnection();
            mqDestinationMBeanName =
                    MQObjectName.createDestinationMonitor(DestinationType.QUEUE, "mq.sys.dmq");
            boolean loop = true;
            while(loop) {
                Thread.sleep(interval);
                Long numMsgs = (Long)mqMsbc.getAttribute(mqDestinationMBeanName, DestinationAttributes.NUM_MSGS);
                if (numMsgs > threshold) {
                    if(_logger.isLoggable(Level.INFO))
                        _logger.log(Level.INFO, sm.getString("nummsgs.exceeds.threshold", numMsgs, threshold));
                    if (alertRef != null) {
                        sendAlert(numMsgs, threshold);
                    } else if (mailRecipients != null) {
                        sendMailAlert(numMsgs, threshold);
		    }
                    if (offset == 0)
                        return;
                    threshold += offset;
                }
                if(!continueProcess())
                    loop = false;
            }
        }catch (InterruptedException exc) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),exc);
        } catch(ConnectorRuntimeException cex) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),cex);
        } catch(MalformedObjectNameException mex) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),mex);
        } catch(MBeanException ex) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),ex);
        } catch(AttributeNotFoundException aex) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),aex);
        } catch(InstanceNotFoundException iex) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),iex);
        } catch(ReflectionException rex) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),rex);
        } catch(IOException ioex) {
            _logger.log(Level.WARNING, sm.getString("action.internal_error"),ioex);
        } finally {
            try {
                mqMi[0].closeMQMBeanServerConnection();
            } catch (Exception ex) {
                _logger.log(Level.WARNING, sm.getString("action.internal_error"),ex);
            }
        }
    }
    
    private void sendAlert(long numMsgs, int threshold) {
        
        String alertMsg =  sm.getString("nummsgs.exceeds.threshold", numMsgs, threshold);
        if (alertRef != null) {
            try {
                Mbean definedMBean = ServerBeansFactory.getMBeanDefinition(
                        ApplicationServer.getServerContext().getConfigContext(), alertRef);
                ObjectName objName =
                        CustomMBeanRegistrationImpl.getCascadingAwareObjectName(definedMBean);
                Object[] params = new Object[2];
                String[] signature = new String[2];
                params[0] = new Notification("deadqueuemessagealert.exceedsthreshold", this,0,alertMsg);
                params[1] = null;
                signature[0] = (Notification.class).getName();
                signature[1] = (java.lang.Object.class).getName();
                MBeanServerFactory.getMBeanServer().invoke(objName,"notification", params, signature);
            } catch (InstanceNotFoundException ex) {
                _logger.log(Level.WARNING, sm.getString("action.internal_error"),ex);
            } catch (Exception ex) {
                _logger.log(Level.WARNING, sm.getString("action.internal_error"),ex);
            }
            
        }
    }
    private void sendMailAlert(long numMsgs, int threshold) {
        
        String alertMsg =  sm.getString("nummsgs.exceeds.threshold", numMsgs, threshold);
	MailAlert mAlert = new MailAlert();
	mAlert.setRecipients(mailRecipients);
	mAlert.setMailResource(mailResource);
        Notification n1 = new Notification("deadqueuemessagealert.exceedsthreshold", this ,0,alertMsg);
	mAlert.notification(n1, null);
    }
}

}
