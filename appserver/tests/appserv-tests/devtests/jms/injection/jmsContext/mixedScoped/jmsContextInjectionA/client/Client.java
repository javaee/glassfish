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

package org.glassfish.test.jms.injection.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.injection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");
    private static String transactionScope = "around TransactionScoped";
    private static String preIdentical = "fingerPrint";

    public static void main (String[] args) {
        STAT.addDescription("Use case A: Two methods on the same bean within separate transactions.");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary();
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "jms-injection-ejb-jmsContextInjectionA";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            SessionBeanInjectionRemote beanRemote = (SessionBeanInjectionRemote) ctx.lookup(SessionBeanInjectionRemote.RemoteJNDIName);
            String context1 = beanRemote.sendMessage1(text);
            String context2 = beanRemote.sendMessage2(text);
            System.out.println("context1:"+context1);
            System.out.println("context1:"+context2);
            
            if (context1.indexOf(transactionScope) != -1){
                System.out.println("The context variables used in the first call are in transaction scope.");
            }else{
                System.out.println("TThe context variables used in the first call are NOT in transaction scope.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }
            
            if (context2.indexOf(transactionScope) != -1){
                 System.out.println("The context variables used in the second call are in transaction scope.");
            }else{
                System.out.println("The context variables used in the second call are NOT in transaction scope.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }
            
            String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(transactionScope));
            String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(transactionScope));
            
            if(context1Annotation.equals(context2Annotation)){
                System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
            }else{
                System.out.println("The context variables in the first and second calls to context.send() injected are not using identical annotations.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }
            
            if (context1.substring(context1.indexOf(transactionScope)).equals(context2.substring(context2.indexOf(transactionScope)))) {
                System.out.println("The context variables used in the first and second calls to context.send() take place in the same transaction.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }else{
                System.out.println("The context variables used in the first and second calls to context.send() take place in the different transaction.");
            }
            STAT.addStatus(ejbName, STAT.PASS);
            

        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus(ejbName, STAT.FAIL);
        }
    }
    
}
