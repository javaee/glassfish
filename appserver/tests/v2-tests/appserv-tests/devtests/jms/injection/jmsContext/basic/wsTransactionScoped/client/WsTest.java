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

import java.io.*;
import javax.naming.*;
import javax.jms.*;
import com.sun.ejte.ccl.reporter.*;
import test.*;
import org.glassfish.test.jms.injection.ejb.*;

/*
 * Unit test for resource injection into servlet filter.
 */
public class WsTest {

    private static final String TEST_NAME = "jms-injection-ws(TransactionScoped)";
    private static final String EXPECTED_RESPONSE = "JSP Hello World!";

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private static String transactionScope = "around TransactionScoped";

    public WsTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for resource injection into webservice "
                            + "filter"+"(TransactionScoped)");
        WsTest wsTest = new WsTest(args);
        try {
            wsTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    public void doTest() throws Exception {
        String text = "WebService Hello World!";
        NewWebService_Service service = new NewWebService_Service_Impl();
        NewWebService_PortType stub = service.getNewWebServicePort();
        if(stub.hello(text).indexOf(transactionScope) == -1)
            throw new Exception("NOT in transactionScope scope!");

        Context ctx = new InitialContext();
        MessageReceiverRemote beanRemote = (MessageReceiverRemote) ctx.lookup(MessageReceiverRemote.RemoteJNDIName);
        boolean received = beanRemote.checkMessage(text);
        if (!received)
            throw new Exception("JMS Message Not Received!");
    }
}
