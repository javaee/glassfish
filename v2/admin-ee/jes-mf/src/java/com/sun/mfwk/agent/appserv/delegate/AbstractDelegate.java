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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.delegate;

import com.sun.mfwk.CMM_MBean;
import com.sun.mfwk.MfDelegate;
import com.sun.mfwk.MfMonitoringState;
import com.sun.mfwk.MfStatesManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import javax.management.ReflectionException;

/**
 * AbstractDelegate provides facilities to handle the mapping 
 * of CMM objects to AS objects. It does the following:
 * <pre>
 * 1) Builds the table of mapping
 * 2) Builds the default values
 * 3) Provides the appropriated getters
 * 4) Implements the initialize method of the com.sun.mfwk.MfDelegate interface
 * 5) Implements the finalize method of the com.sun.mfwk.MfDelegate interface 
 *    (not supported in this example)
 * 6) Implements the invoke method (not currently supported)
 * 7) Implements the refresh method
 * </pre>
 */
public abstract class AbstractDelegate implements MfDelegate {
    
    /** Cmm creation class name of corresponding CMM object */
    public CMM_MBean object = null;
    
    /**
     * Connection to use to get the attributes from the instrumented 
     * Component Product
     */
    public MBeanServerConnection mbs=null;
    
    /** ObjectName of the instrumented Component Product object */
    public ObjectName instrumentedObjectName=null;

    /** Application Server instance name */
    public String serverName = null;

    /** Application Server domain name */
    public String domainName = null;
    
    private Map mapper = null;
    
    private Integer monitoringState = MfMonitoringState.STATE_CREATED;
    private ArrayList managementChainStates = new ArrayList();
    private Boolean managementChainActivated = Boolean.FALSE;

    /**
     * Default contructor
     */
    public AbstractDelegate() {
        this.mapper = new HashMap();
    }
    
    /**
     * Adds a mapping entry for wellknown attribute
     *
     * @param CMM_Attribute attribute to provide as defined by the 
     *        CMM specifications
     * @param mapping_Attribute attribute as defined in the Java ES 
     *        Application Server Component Product
     * @param peer ObjectName of the AS object MBean
     */
    public void addMappingEntry(String CMM_Attribute, String mapping_Attribute,
            ObjectName peer, AttributeHandler handler) {
        
        mapper.put(CMM_Attribute, 
            new RemoteAttribute(mapping_Attribute, peer, handler));
    }

    /**
     * Adds a mapping entry for wellknown attribute
     *
     * @param CMM_Attribute attribute to provide as defined by the 
     *        CMM specifications
     * @param mapping_Attribute attribute as defined in the Java ES 
     *        Application Server Component Product
     * @param peer ObjectName of the AS object MBean
     */
    public void addMappingEntry(String CMM_Attribute,
            String mapping_Attribute,
            ObjectName peer) {
        
        mapper.put(CMM_Attribute, new RemoteAttribute(mapping_Attribute, peer));
    }
    
    /**
     * Adds a default mapping entry for wellknown attribute without 
     * requesting for attributes values in Java ES Application server instance.
     *
     * @param CMM_Attribute attribute to provide as defined by the 
     *        CMM specifications
     * @param defaultValue default attribute as defined in the Java 
     *        ES Application Server Component Product
     */
    public void addDefaultMappingEntry(String CMM_Attribute,
            Object defaultValue) {
        
        mapper.put(CMM_Attribute, defaultValue);
    }
    
