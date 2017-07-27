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

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.jms.Destination;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.naming.InitialContext;

@JMSDestinationDefinitions(
        value = {
                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/HelloStatefulEJB_ModByDD_JMSDestination",
                        interfaceName = "javax.jms.Queue",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "global-scope resource defined by @JMSDestinationDefinition",
                        name = "java:global/env/HelloStatefulEJB_Annotation_JMSDestination",
                        interfaceName = "javax.jms.Queue",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalQueue"
                ),

                @JMSDestinationDefinition(
                        description = "application-scope resource defined by @JMSDestinationDefinition",
                        name = "java:app/env/HelloStatefulEJB_Annotation_JMSDestination",
                        interfaceName = "javax.jms.Topic",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "module-scope resource defined by @JMSDestinationDefinition",
                        name = "java:module/env/HelloStatefulEJB_Annotation_JMSDestination",
                        interfaceName = "javax.jms.Topic",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalTopic"
                ),

                @JMSDestinationDefinition(
                        description = "component-scope resource defined by @JMSDestinationDefinition",
                        name = "java:comp/env/HelloStatefulEJB_Annotation_JMSDestination",
                        interfaceName = "javax.jms.Queue",
                        resourceAdapter = "jmsra",
                        destinationName = "myPhysicalQueue"
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
        // JMSDestination-Definition through Annotation
        lookupJMSDestination("java:global/env/Appclient_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/Appclient_Annotation_JMSDestination", true);
        lookupJMSDestination("java:app/env/Appclient_Annotation_JMSDestination", true);
        lookupJMSDestination("java:module/env/Appclient_Annotation_JMSDestination", false);
        lookupJMSDestination("java:comp/env/Appclient_Annotation_JMSDestination", false);

        lookupJMSDestination("java:global/env/Servlet_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/Servlet_JMSDestination", true);
        lookupJMSDestination("java:app/env/Servlet_JMSDestination", true);
        lookupJMSDestination("java:module/env/Servlet_JMSDestination", false);
        lookupJMSDestination("java:comp/env/Servlet_JMSDestination", false);

        lookupJMSDestination("java:global/env/HelloStatefulEJB_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/HelloStatefulEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloStatefulEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloStatefulEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:comp/env/HelloStatefulEJB_Annotation_JMSDestination", true);

        lookupJMSDestination("java:global/env/HelloEJB_ModByDD_JMSDestination", true);
        lookupJMSDestination("java:global/env/HelloEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloEJB_Annotation_JMSDestination", true);
        lookupJMSDestination("java:comp/env/HelloEJB_Annotation_JMSDestination", false);

        // JMSDestination-Definition through DD
        lookupJMSDestination("java:global/env/Application_DD_JMSDestination", true);
        lookupJMSDestination("java:app/env/Application_DD_JMSDestination", true);

        lookupJMSDestination("java:global/env/Appclient_DD_JMSDestination", true);
        lookupJMSDestination("java:app/env/Appclient_DD_JMSDestination", true);
        lookupJMSDestination("java:module/env/Appclient_DD_JMSDestination", false);
        lookupJMSDestination("java:comp/env/Appclient_DD_JMSDestination", false);

        lookupJMSDestination("java:global/env/Web_DD_JMSDestination", true);
        lookupJMSDestination("java:app/env/Web_DD_JMSDestination", true);
        lookupJMSDestination("java:module/env/Web_DD_JMSDestination", false);
        lookupJMSDestination("java:comp/env/Web_DD_JMSDestination", false);

        lookupJMSDestination("java:global/env/HelloStatefulEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloStatefulEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloStatefulEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:comp/env/HelloStatefulEJB_DD_JMSDestination", true);

        lookupJMSDestination("java:global/env/HelloEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:app/env/HelloEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:module/env/HelloEJB_DD_JMSDestination", true);
        lookupJMSDestination("java:comp/env/HelloEJB_DD_JMSDestination", false);

        System.out.println("Stateful EJB lookup jms-destination-definitions successfully!");
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

    private void lookupJMSDestination(String jndiName, boolean expectSuccess) {
        try {
            System.out.println("Stateful EJB lookup jms destination: " + jndiName);
            InitialContext ic = new InitialContext();
            Destination dest = (Destination) ic.lookup(jndiName);
            System.out.println("Stateful EJB can access jms destination: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Stateful EJB failed to access jms destination: " + jndiName, e);
            }
            System.out.println("Stateful EJB cannot access jms destination: " + jndiName);
            return;
        }
        if (!expectSuccess) {
            throw new RuntimeException("Stateful EJB should not run into here.");
        }
    }

}
