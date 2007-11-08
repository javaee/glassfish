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

package com.sun.enterprise.management.model;

import java.util.*;
import javax.management.*;
import javax.naming.*;
import javax.naming.event.*;
import com.sun.appserv.server.util.Version;
import com.sun.appserv.management.util.jmx.JMXUtil;


public class J2EEDomainMdl extends J2EEEventProviderMOMdl implements NotificationListener {
    public static final String DOMAINNAME;
    private static final ObjectName jmImpl;
    private final boolean debug = false;
  
    static {
        String      domainNameTemp = null;
        ObjectName jmImplTemp   = null;
        
        try {
            //DOMAINNAME  = com.sun.enterprise.server.ApplicationServer.getServerContext().getDefaultDomainName();
            //DOMAINNAME = java.net.InetAddress.getLocalHost().getHostName();DOMAINNAME = "com.sun.appserv";
            // By default lets set it to com.sun.appserv
            domainNameTemp = com.sun.enterprise.management.util.J2EEModuleUtil.getDomainName(); 
            jmImplTemp = JMXUtil.newObjectName("JMImplementation:type=MBeanServerDelegate");
        } catch (Exception e) {
            domainNameTemp = "com.sun.appserv";
            jmImplTemp      = null;
        }
        finally {
            DOMAINNAME  = domainNameTemp;
            jmImpl      = jmImplTemp;
        }
    }

    private static final String MANAGED_OBJECT_TYPE = "J2EEDomain";

    private final String [] eventTypes = new String [] {"j2ee.object.created","j2ee.object.deleted"};

    public J2EEDomainMdl() {
        super(DOMAINNAME, false, false);
        try {
            getMBeanServer().addNotificationListener(jmImpl, this, null, "J2EEDomain");
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }
    public J2EEDomainMdl(String domainname) {
        super(domainname == null ? DOMAINNAME : domainname, false, false);
        if ( ! domainname.equals( DOMAINNAME ) )
        {
            throw new IllegalArgumentException();
        }

        try {
            getMBeanServer().addNotificationListener(jmImpl, this, null, "J2EEDomain");
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }
    public J2EEDomainMdl(String domainname, String serverName) {
        super(domainname, serverName, false, false);
        /*
        DOMAINNAME = domainname;
        */
        try {
            getMBeanServer().addNotificationListener(jmImpl, this, null, "J2EEDomain");
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }
    //constructor for generic instantiation from the MBeanRegistry
    public J2EEDomainMdl(String[] location) {
        this(location[0]);
    }

    // Whether events are to be rebroadcast from all MBeans in the domain.
    // NOTE: These JSR 77 MBeans are *not* a public interface (private internal implementation detail).
    // So there is no point in complying with a standard for MBeans that no-one is supposed to use!
    // Clients should use only the AMX JSR 77 MBeans.
    static final boolean    REBROADCAST = true; // turned on, pending testing
    
		public void
	postRegister( Boolean registrationDone ) {

        if ( registrationDone.booleanValue() ) {
            if ( ! getObjectName().getDomain().equals( DOMAINNAME ) )
            {
                throw new IllegalArgumentException();
            }
            
            if ( REBROADCAST ) {
                try {
                    getMBeanServer().addNotificationListener(jmImpl, this, null, "J2EEDomain");
                }
                catch( Exception e ) {
                    mLogger.warning( e.toString() );
                    throw new RuntimeException( "J2EEDomainMdl.postRegister", e );
                }
           }
        }
    }
    
/** 
* A list of all J2EE Servers in this domain. 
* @supplierCardinality 0..* 
*/ 
    public String[] getservers(){
        Set servers = findNames("j2eeType=J2EEServer");

        Iterator it = servers.iterator();
        String [] ret = new String[servers.size()];
        int i =0;
        while(it.hasNext()) {
            ret[i++] = ((ObjectName)it.next()).toString();
        }
        return ret;
    }

    public String[] geteventTypes(){
        return eventTypes;
    }


    // Modified to fix bug# 6290783 
    // Discussed among Sreeni, Lloyd and Hans
    // Pl. refer to section JSR77.3.1.1.4 for more details.
    public void handleNotification(Notification n, Object handback) {
        if (! (n instanceof MBeanServerNotification)) {
            return;
        }
        
        //final ObjectName selfObjectName = getObjectName();
        ObjectName selfObjectName = null;
        try {
            selfObjectName = new ObjectName(getobjectName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // do not listen to itself
        final ObjectName source = ((MBeanServerNotification)n).getMBeanName();
        if (source.equals( selfObjectName ) ) {
            return;
        }

        // J2EEDomain managed object must emit all events from all 
        // event providers in the domain. Note that it is not restricted
        // to jsr77 types but applicable for all events in this domain.
        // So make sure the events belong to this domain and not other domains.

        if (! source.getDomain().equalsIgnoreCase( selfObjectName.getDomain() )) {
            return;
        }

        // Handle register and unregister event notifications.

        final String nType   = n.getType();
        MBeanServerNotification jsr77N = null;

        if (nType.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
            if ( isEventProvider(source) ) {
                try {
                    getMBeanServer().addNotificationListener( source, this, null, getname() );
                } catch (Exception e) {
                    if (debug) e.printStackTrace();
                }
            }
            
            jsr77N = new MBeanServerNotification("j2ee.object.created", selfObjectName, n.getSequenceNumber(), source);
         }
         else if ( nType.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION) ) {
            jsr77N = new MBeanServerNotification("j2ee.object.deleted", selfObjectName, n.getSequenceNumber(), source);
         }

        if (jsr77N != null) {
            // notify listeners of an new MBean in the domain
            sendNotification(jsr77N);
            return;
        }
        
        // send the original Notification
        sendNotification( n );
    }

    /**
     * The type of the J2EEManagedObject as specified by JSR77. The class that implements a specific type must override this method and return the appropriate type string.
     */
    public String getj2eeType() {
        return MANAGED_OBJECT_TYPE;
    }
    
    /**
     * The name of the J2EEManagedObject. All managed objects must have a unique name within the context of the management
     * domain. The name must not be null.
     */
    public final String getobjectName() {
        Set s = findNames("j2eeType="+getj2eeType()+",name="+DOMAINNAME);
        Object [] objs = s.toArray();
        if (objs.length > 0) {
            String name = ((ObjectName)objs[0]).toString();
            return name;
        } else {
            return null;
        }}
    
    /**
     * The full version of the application server product.
     */
    public String getapplicationServerFullVersion() {
        return Version.getFullVersion();
    }
    
    /**
     * The version of the application server product.
     */
    public String getapplicationServerVersion() {
        return Version.getVersion();
    }
    

    boolean isEventProvider( final ObjectName candidate ) {
	boolean isEventProvider = false;
	try {
		final MBeanInfo info    = getMBeanServer().getMBeanInfo( candidate);
		if ( JMXUtil.getMBeanAttributeInfo( info, "eventProvider" ) != null ) {
	    		isEventProvider = (Boolean)getMBeanServer().getAttribute( 
						candidate, "eventProvider");
		}
	} catch (Exception e) {
		if (debug) e.printStackTrace();
	}
	return isEventProvider;
    }
}
