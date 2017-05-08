/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * ConnectionImplTest.java
 *
 * Created on October 2, 2000, 10:36 AM
 */

package com.sun.enterprise.tools.verifier.tests.connector.cci;

import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.*;
import java.util.*;

/**
 * Utility methods for all tests related to java.resource.cci.Connection 
 * interface implementation
 *
 * @author  Jerome Dochez
 * @version 
 */
public abstract class ConnectionTest extends CCITest {

   /**
     * <p>
     * Get the <code>Class</code> object of the class declared as implementing
     * the javax.resource.cci.Connection interface and declared in the 
     * archive deployment descriptors under the connection-impl-class
     * </p> 
     *
     * @param descriptor the rar file deployment descriptor
     * @param result instance to use to put the result of the test
     * @return Class object for the connectionimpl-class that implements
     * javax.resource.cci.Connection
     */
    protected Class getConnectionImpl(ConnectorDescriptor descriptor,
    Result result) 
    {
      ComponentNameConstructor compName = 
        getVerifierContext().getComponentNameConstructor();
      OutboundResourceAdapter outboundRA =
        descriptor.getOutboundResourceAdapter();
      if(outboundRA == null)
      {
        return null;
      }
      Set connDefs = outboundRA.getConnectionDefs();
      Iterator iter = connDefs.iterator();
      while(iter.hasNext()) 
      {
        ConnectionDefDescriptor connDefDesc = (ConnectionDefDescriptor)
          iter.next();
        String impl = connDefDesc.getConnectionImpl();
        Class implClass = null;
        try
        {
          implClass = Class.forName(impl, false, getVerifierContext().getClassLoader());
        } catch(ClassNotFoundException cnfe) {
          result.addErrorDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.failed(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.cci.ConnectionTest.nonexist",
               "Error: The class [ {0} ] as defined in the connection-impl-class deployment descriptor does not exist",
               new Object[] {impl}));
          return null;
        }            
        if(isImplementorOf(implClass, "javax.resource.cci.Connection"))
          return implClass;
      }
      return null;
    }


    /**
     * <p>
     * Test whether the class declared in the deployemnt descriptor under the 
     * connection-impl-class tag is available
     * </p>
     * 
     * @param descriptor the deployment descriptor
     * @param result instance to use to put the result of the test
     * @return true if the test succeeds
     */
    protected Class testConnectionImpl(ConnectorDescriptor descriptor, Result result) 
    {
      Class mcf = getConnectionImpl(descriptor, result);
      if (mcf == null) {
        ComponentNameConstructor compName = 
          getVerifierContext().getComponentNameConstructor();
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            ("com.sun.enterprise.tools.verifier.tests.connector.cci.ConnectionTest.nonimpl", 
             "Error: The resource adapter must implement the javax.resource.cci.Connection interface and declare it in the connection-impl-class deployment descriptor."));                
      }
      return mcf;
    }

    /**
     * <p>
     * Test wether the class declared in the deployemnt descriptor under the 
     * connectionfactory-impl-class tag implements an interface 
     * </p>
     * 
     * @param descriptor the deployment descriptor
     * @param interfaceName the interface name we look for
     * @param result instance to use to put the result of the test
     * @return true if the test succeeds
     */
    protected boolean testImplementationOf(ConnectorDescriptor descriptor, String interfaceName, Result result) 
    {
      Class mcf = testConnectionImpl(descriptor, result);
      if (mcf != null) 
        return testImplementationOf(mcf, interfaceName, result);
      return false;
    }
}
