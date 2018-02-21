/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.mail.MailSessionDefinition;
import javax.mail.MailSessionDefinitions;
import javax.ejb.Stateful;
import javax.naming.InitialContext;

@MailSessionDefinitions(
        value = {
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:global/mail/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),

                @MailSessionDefinition(description = "Mail Session Description 3",
                        name = "java:app/mail/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 4",
                        name = "java:module/mail/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
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
        boolean global = lookupMailSession("java:global/mail/HelloStatefulEJB_MailSession", true);
        boolean appHelloStatefulEjb = lookupMailSession("java:app/mail/HelloStatefulEJB_MailSession", true);

        boolean globalHelloEJB = lookupMailSession("java:global/mail/HelloEJB_MailSession", true);
        boolean moduleHelloEjb = lookupMailSession("java:module/mail/HelloEJB_MailSession", true);

        boolean globalServlet = lookupMailSession("java:global/env/Servlet_MailSession", true);
        boolean appServlet = lookupMailSession("java:app/env/Servlet_MailSession", true);
        boolean moduleServlet = lookupMailSession("java:module/mail/Servlet_MailSession", false);

        boolean globalServlet_MS_MailSession = lookupMailSession("java:global/mail/Servlet_MS_MailSession", true);

        boolean globalHelloStateful_MS_MailSession = lookupMailSession("java:global/mail/HelloStatefulEJB_MS_MailSession", true);

        boolean globalHello_MS_MailSession = lookupMailSession("java:global/mail/HelloEJB_MS_MailSession", true);

        boolean globalAppLevel_MS_MailSession = lookupMailSession("java:global/mail/Application_Level_MailSession", true);
        boolean appAppLevel_MS_MailSession = lookupMailSession("java:app/mail/Application_Level_MailSession", true);

        if (global && globalHelloEJB && globalServlet && appServlet &&
                globalServlet_MS_MailSession && globalHelloStateful_MS_MailSession
                && globalHello_MS_MailSession && appHelloStatefulEjb && moduleHelloEjb && !moduleServlet
                && globalAppLevel_MS_MailSession && appAppLevel_MS_MailSession) {
            System.out.println("StatefulEJB mail-session Success");

        } else {
            System.out.println("StatefulEJB mail-session Failure");
            throw new RuntimeException("StatefulEJB mail-session Failure");
        }
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

    private boolean lookupMailSession(String mailSessionName, boolean expectSuccess) {
        try {
            InitialContext ic = new InitialContext();
            Object ds = ic.lookup(mailSessionName);
            return true;
        } catch (Exception e) {
            if (expectSuccess) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
