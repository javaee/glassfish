/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amxtest.j2ee;

import com.sun.appserv.management.ext.wsmgmt.MessageTrace;
import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.j2ee.WebServiceEndpoint;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.io.IOException;
import java.util.Set;


/**
 */
public final class WSMsgTraceTest
        extends AMXTestBase {
    public WSMsgTraceTest()
            throws IOException {
    }

    public void
    testMessageTrace()
            throws ClassNotFoundException {
        final Set<WebServiceEndpoint> s =
                getQueryMgr().queryJ2EETypeSet(J2EETypes.WEB_SERVICE_ENDPOINT);

        for (final WebServiceEndpoint wsp : s) {
            MessageTrace[] msgs = wsp.getMessagesInHistory();
            if (msgs == null) {
                System.out.println(" No messages collected ");
                return;
            }
            System.out.println(" Collected messages  " + msgs.length);
            for (int idx = 0; idx < msgs.length; idx++) {
                final MessageTrace msg = msgs[idx];

                System.out.println(" message id  " + msg.getMessageID());
                System.out.println(" application id " + msg.getApplicationID());
                System.out.println(" endpoint name " + msg.getEndpointName());
                System.out.println(" response size " + msg.getResponseSize());
                System.out.println(" request size " + msg.getRequestSize());
                System.out.println(" transport type is " +
                        msg.getTransportType());
                System.out.println(" request headers are " +
                        msg.getHTTPRequestHeaders());
                System.out.println(" response headers are " +
                        msg.getHTTPResponseHeaders());
                System.out.println(" fault code is  " + msg.getFaultCode());
                System.out.println(" fault string is  " + msg.getFaultString());
                System.out.println(" fault actor is " + msg.getFaultActor());
                System.out.println(" client host is  " + msg.getClientHost());
                System.out.println(" principal name is " +
                        msg.getPrincipalName());
            }
        }
    }


}