    /**
     * Gets an attribute value identified by its attribute name
     *
     * @param attribute name to get
     * @throws javax.management.AttributeNotFoundException if the attribute 
     *         is not found
     * @throws javax.management.MBeanException if a problem occurred
     * @throws javax.management.ReflectionException if a problem occurred
     * @return the value of the requested attribute or the default value
     */
    public Object getAttribute(String attribute)
        throws HandlerException, AttributeNotFoundException, MBeanException, ReflectionException  {

        Object value = mapper.get(attribute);

        if ( value instanceof RemoteAttribute ) {

            RemoteAttribute ra = (RemoteAttribute)value;

            try {
                if (ra.handler != null) {
                    return (ra.handler).handleAttribute(
                                        ra.peer, ra.attribute, this.mbs);
                } else {
                    return this.mbs.getAttribute(ra.peer, ra.attribute);
                }

            } catch (InstanceNotFoundException e) {

                LogDomains.getLogger().log(Level.WARNING, 
                    "InstanceNotFoundException while trying to get attribute " 
                    + ra.attribute + " from MBean " + ra.peer, e);

                throw new MBeanException(e);

            } catch (java.io.IOException e) {

                LogDomains.getLogger().fine(
                    "Found stale connection while trying to get attribute " 
                    + ra.attribute + " from MBean " + ra.peer 
                    + ". Server name: " + serverName);

                // server name is not set; can not refresh the connection
                if (serverName == null) {
                    throw new MBeanException(e);
                }

                try {
                    // refresh connection
                    ConnectionRegistry r = ConnectionRegistry.getInstance();
                    this.mbs = r.getConnection(serverName, domainName);

                    LogDomains.getLogger().fine(
                        "Refreshed connection for server: " + serverName);

                    if (ra.handler != null) {
                        return (ra.handler).handleAttribute(
                                            ra.peer, ra.attribute, this.mbs);
                    } else {
                        return this.mbs.getAttribute(ra.peer, ra.attribute);
                    }

                } catch (Exception ex) {
                    LogDomains.getLogger().log(Level.WARNING, 
                        "Exception while trying to get attribute " 
                        + ra.attribute + " from MBean " + ra.peer 
                        + ". Connection refresh attempt failed.", e);

                    throw new MBeanException(ex);
                }
            }

        } else if ( value != null ) {
            return value;
        }

        return "Not_Supported";
    }


    /**
     * Gets attribute values for the given attribute names
     *
     * @param attribute names to get the values of
     * @throws javax.management.AttributeNotFoundException if the attribute 
     *         is not found
     * @throws javax.management.MBeanException if a problem occurred
     * @throws javax.management.ReflectionException if a problem occurred
     * @return the values of the requested attributes
     */
    public AttributeList getAttributes(String[] attributes)
        throws HandlerException, AttributeNotFoundException, MBeanException, ReflectionException,
               InstanceNotFoundException, IOException  {
        if(attributes == null) {
            throw new IllegalArgumentException();
        }

        AttributeList attributeList = new AttributeList();
        Attribute attribute;
        for (int i=0; i<attributes.length; i++) {
            attributeList.add(new Attribute(attributes[i],
                getAttribute(attributes[i]))); 
        } 

        return attributeList;
    }    


    /**
     * Facility to perform a query names on the remote MBeanServer
     * @param pattern ObjectName pattern
     * @return the Set of matching ObjectNames
     */
    public Set queryNames(ObjectName pattern) {

        try {
            return this.mbs.queryNames(pattern, null);

        } catch(java.io.IOException e) {

            LogDomains.getLogger().fine(
                "Found stale connection for server: " + serverName);

            try {
                if (serverName != null) {
                    // refresh connection
                    ConnectionRegistry r = ConnectionRegistry.getInstance();
                    this.mbs = r.getConnection(serverName, domainName);

                    LogDomains.getLogger().fine(
                        "Refreshed connection for server: " + serverName);

                    return this.mbs.queryNames(pattern, null);
                }
            } catch (Exception ex) {
                System.err.println("Exception: " + ex);
            }

        } catch(Exception e) {
            System.err.println("Error: " + e);
        }

        return null;
    }
    
    /**
     * Implements the initialize method of com.sun.mfwk.MfDelegate. 
     * Instanciate a ModuleManagerSupport which initializes the 
     * delegateFactory with the SupportedDelegateClassName Map.
     *
     * @param params The MbeanServerConnection, CMM object of the delegate
     * @throws java.lang.Exception if a problem occured
     */
    public void initialize(Object[] params) throws Exception {

        CMM_MBean object=null;
        MBeanServerConnection mbs=null;
        ObjectName objectName=null;
        
        // Check params
        if (params.length!=3) {
            throw new Exception("Invalid Parameters");
        }
        
        if (params[0] instanceof com.sun.mfwk.CMM_MBean) {
            object=(CMM_MBean)params[0];
            this.object=object;
        } else {
            throw new Exception("Invalid Parameters");
        }
        
        if (params[1] instanceof javax.management.MBeanServerConnection) {
            mbs=(MBeanServerConnection)params[1];
            this.mbs=mbs;
        } else {
            throw new Exception("Invalid Parameters");
        }
        
        if (params[2] instanceof javax.management.ObjectName) {
            objectName=(ObjectName)params[2];
            this.instrumentedObjectName=objectName;
        } else {
            throw new Exception("Invalid Paramaters");
        }
        
    }
    
