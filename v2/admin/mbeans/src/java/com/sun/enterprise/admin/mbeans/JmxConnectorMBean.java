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
 * $Id: JmxConnectorMBean.java,v 1.2 2006/03/18 00:34:36 kravtch Exp $
 */

package com.sun.enterprise.admin.mbeans;

//JMX imports
import javax.management.AttributeNotFoundException;
import javax.management.ReflectionException;
import javax.management.MBeanException;

import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxServiceUrlFactory;
import javax.management.remote.JMXServiceURL;

// commons imports
import com.sun.enterprise.util.i18n.StringManager;

//admin/config imports
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.MBeanConfigException;

//config imports
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.ConfigException;

// java.net
import java.net.InetAddress;
import java.net.UnknownHostException;

public class JmxConnectorMBean extends BaseConfigMBean
{
    final private static String HOST_HOLDER_VALUE = "<host-name>";
    final private static int PORT_HOLDER_VALUE = 12345;

    private static final StringManager localStrings =
            StringManager.getManager(DomainMBean.class);
    
    private String getJMXServiceURL()
       throws UnknownHostException
    {
        
        //first create template
        JMXServiceURL url = 
        JmxServiceUrlFactory.forJconsoleOverRmiWithJndiInAppserver(
                HOST_HOLDER_VALUE, PORT_HOLDER_VALUE);
        String strUrl  = url.toString();
        //now - modify it
        JmxConnector bean = (JmxConnector)this.getBaseConfigBean();
        String host = bean.getAddress();
        if(host!=null && host.trim().equals("0.0.0.0"))
        {
           //host = InetAddress.getLocalHost().getHostName();    
           host = null; //left <host-name> placeholder in url
        }
        if(host!=null)
            strUrl = strUrl.replaceAll(HOST_HOLDER_VALUE, host);
        try {
           int port = Integer.parseInt(bean.getPort());
           strUrl = strUrl.replaceAll(String.valueOf(PORT_HOLDER_VALUE), 
                   String.valueOf(port));
        } catch (Exception e)
        {
           strUrl = strUrl.replaceAll(String.valueOf(PORT_HOLDER_VALUE), 
                   "<port>");
        }
        
        return strUrl;
    }
    
    /** overriding of the super getAttribute() 
     *  to provide "in mbean only" JMXServiceURL attribute
     **/
    public Object getAttribute(String name)
       throws AttributeNotFoundException, MBeanException, ReflectionException {

        if(name.equals(JMX_SERVICE_URL_ATTRNAME))
        {
            try {
                return getJMXServiceURL();
            } catch (UnknownHostException uhe) {
                throw new MBeanException(uhe);
            }
        }
        return super.getAttribute(name);
    }
    
    public static final String JMX_SERVICE_URL_ATTRNAME  = "JMXServiceURL";
}
