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

package com.sun.enterprise.admin.server.core.mbean.config;

//JMX imports
import javax.management.MBeanInfo;

//Admin imports
import com.sun.enterprise.admin.common.exception.JCAAdminException;
import com.sun.enterprise.admin.common.ResourceAdapterInfo;
import com.sun.enterprise.admin.server.core.mbean.meta.MBeanEasyConfig;

//JDK imports
import java.util.Properties;
import java.util.List;

/**
    Handler for all JMS related admin requests.
    <p>
    ObjectName of this MBean is: 
		ias:type=jmsadmin
*/

public class JCAAdminHandler extends AdminBase {

  public JCAAdminHandler() {
  }

  /**
   *return a list of resource adapter names that are deployed
   */      
  public Object[] listResourceAdapterNames(String iASInstanceName) throws JCAAdminException {
       return null;
  }
  
  /**
   *return the properties of a specific resource adapter
   */
  public ResourceAdapterInfo getResourceAdapterProperties(String iasInstanceName, 
                                String adapterName) throws JCAAdminException {
      return null;
  }
  
  /**
   *return the value of a specific property for a specific resource adapter
   */
  public Object getResourceAdapterInstanceProperty(String iasInstanceName, String adapterInstance,
                                      String propertyName) throws JCAAdminException {
      return new Object();
  }
    /** Every resource MBean should override this method to execute specific
     * operations on the MBean. This method is enhanced in 8.0. It was a no-op
     * in 7.0. In 8.0, it is modified to invoke the actual method through
     * reflection.
     * @since 8.0
     * @see javax.management.MBeanServer#invoke
     * @see #getImplementingClass
     */
    protected Class getImplementingClass() {
        return ( this.getClass() );
    }
    
    /** Reflection requires the implementing object.  */
    protected Object getImplementingMBean() {
        return ( this );
    }
    /** Implementation of getMBeanInfo. This appears here, so that this class
     * can do additional things in case it wants to have different information
     * in the MBeanInfo. Ideally the superclass AdminBase should be able to do
     * this.
    */
	public MBeanInfo getMBeanInfo()
	{
	    try {
	        return (new MBeanEasyConfig(getClass(), mAttrs, mOpers, null)).getMBeanInfo();
	    } 
	    catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    final String[] mAttrs = new String[0];
    
    final String[] mOpers = new String[] 
    {
        "getResourceAdapterInstanceProperty(String iasInstanceName, String adapterInstance), INFO",
        "getResourceAdapterProperties(String iasInstanceName, String adapterName), INFO",
        "listResourceAdapterNames(String iASInstanceName), INFO",
    };
}
