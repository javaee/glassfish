/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateless;

import javax.naming.InitialContext;
import javax.resource.ConnectionFactoryDefinitions;
import javax.resource.ConnectionFactoryDefinition;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.annotation.Resource;

@ConnectionFactoryDefinitions(
     value = {
          @ConnectionFactoryDefinition(
                description="global-scope resource defined by @ConnectionFactoryDefinition",
                name = "java:global/env/HelloEJB_ModByDD_ConnectionFactory",
                interfaceName = "javax.resource.cci.ConnectionFactory",
                resourceAdapter = "cfd-ra",
                properties = {"testName=foo"}
          ),
          @ConnectionFactoryDefinition(
               description = "global-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:global/env/HelloEJB_Annotation_ConnectionFactory", 
               interfaceName = "javax.resource.cci.ConnectionFactory", 
               resourceAdapter = "cfd-ra",
               transactionSupport = TransactionSupportLevel.LocalTransaction,
               maxPoolSize = 16,
               minPoolSize = 4,
               properties = {"testName=foo"}
          ),
          
          @ConnectionFactoryDefinition(
               description = "application-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:app/env/HelloEJB_Annotation_ConnectionFactory", 
               interfaceName = "javax.resource.cci.ConnectionFactory", 
               transactionSupport = TransactionSupportLevel.XATransaction,
               maxPoolSize = 16,
               minPoolSize = 4,
               resourceAdapter = "cfd-ra",
               properties = {"testName=foo"}
          ),
          
          @ConnectionFactoryDefinition(
               description = "module-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:module/env/HelloEJB_Annotation_ConnectionFactory", 
               interfaceName = "javax.resource.cci.ConnectionFactory", 
               resourceAdapter = "cfd-ra",
               properties = {"testName=foo"}
          ),
          
          @ConnectionFactoryDefinition(
               description = "component-scope resource defined by @ConnectionFactoryDefinition", 
               name = "java:comp/env/HelloEJB_Annotation_ConnectionFactory", 
               interfaceName = "javax.resource.cci.ConnectionFactory", 
               resourceAdapter = "cfd-ra",
               properties = {"testName=foo"}
          )

     }
)
@Stateless
public class HelloEJB implements Hello {

    @javax.annotation.Resource(name="java:comp/env/HelloEJB_Annotation_ConnectionFactory")
    ConnectionFactory cf;
    
    public void hello() {
        try {
            Connection c = cf.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to access connector resource through injection", e);
        }

        // Connection-Factory-Definition through Annotation
        lookupConnectionFactory("java:global/env/Servlet_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/Servlet_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/Servlet_ConnectionFactory", false);
        lookupConnectionFactory("java:comp/env/Servlet_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloStatefulEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloStatefulEJB_Annotation_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloEJB_Annotation_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloEJB_Annotation_ConnectionFactory", true);

        // Connection-Factory-Definition through DD
        lookupConnectionFactory("java:global/env/EAR_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/EAR_ConnectionFactory", true);

        lookupConnectionFactory("java:global/env/Web_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/Web_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/Web_DD_ConnectionFactory", false);
        lookupConnectionFactory("java:comp/env/Web_DD_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloStatefulEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloStatefulEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloStatefulEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloStatefulEJB_DD_ConnectionFactory", false);

        lookupConnectionFactory("java:global/env/HelloEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:app/env/HelloEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:module/env/HelloEJB_DD_ConnectionFactory", true);
        lookupConnectionFactory("java:comp/env/HelloEJB_DD_ConnectionFactory", true);
        
        System.out.println("In HelloEJB::hello()");
    }

    private void lookupConnectionFactory(String jndiName, boolean expectSuccess) throws RuntimeException{
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory ds = (ConnectionFactory) ic.lookup(jndiName);
            c = ds.getConnection();
            System.out.println("Stateless EJB: can access connector resource : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
                throw new RuntimeException("Fail to access connector resource: "+jndiName, e);
            }else{
                System.out.println("Stateless EJB cannot access connector resource : " + jndiName);
            }
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            }
        }
    }
    

}
