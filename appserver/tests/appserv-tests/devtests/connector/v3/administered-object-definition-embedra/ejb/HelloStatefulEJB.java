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

import javax.ejb.Stateful;
import javax.annotation.PostConstruct;
import javax.resource.AdministeredObjectDefinitions;
import javax.resource.AdministeredObjectDefinition;

import javax.naming.*;

@AdministeredObjectDefinitions(
        value = {
                @AdministeredObjectDefinition(
                        description="global-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:global/env/HelloStatefulEJB_ModByDD_AdminObject",
                        interfaceName = "javax.jms.Destination",
                        className = "connector.MyAdminObject",
                        resourceAdapter="#aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),

                @AdministeredObjectDefinition(
                        description="global-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:global/env/HelloStatefulEJB_Annotation_AdminObject",
                        interfaceName = "javax.jms.Destination",
                        className = "connector.MyAdminObject",
                        resourceAdapter="#aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),

                @AdministeredObjectDefinition(
                        description="application-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:app/env/HelloStatefulEJB_Annotation_AdminObject",
                        interfaceName = "javax.jms.Destination",
                        className = "connector.MyAdminObject",
                        resourceAdapter="#aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),

                @AdministeredObjectDefinition(
                        description="module-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:module/env/HelloStatefulEJB_Annotation_AdminObject",
                        interfaceName = "javax.jms.Destination",
                        className = "connector.MyAdminObject",
                        resourceAdapter="#aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                ),

                @AdministeredObjectDefinition(
                        description="component-scope resource defined by @AdministeredObjectDefinition",
                        name = "java:comp/env/HelloStatefulEJB_Annotation_AdminObject",
                        interfaceName = "javax.jms.Destination",
                        className = "connector.MyAdminObject",
                        resourceAdapter="#aod-ra",
                        properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
                )
        }
)
@Stateful
public class HelloStatefulEJB implements HelloStateful {

    @PostConstruct
    public void postConstruction() {
        System.out.println("In HelloStatefulEJB::postConstruction()");
    }

    public void hello() {

        // Connector-Resource-Definition through Annotation
        lookupAdminObject("java:global/env/Servlet_AdminObject", true);
        lookupAdminObject("java:app/env/Servlet_AdminObject", true);
        lookupAdminObject("java:module/env/Servlet_AdminObject", false);
        lookupAdminObject("java:comp/env/Servlet_AdminObject", false);

        lookupAdminObject("java:global/env/HelloStatefulEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:app/env/HelloStatefulEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:module/env/HelloStatefulEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloStatefulEJB_Annotation_AdminObject", true);

        lookupAdminObject("java:global/env/HelloEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:app/env/HelloEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:module/env/HelloEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloEJB_Annotation_AdminObject", false);

        // Connector-Resource-Definition through DD
        lookupAdminObject("java:global/env/EAR_AdminObject", true);
        lookupAdminObject("java:app/env/EAR_AdminObject", true);

        lookupAdminObject("java:global/env/Web_DD_AdminObject", true);
        lookupAdminObject("java:app/env/Web_DD_AdminObject", true);
        lookupAdminObject("java:module/env/Web_DD_AdminObject", false);
        lookupAdminObject("java:comp/env/Web_DD_AdminObject", false);

        lookupAdminObject("java:global/env/HelloStatefulEJB_DD_AdminObject", true);
        lookupAdminObject("java:app/env/HelloStatefulEJB_DD_AdminObject", true);
        lookupAdminObject("java:module/env/HelloStatefulEJB_DD_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloStatefulEJB_DD_AdminObject", true);

        lookupAdminObject("java:global/env/HelloEJB_DD_AdminObject", true);
        lookupAdminObject("java:app/env/HelloEJB_DD_AdminObject", true);
        lookupAdminObject("java:module/env/HelloEJB_DD_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloEJB_DD_AdminObject", false);
        
        System.out.println("StatefulEJB datasource-definitions Success");

    }

    public void sleepFor(int sec) {
        try {
            for (int i = 0; i < sec; i++) {
                Thread.currentThread().sleep(1000);
            }
        } catch (Exception ex) {
        }
    }

    public void ping() {
    }

    private void lookupAdminObject(String jndiName, boolean expectSuccess) throws RuntimeException{
        try {
            InitialContext ic = new InitialContext();
            Object ao = ic.lookup(jndiName);
            System.out.println("Stateful EJB: can access administered object : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
                throw new RuntimeException("Fail to access administered object: "+jndiName, e);
            }else{
                System.out.println("Stateful EJB: can not access administered object : " + jndiName);
            }
        }
    }

}
