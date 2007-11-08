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

package com.sun.enterprise;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Timer;
import javax.ejb.spi.HandleDelegate;

import com.sun.ejb.*;
import com.sun.enterprise.ManagementObjectManager;
import com.sun.enterprise.resource.ResourceInstaller;
import javax.resource.spi.ConnectionManager;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.naming.ProviderManager;


import com.sun.enterprise.util.FeatureAvailability;

//IASRI 4717059 BEGIN
//import com.sun.ejb.ROBNotifier;  
//IASRI 4717059 END

/**
 * The Switch class holds references to all the components in an EJB
 * server including Containers, TM, Protocol Manager, etc.
 */
public class Switch {

    private static final String EJB_CONFIG_FILE = "ejbconfig.properties";
    public static final int APPCLIENT_CONTAINER = 1;
    public static final int EJBWEB_CONTAINER = 2;
    private static final Switch theSwitch   = new Switch();
    
    //private Vector containers = new Vector();
    private Hashtable containerDescriptorTable = new Hashtable();
    private ProtocolManager protocolManager;
    private J2EETransactionManager tm;
    private ContainerFactory containerFactory;
    private InvocationManager invocationManager;
    private NamingManager namingManager;
    private InjectionManager injectionManager;
    private PoolManager poolManager;
    private ResourceInstaller resourceInstaller;
    private Timer timer;
    private HandleDelegate handleDelegate;
    private int containerType;
    private volatile ManagementObjectManager managementObjectManager;
    private Agent callFlowAgent;
    private ProviderManager providerManager;
    
	//IASRI 4717059 BEGIN
	/*
    private ROBNotifier robNotifier;
    
    public ROBNotifier getROBNotifier() {
	    return robNotifier;
    }

    public void setROBNotifier(ROBNotifier robNotifier) {
    	this.robNotifier = robNotifier;
    } 
	*/
	//IASRI 4717059 END

    public static Switch getSwitch() {
        return theSwitch;
    }
    
    public ProviderManager getProviderManager() {
	return providerManager;
    }
    
    public void setProviderManager(ProviderManager pf) {
	providerManager = pf;
    }

    public ProtocolManager getProtocolManager() {
	return protocolManager;
    }

    public void setProtocolManager(ProtocolManager pm) {
	protocolManager = pm;
    }

    public NamingManager getNamingManager() {
	return namingManager;
    }

    public void setNamingManager(NamingManager nm) {
	namingManager = nm;
    }

    public InjectionManager getInjectionManager() {
        return injectionManager;
    }

    public void setInjectionManager(InjectionManager im) {
        injectionManager = im;
    }

    public J2EETransactionManager getTransactionManager() {
	return tm;
    }

    public void setTransactionManager(J2EETransactionManager tm) {
	this.tm = tm;
    }

    public PoolManager getPoolManager() {
        return poolManager;
    }

    public void setPoolManager(PoolManager poolManager) {
        this.poolManager = poolManager;
    }

    public ResourceInstaller getResourceInstaller() {
        return resourceInstaller;
    }

    public void setResourceInstaller(ResourceInstaller resourceInstaller) {
        this.resourceInstaller = resourceInstaller;
    }

    public InvocationManager getInvocationManager() {
        return invocationManager;
    }

    public void setInvocationManager(InvocationManager invocationManager) {
        this.invocationManager = invocationManager;
    }
    

    public ContainerFactory getContainerFactory() {
	    return containerFactory;
    }

    public void setContainerFactory(ContainerFactory containerFactory) {
    	this.containerFactory = containerFactory;
    }

    /**
     * Returns the deployment descriptor for the EJB container or
     * Servlet context provided. This is used by the Transaction/Naming
     * Security Managers.
     */
    public Object getDescriptorFor(Object containerContext)
    {
	    return containerDescriptorTable.get(containerContext);
    }

    /**
     * Sets the deployment descriptor for the EJB container or
     * Servlet context provided. 
     */
    public Object setDescriptorFor(Object containerContext, Object desc)
    {
	    return containerDescriptorTable.put(containerContext, desc);
    }

    /**
     * Remove the descriptor from the hashtable
     */
    public void removeDescriptorFor(Object containerContext)
    {
	    containerDescriptorTable.remove(containerContext);
    }

    private final Object getManagementObjectManagerLock = new Object();
    
    public ManagementObjectManager getManagementObjectManager() {
        synchronized( getManagementObjectManagerLock ) {
            if ( managementObjectManager == null ) {
                   try {
                    managementObjectManager = (ManagementObjectManager)
                        Class.forName("com.sun.enterprise.management.util.J2EEManagementObjectManager").newInstance();
                } catch(Exception e) {
                    System.err.println(e);
                }
            }
        }
        return managementObjectManager;
    }   

    private final Object getTimerLock = new Object();
    public Timer getTimer() {
        synchronized( getTimerLock ) {
            if( timer == null ) {
                // Create a scheduler as a daemon so it
                // won't prevent process from exiting.
                timer = new Timer(true);
            }
        }
        return timer;
    }
 
    private final Object getHandleDelegateLock = new Object();
    
    public HandleDelegate getHandleDelegate() {
        synchronized(getHandleDelegateLock) {
            if (handleDelegate == null) {
                handleDelegate =
                    com.sun.enterprise.iiop.IIOPHandleDelegate.getHandleDelegate();
            }
        }
        return handleDelegate;
    }
    
    public void setContainerType(int type){
	containerType = type;
    }
    public int getContainerType(){
	return containerType;
    }

    public synchronized void setCallFlowAgent(Agent callFlowAgent) {
    	this.callFlowAgent = callFlowAgent;
        
        FeatureAvailability.getInstance().registerFeature(
            FeatureAvailability.CALL_FLOW_FEATURE, callFlowAgent );
    }
        
    public Agent getCallFlowAgent() {
    	return this.callFlowAgent;
    }
}















