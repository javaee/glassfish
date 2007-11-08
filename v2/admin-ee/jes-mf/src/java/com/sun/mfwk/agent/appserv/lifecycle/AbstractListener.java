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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.lifecycle;

import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping;
import com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications;
import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.util.BundleReader;
import com.sun.mfwk.agent.appserv.util.Utils;

/**
 * Class used by ListenerManagerImpl to implement its APIs. Listens to specific
 * mbean on the Server as specified by its derived class..
 */
abstract class AbstractListener implements NotificationListener {

   /**
    * Constructs the AbstractListener object to listen to notifications 
    * from the given server for the given client.
    * 
    * @param connection the mbean server connection to listen to
    * @param client     the client for which to listen to
    * @param filter     the object to filter out unwanted notifcations 
    * @param handback   the object which is sent back along to the listener
    *                   along with the notification without any modifications
    */
    AbstractListener(MBeanServerConnection connection, String asInstance, 
        String asDomain, NotificationListener client, 
            NotificationFilter filter, Object handback){
        this.connection = connection;
        this.client = client;
        this.filter = filter;
        this.handback = handback;
        this.asInstance = asInstance;
        this.asDomain = asDomain;
        runtimeToMonitoring = new HashMap();
    }


   /**
    * Specifies whether the given notification is of interest.
    *
    * This method is used when null is provided for NotificationFilter in the
    * <code>LifecycleManager</code> API <code>addNotificationListener</code>
    * This method uses the information from notification.xml to figure out 
    * whether the given notification is of interest to us. This method does
    * the following: Compares the input notification with each of the <object-name>
    * element of notification.xml till the match is found. On match, it returns
    * <code>true</code> else it returns <code>false</code>. Input notification is
    * compared with the <object-name> element by matching each of the name/value
    * pairs of <attribute> subelement with the property/value pair in the ObjectName
    * of the object sending the notification.
    *
    * @param notification the notification recieved
    *
    * @return boolean <code>true</code> if the input notification is of interest;
    *                 else <code>false</code>
    */
    private boolean isNotificationEnabled(Notification notification){
        ObjectName objectName = getObjectName(notification);
        assert(objectName != null);
        if(objectName != null){
            try{
                //Get the in-memory representation of the notification.xml.
		Notifications notifications = getNotifications();
                if(notifications != null) {
                    //Get the domain of interest from the notification.xml
                    //Consider notifications only from this domain.
	            String domain = notifications.getDomain();
		    if(domain.equals(objectName.getDomain())){
	        	com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName name = null;

                        //Return if notification is because of cascaded object.
                        //Plugin recieves two notifications for each monitoring object on the remote
                        //instance. One, when the monitoring object gets created on the remote server
                        //and the other when it gets cascaded to the DAS. This causes a problem when
                        //we have same Application or Module deployed to DAS and remote server.
                        //Cascaded object is overwriting the object instrumented by the remote server.
                        //To avoid this, plugin should not instrument CMM objects for cascaded AS mbeans.
                        if(!asInstance.equals(objectName.getKeyProperty(Constants.SERVER_NAME_KEY))) {
                            return false;
                        } 

                        //for each of the <object-name> elements from notification.xml
			for(int i=0; i<notifications.sizeObjectName(); i++){
                            name = notifications.getObjectName(i);
			    com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute attribute = null;
			    String attributeName = null;
			    String attributeValue = null;
			    String value = null;
			    boolean ofInterest = true;
                            //for each of the <attribute> element of <object-name>
                            //  compare the <name> and <value> elements with the key/value
                            //  pairs in input notification object name.
			    for(int j=0; j<name.sizeAttribute(); j++){
			        attribute = name.getAttribute(j);
				attributeName = attribute.getName();
				attributeValue = attribute.getValue();
				value = objectName.getKeyProperty(attributeName);

                                if(attributeValue != null){
                                    if(!attributeValue.equals(value)){
                                        ofInterest = false;
                                        break;
                                    }
                                } else {
                                    //checking for equality in case of null values.
                                    //attributeValue and value are equal if both are null
                                    if(value != null){
                                        ofInterest = false;
                                        break;
                                    }
                                }
		            }
			    if(ofInterest == true){
                                return true;
			    }
		       }
	            }
                }
            } catch(Exception ex){
                Utils.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return false;
    }


   /**
    * Recieves notifications from the server.
    * 
    * This method does the following if the input notification is of interest.
    * If notification type is <code>JMX.mbean.unregistered</code>, forwards 
    * to the client.
    * </p>
    * If notification type is <code>JMX.mbean.registered</code>, creates new 
    * thread to handles the notification. 
    *
    * @param notification     the notification recieved
    * @param handback   the handback object used when we registered this listener
    */
    public void handleNotification(Notification notification, Object handback){
        if(isNotificationEnabled(notification)){
            ObjectName objectName = getObjectName(notification);
            if(objectName != null){
                if(client != null){
                    notification.setUserData(objectName);
                    client.handleNotification(notification, handback);
                }
            }
        }
    }


    /**
     * Determines whether this object represents the given inputs.
     *
     * @return boolean <code>true</code> if this object represents given inputs;
     *                 else <code>false</code>
     */
    boolean isMatch(MBeanServerConnection connection, 
                NotificationListener client) {
        if((connection.equals(this.connection)) && 
            (client.equals(this.client))) { 
            return true;
        }
        return false;
    }


    /**
     * Determines whether this object represents the given inputs.
     *
     * @return boolean <code>true</code> if this object represents given inputs;
     *                 else <code>false</code>
     */
    boolean isMatch(MBeanServerConnection connection, 
                NotificationListener client, NotificationFilter filter,
                    Object handback) {
        if(isMatch(connection, client)) {
            if((filter.equals(this.filter)) && 
                (handback.equals(this.handback))) { 
                return true;
            }
        }
        return false;
    }


    /**
     * Registers listener on the registered object specified by the derived class.
     * Registers listener for a given client with given filter & handback object.
     * If provided filter is null, then this objects registers itself as a 
     * listener.
     * 
     * @thows  InstanceNotFoundException 
     * @throws IOException 
     */
    void registerListener() throws InstanceNotFoundException, IOException {
        ObjectName objectName = getObjectName(connection);
        if(objectName != null){
                assert (connection != null): "Connection not available";
                if((connection != null) && (client != null)){
                    if(filter != null){
                        connection.addNotificationListener(objectName, client, filter, handback);
                    } else {
                        connection.addNotificationListener(objectName, this, null, handback);
                    }
                } else {
                    Utils.log(Level.INFO, "Listener not registered. Invalid arguments");
                }
        } else {
            Utils.log(Level.INFO, "Not a valid registered Object Name");
        }
    }


    /**
     * Method that unregisters the listener on given mbean for
     * a specific client if this object is the one who registered it.
     *
     * @param connection the given connection object
     * @param client the given client object
     * 
     * @thows  InstanceNotFoundException 
     * @throws IOException 
     */
    void unregisterListener(MBeanServerConnection connection, 
            NotificationListener client) throws InstanceNotFoundException,
            ListenerNotFoundException, IOException{
        ObjectName objectName = getObjectName(connection);
        if(objectName!= null){
            assert (connection != null): "Connection not available";
            connection.removeNotificationListener(objectName, client);
        } else {
            Utils.log(Level.INFO, "Not a valid registered Object Name");
        }
    }


    /**
     * Method that unregisters the listener on given mbean for
     * a specific client if this object is the one who registered it.
     *
     * @param connection the given connection object
     * @param client the given client object
     * @param filter the given filter object
     * @param handback the given handback object
     *
     * @thows  InstanceNotFoundException 
     * @throws IOException 
     */
    void unregisterListener(MBeanServerConnection connection, 
            NotificationListener client, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        ObjectName objectName = getObjectName(connection);
        if(objectName!= null){
            assert (connection != null): "Connection not available";
            if(filter != null) {
                connection.removeNotificationListener(objectName, client, filter, handback);
            } else {
                connection.removeNotificationListener(objectName, this, filter, handback);
            }
        } else {
            Utils.log(Level.INFO, "Not a valid registered Object Name");
        }
    }


    protected abstract String getObjectNameString();


   /**
    * Utility method to print the information of the given notification.
    */
    protected void printInfo(MBeanServerNotification notification){
        System.out.println(" Notification " + notification.toString());
        System.out.println("Sequence Number: " + notification.getSequenceNumber());
        System.out.println("Time Stamp: " + notification.getTimeStamp());
        System.out.println("MBean Name: " + notification.getMBeanName().getCanonicalName());
        System.out.println("Message: " + notification.getMessage());
        System.out.println("Source: " + notification.getSource().toString());
        System.out.println("Type: " + notification.getType());
        System.out.println("UserData: " + notification.getUserData());
    }


   /**
    * <code>Thread</code> that handles the Notification without blocking the
    * the main thread. It waits till the object sending the notification is
    * ready or times out after the specified time. It forwards the notification
    * to the client only if both the following conditions satisfies -
    *        the object sending the notification is ready
    *        corresponding monitoring mbean is registered on the registered
    * The second condition is required because the monitoring mbean may not get
    * created in case the monitoring level is off.
    */
    private final class HandleNotificationThread extends Thread
    {
        private final   long             timeOutSeconds;
        private final   int             startAfterSeconds;
        private boolean                 timeOutReached;
        private long                    startTime;
        
        private ObjectName              objectName;
        private String              monitoringObjectName;
        private MBeanServerConnection   connection;
        
        NotificationListener            client;
        Notification                    notification;
        Object                          handback;
        Integer                         status;

        public HandleNotificationThread(final long timeOutSeconds, 
                final int startAfterSeconds, final ObjectName objectName,
                final String monitoringObjectName, 
                final MBeanServerConnection connection, 
                final NotificationListener client, 
                final Notification notification, final Object handback) {
            this.timeOutSeconds     = timeOutSeconds;
            this.startAfterSeconds  = startAfterSeconds;
            this.objectName         = objectName;
            this.monitoringObjectName = monitoringObjectName;
            this.connection         = connection;
            this.timeOutReached     = false;
            this.client             = client;
            this.notification       = notification;
            this.handback           = handback;
        }


       /**
        * Waits till the one of the follwoing happens - notification object is
        * ready or specified time interval is past or notification object gets
        * into failed state. In case of ready state of notification object, this
        * methods checks if the corresponding monitoring mbean is registered or
        * not. If monitoring mbean is registered then this methods sets the 
        * monitoring mbeanname as UserData in notification and forwards the 
        * notification to the client.
        */
        public void run() {
            startTime = System.currentTimeMillis();
            try
            {
                Thread.currentThread().sleep(startAfterSeconds * 1000);
                while (!timeOutReached() && keepWaiting()){
                    try {
                        //sleep for 30 seconds
                        Thread.currentThread().sleep(30000);
                        computeTimeOut();
                    }
                    catch (InterruptedException ie){
                        timeOutReached = true;
                    }
                }
            }
            catch (Exception e){
                timeOutReached = true;
            }

            if((status.intValue() == RUNNING_STATE) && (client != null)){
                if(monitoringObjectName != null){
                    try{
                        ObjectName objectName = ObjectName.getInstance(monitoringObjectName);
                        if(connection != null){
                            //sleep for 30 seconds
                            Thread.currentThread().sleep(30000);
                            if (connection.isRegistered( new ObjectName( monitoringObjectName )  )){
                                notification.setUserData(new ObjectName(monitoringObjectName));
                                client.handleNotification(notification, handback);
                            }
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                } else {
                    client.handleNotification(notification, handback);
                }
            }
        }


        private boolean timeOutReached(){
            return timeOutReached;
        }

        private void computeTimeOut(){
            long currentTime = System.currentTimeMillis();
            timeOutReached =
                ((currentTime - startTime) >= (timeOutSeconds * 1000));
        }

        private boolean keepWaiting(){
            boolean returnValue = true;
            status = null;
            if((objectName != null) && (connection != null)){
                try{
                    status = (Integer)connection.getAttribute(objectName, "state");   //NOI18N
                    if((status.intValue() == RUNNING_STATE) || (status.intValue() == FAILED_STATE)){
                        returnValue = false;
                    }
                }
                catch(Exception ex){
                    status = null;
                    returnValue = false;
                }
            }
            return returnValue;
        }
    }


   /**
    * Method that froms the objectname for the monitoring mbean from the objectname
    * of the runtime mbean and <object-name> element of <code>notification.xml</code>.
    * This method this stores runtime mbean objectname and monitoring mbean
    * objectname into the map for later use.
    *
    * @param domain the domain the monitoring mbean domain
    * @param name the in-memory representation of <object-name> element of <code>notification.xml</code>.
    * @param objectName the runtime mbean <code>ObjectName</code>
    */
    private void updateObjectNameMap(String domain,
            com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName name,
                ObjectName objectName){
        String template = name.getMonitoringMbeanNameTemplate();
        if(template != null){
            template = domain + ":" + template;                 //NOI18N

            Mapping mapping = null;
            String property = null;
            String value = null;
            Map keyValueMap = new HashMap();
            for(int i=0; i<name.sizeMapping(); i++){
                mapping = name.getMapping(i);
                value = objectName.getKeyProperty(mapping.getRuntimeMbeanAttributeName());
                property = mapping.getMonitoringMbeanAttributeValue();

                //In case of web-modules, the name key vaule of runtime mbean
                //object name is different from the one in monitoring mbean
                //object name. In runtime mbean object name, the name 
                //key value is context + name. We need to account for this.
                if(value.startsWith("//")){
                    value = value.substring(value.lastIndexOf('/') + 1);
                }
                keyValueMap.put(property, value);
            }
            String monitoringMBeanObjectName;
            try{
                monitoringMBeanObjectName = ObjectNameHelper.tokenize(template, keyValueMap);
                runtimeToMonitoring.put(objectName, monitoringMBeanObjectName);
            }catch(NoSuchFieldException e){
                //do nothing
            }
        }
    }


   /**
    * Returns ObjectName of the object that sent the gvien <code>Notification</code>
    *
    * @returns ObjectName the ObjectName extracted from the given <code>notification</code> parameter
    */
    private ObjectName getObjectName(Notification notification){
        ObjectName objectName = null;
        if(notification.getClass().getName().equals("javax.management.MBeanServerNotification")){  //NOI18N
            MBeanServerNotification msn = (MBeanServerNotification)notification;
            //printInfo(msn);
            String canonicalName = msn.getMBeanName().getCanonicalName();
            try {
                objectName = new ObjectName(canonicalName);
            } catch(javax.management.MalformedObjectNameException ex){
                Utils.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return objectName;
    }


   /**
    * Returns the registered ObjectName for the given <code>MBeanServerConnection</code>.
    * To be specific it gets the object name from the derived class, forms the ObjectName
    * object from it and returns it if and only if, its registered object on the given 
    * connection.
    *
    * @returns ObjectName the registered ObjectName on the given connection.
    */
    private ObjectName getObjectName(MBeanServerConnection connection) {
        String objectNameString = getObjectNameString();
        if((objectNameString != null) && (objectNameString.length() > 0)){
            try{
                ObjectName objectName = ObjectName.getInstance(objectNameString);
                if(connection != null){
                    if (connection.isRegistered( new ObjectName( objectNameString )  )){
                        return objectName;
                    }
                }
            }catch(Exception ex){
                Utils.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return null;
    }


   /**
    * Returns Notifications object; in-memory representation of 
    * com/sun/mfwk/agent/appserv/lifecycle/notifications.xml.
    * 
    * @returns Notifications the in-memory representation of
    * com/sun/mfwk/agent/appserv/lifecycle/notifications.xml
    */
    static private Notifications getNotifications(){
        if(notifications == null){
            InputStream inputStream = Utils.getInputStream(notificationsFile);
  	    if(inputStream != null) {
	        try {	
                    notifications = Notifications.read(inputStream);
		} catch(Exception e) {
                    Utils.log(Level.WARNING, e.getMessage(), e);
		}
            } else {
                String format =
                BundleReader.getValue("MSG_Unable_to_use_file");        //NOI18N
                Object[] arguments = new Object[]{notificationsFile };
                Utils.log(Level.WARNING, MessageFormat.format(format, arguments));
            }
        }
	return notifications;
    } 


    private static String notificationsFile = 
        "com/sun/mfwk/agent/appserv/lifecycle/dtds/notifications.xml";


    private static Notifications notifications = null;
    private MBeanServerConnection connection = null;
    private NotificationListener client = null;
    private NotificationFilter filter = null;
    private Object handback = null;
    private Map runtimeToMonitoring;
    private String asInstance = null;
    private String asDomain = null;

    public static final int STARTING_STATE = 0;
    public static final int RUNNING_STATE = 1;
    public static final int STOPPING_STATE = 2;
    public static final int STOPPED_STATE = 3;
    public static final int FAILED_STATE = 4;
}
