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

package com.sun.enterprise.ee.admin.cascading;

import com.sun.jdmk.remote.cascading.proxy.ProxyCascadingAgent;
import javax.management.remote.JMXServiceURL;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import com.sun.jdmk.remote.cascading.MBeanServerConnectionFactory;
import javax.management.QueryExp;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.ee.EELogDomains;

/**
 * This class extends ProxyCascadingAgent so that we can install
 * our own CascadingProxy. This enables us gain control on proxying 
 * behavior. While cascading happens, we can do application server
 * specific processing, for ex. registartion of monitoring mBeans
 * in the dotted name registry.
 *
 * @author Sreenivas Munnangi
 */

public class ASProxyCascadingAgent extends ProxyCascadingAgent {

    // Logger and StringManager
    private static final Logger _logger =
	Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    private static final StringManager _strMgr =
	StringManager.getManager(ASProxyCascadingAgent.class);
    
    // Pass thru constructors
    // which will invoke the corresponding construtor
    // on super class

    public ASProxyCascadingAgent(MBeanServerConnectionFactory sourceConnection,
                               ObjectName  sourcePattern,
                               QueryExp    sourceQuery,
                               String      targetPath,
			       MBeanServer mBeanServer,
                               String      description) {
	super(sourceConnection,sourcePattern,sourceQuery,targetPath,mBeanServer,description);
    }

    public ASProxyCascadingAgent(MBeanServerConnectionFactory sourceConnection,
                               ObjectName  sourcePattern,
                               QueryExp    sourceQuery,
                               String      description) {
	super(sourceConnection,sourcePattern,sourceQuery,description);
    }

/*
    public ASProxyCascadingAgent(JMXServiceURL jMXServiceURL, 
			       Map map, 
			       ObjectName objectName, 
			       QueryExp queryExp, 
			       String s, 
			       String s1) throws java.io.IOException {
	super(jMXServiceURL,map,objectName,queryExp,s,s1);
    }
*/


    /**
     * Extends the default behavior by returning the
     * extended class of CascadingProxy.
     * @param  sourceName The name of the source MBean.
     * @param  cf The <tt>MBeanServerConnectionFactory</tt> used to obtain 
     *         connections with the source <tt>MBeanServer</tt>.
     * @return A new cascading proxy for the given source MBean.
     **/
    protected Object createProxy(ObjectName sourceName,
				 MBeanServerConnectionFactory cf) {
	if (sourceName != null) {
            _logger.log(Level.FINE, "cascading.proxycasacdingagent.objectname", sourceName);
	}
	return new ASCascadingProxy(sourceName,cf);
    }

}
