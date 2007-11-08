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

import javax.management.ObjectName;
import java.util.Set;
import java.util.Map;
import java.io.Serializable;
import com.sun.enterprise.admin.wsmgmt.msg.MessageTraceMgr;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.enterprise.admin.wsmgmt.stats.spi.StatsProviderManager;
import com.sun.enterprise.admin.wsmgmt.stats.spi.WebServiceEndpointStatsProvider;

/**
 * WebServiceEndpoint rutime MBean. This MBean acts as base class for
 * ServletWebServiceEndpoint and EJBWebServiceEndpoint.
 * Please use either ServletWebServiceEndpoint or EJBWebServiceEndpoint, inorder
 * to instantiate runtime mbean of type WebServiceEndpoint.
 */ 
public abstract class WebServiceEndpointMdl extends J2EEManagedObjectMdl {
    
    private static final String MANAGED_OBJECT_TYPE = "WebServiceEndpoint";
    protected static final String WEB_MBEAN = "ServletWebServiceEndpoint";
    protected static final String EJB_MBEAN = "EJBWebServiceEndpoint";

    private final String moduleName;
    private final String registrationName;
    private final String applicationName;
    private final String epName;
    private final boolean isEjb;
    private final boolean isStandAlone;
    private String mbeanName = null;
    
    /**
     * constructor.
     *
     * @param name          Name of the web service endpoint
     * @param moduleName    Name of the module (which this web service belongs)
     * @param regName       registration name of the application or module
     * @param isVirtual     true, if the module is stand alone, false otherwise
     * @param isEjb         If this Web service endpoint is implemented as EJB
     *                      or Servlet
     */
    WebServiceEndpointMdl(String name, String mName, String regName,
    boolean isVirtual, boolean isejb) {
        super(name,false, false, false);
        this.moduleName = mName;
        this.applicationName = regName;
        this.registrationName = regName;
        this.isStandAlone = isVirtual;
        this.epName = name;
        this.isEjb = isejb;
        if (isejb == true) {
            mbeanName = EJB_MBEAN;
        } else {
            mbeanName = WEB_MBEAN;
        }
    }

    /**
     * constructor.
     *
     * @param name          Name of the web service endpoint
     * @param moduleName    Name of the module (which this web service belongs)
     * @param regName       registration name of the application or module
     * @param serverName    Name of the server instance
     * @param isVirtual     true, if the module is stand alone, false otherwise
     * @param isEjb         If this Web service endpoint is implemented as EJB
     *                      or Servlet
     */
    WebServiceEndpointMdl(String name, String moduleName, 
            String regName, String serverName, boolean isVirtual,
            boolean isejb) {

        super(name, serverName, false, false, false);
        this.moduleName = moduleName;
        this.applicationName = regName;
        this.registrationName = regName;
        this.epName = name;
        this.isEjb = isejb;
        if (isejb == true) {
            mbeanName = EJB_MBEAN;
        } else {
            mbeanName = WEB_MBEAN;
        }
        this.isStandAlone = isVirtual;
    }

    /**
     * Accessor method for the module name key
     *
     * @return String   Module's name
     */
    public String getModule(){
       return this.moduleName;
    }

    /**
     * Accessor method for the J2EE Application key
     *
     * @return String   Application's name
     */
    public String getJ2EEApplication(){
       return this.applicationName;
    }
    
    /**
     * This returns the underlying implementation Servlet or EJB' type
     * It could be J2EETypes.SERVLET or J2EETypes.EJB.
     *
     * @return String   "EJB" or "SERVLET"
     */
 	public String getImplementationType() {
        if (isEjb) {
            return "EJB";
        } else {
            return "SERVLET";
        }
    }

	/**
	 * Returns last N message content and info collected for this web service.
     *
     * @return MessageTrace[] collection message content/trace information
	 */
	public Map<String,Serializable>[] getMessagesInHistory() {
        String partialEpName = null;

        if (isStandAlone) {
            // standalone module
            partialEpName = this.epName;
        } else {
            partialEpName = this.moduleName+ "#" + this.epName;
        }

        Map<String,Serializable>[] maps = null; 
        MessageTrace[] result = 
            MessageTraceMgr.getInstance().getMessages(this.registrationName,
                 partialEpName);
        if ( result == null) {
            return null;
        }
        if (  result.length > 0 ) {
            maps = new Map[result.length];
            for ( int idx =0; idx < result.length; idx++) {
                maps[idx] = result[idx].asMap();
                TypeCast.checkSerializable( maps[ idx] );
            }
        } 
        return maps;
        
    }

    /**
     * The type of the J2EEManagedObject as specified by JSR77. The class that 
     * implements a specific type must override this method and return the 
     * appropriate type string.
     * 
     * @return String J2eeType of this Managed Object
     */
    public String getj2eeType() {
        return MANAGED_OBJECT_TYPE;
    }

    /**
     * The MBean name of the J2EEManagedObject as specified in
     * runtime-mbeans-descriptors.xml. This value is used in registering the 
     * MBean.
     * 
     * @return String MBeanName value of this Managed Object
     */
    public abstract String getMBeanName(); 

    /**
     * Returns the stats provider for this web service endpoint.
     */
    private WebServiceEndpointStatsProvider getWSProvider() {
        final String NS = "#";
        StatsProviderManager spMgr = StatsProviderManager.getInstance();

        String fqName = null;
        if (isStandAlone == false) {
            fqName = registrationName + NS + moduleName + NS + epName;
        } else {
            fqName = registrationName + NS + epName;
        }
        WebServiceEndpointStatsProvider provider = 
                spMgr.getEndpointStatsProvider(fqName);

        return provider;
    }

    /**
     * Resets the statistics.
     */
    public void resetStats() {
        WebServiceEndpointStatsProvider provider = getWSProvider();
        if (provider != null) {
            provider.reset();
        }
    }

    /**
     * Returns the last reset time stamp in milliseconds.
     *
     * @return last reset time stamp in milliseconds
     */
    public long getLastResetTime() {
        long resetTime = 0;
        WebServiceEndpointStatsProvider provider = getWSProvider();
        if (provider != null) {
            resetTime = provider.getLastResetTime();
        }

        return resetTime;
    }

   public String getobjectName() {
	// The end point is exposed either thru a web module or an ejb module
	// which have proper jsr77 mbeans. This method is provided to satisfy the 
	// abstarct method in the super class
	return null;
   }
}