    /**
     * Not Supported
     *
     * @param action to perform
     * @param parameters of the method to invoke
     * @param signatures of the method parameters
     *
     * @throws java.io.IOException if a problem occured
     * @throws javax.management.MBeanException if a problem occured
     * @throws javax.management.ReflectionException if a problem occured
     * @throws javax.management.InstanceNotFoundException 
     *         if instance could not be found
     * @return the method result
     */
    public Object invoke(String action, Object[] parameters, 
            String[] signatures) 
            throws java.io.IOException, MBeanException, ReflectionException, 
            InstanceNotFoundException {
        
        return null;
    }
    
    /**
     * Not supported
     *
     * @throws java.lang.Exception if the refresh of attribute could not be done
     */
    public void refresh() throws Exception {
    }
    
    public Class getCMMInterface() {
        return this.object.getCMMInterface();
    }

    /**
     * Not supported
     *
     * @throws java.lang.Exception if the refresh of attribute could not be done
     */
    public void setAttribute(javax.management.Attribute attribute) 
            throws AttributeNotFoundException {
         String name = attribute.getName();

         //SNMP initialization
         //Set the monitoring state of the object. Send notification
         //to the MfStatesManager (main interface with SNMP mediation).
         if (name.equals(MfMonitoringState.MONITORING_STATE_ATTRIBUTE)) {
             setMonitoringState((Integer)attribute.getValue());
             return;
         }
    }

    /**
     * Not supported
     * @throws java.lang.Exception if the refresh of attribute could not be done
     */
    public void setOffLine(boolean isOffLine) {
    }

    public Map getAttributeMappings() {
        return mapper;
    }
    
    private class RemoteAttribute {
        String attribute = null;
        ObjectName peer = null;
        AttributeHandler handler = null;
        
        public RemoteAttribute(String attribute, ObjectName peer) {
            this(attribute, peer, null);
        }

        public RemoteAttribute(String attribute, ObjectName peer, 
                AttributeHandler handler) {

            this.attribute = attribute;
            this.peer = peer;
            this.handler = handler;
        }

        public String toString() {
            return "Attribute: " + attribute + " ObjectName: " + peer;
        }
    }

    private synchronized void setMonitoringState(Integer state) {
        if (this.monitoringState!=state) {
            Integer oldValue = this.monitoringState;
            this.monitoringState = state;

            MfStatesManager.sendNotification(MfMonitoringState.MONITORING_STATE_ATTRIBUTE,
                oldValue, this.monitoringState, this.object.getInstanceID());

            if (this.getCMMInterface().getName().equals("com.sun.cmm.CMM_Capabilities")) {
                ArrayList setState = new ArrayList();
                ArrayList oldList = this.managementChainStates;
                setState.add(MfMonitoringState.toString(state));
                this.managementChainStates.clear();
                this.managementChainStates.add(setState);
                object.sendAttributeChangeNotification(MfMonitoringState.CHAIN_STATE_ATTRIBUTE,
                    oldList, this.managementChainStates);
                this.setManagementChainActivated();
            }
        }
   }


   private synchronized void setManagementChainActivated() {

       boolean isActivated=true;
       String status=null;

       Iterator iterator = this.managementChainStates.iterator();
       while (iterator.hasNext() && (isActivated)) {
           ArrayList list = (ArrayList)iterator.next();
           if (!list.isEmpty()) {
               Iterator iterator2 = list.iterator();
               while (iterator2.hasNext() && (isActivated)) {
                   status = (String)iterator2.next();
                   if (!(status.equals(MfMonitoringState.STR_STATE_INITIALIZED))) {
                       isActivated = false;
                   }
               }
           }
       }

       Boolean newValue = new Boolean(isActivated);

       if (!newValue.equals(this.managementChainActivated)) {
           object.sendAttributeChangeNotification(MfMonitoringState.CHAIN_ACTIVATED_ATTRIBUTE,
           this.managementChainActivated, newValue);
           this.managementChainActivated = newValue;
       }

   }

}
