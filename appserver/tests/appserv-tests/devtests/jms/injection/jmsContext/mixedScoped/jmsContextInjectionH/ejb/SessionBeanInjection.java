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

package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import java.lang.SecurityException;
import java.lang.IllegalStateException;


/**
 *
 * @author JIGWANG
 */
@Stateless(mappedName="SessionBeanInjection/remote")
@TransactionManagement(TransactionManagementType.BEAN)
public class SessionBeanInjection implements SessionBeanInjectionRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Inject 
    UserTransaction ut;
    
    private static String requestScope = "around RequestScoped";
    private static String transactionScope = "around TransactionScoped";
    private static String preIdentical = "fingerPrint";

    public Boolean sendMessage(String text) {

        String context1 = "";
        String context2 = "";
        String context3 = "";
        String context4 = "";
        String context5 = "";
        try {
            TextMessage msg = jmsContext.createTextMessage(text);
            
            jmsContext.createProducer().send(queue, msg);
            context1 = jmsContext.toString();
            System.out.println("JMSContext1:"+context1);
            
            jmsContext.createProducer().send(queue, msg);
            context2 = jmsContext.toString();
            System.out.println("JMSContext2:"+context2);

            ut.begin();
            jmsContext.createProducer().send(queue, msg);
            context3 = jmsContext.toString();
            System.out.println("JMSContext3:"+context3);
            ut.commit();
            
            jmsContext.createProducer().send(queue, msg);
            context4 = jmsContext.toString();
            System.out.println("JMSContext4:"+context4);

            jmsContext.createProducer().send(queue, msg);
            context5 = jmsContext.toString();
            System.out.println("JMSContext5:"+context5);

            return checkScope(context1, context2, context3, context4, context5);
        } catch (Exception e) {
            e.printStackTrace();
            try{
                ut.rollback();
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
        return false;
    }

    public Boolean checkScope(String context1, String context2, String context3, String context4, String context5){

        if (context1.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the first call are in request scope.");
        }else{
            System.out.println("The context variables used in the first call are NOT in request scope.");
            return false;
        }
            
        if (context2.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the second call are in request scope.");
        }else{
            System.out.println("The context variables used in the second call are NOT in request scope.");
            return false;
        }
        
        if (context3.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the third call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the third call are NOT in transaction scope.");
            return false;
        }
        
        if (context4.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the fourth call are in request scope.");
        }else{
            System.out.println("The context variables used in the fourth call are NOT in request scope.");
            return false;
        }
            
        if (context5.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the fifth call are in request scope.");
        }else{
            System.out.println("The context variables used in the fifth call are NOT in request scope.");
            return false;
        }
        
        String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(requestScope));
        String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(requestScope));
        String context3Annotation = context3.substring(context3.indexOf(preIdentical),context3.indexOf(transactionScope));
        String context4Annotation = context4.substring(context4.indexOf(preIdentical),context4.indexOf(requestScope));
        String context5Annotation = context5.substring(context5.indexOf(preIdentical),context5.indexOf(requestScope));
        
        if(context1Annotation.equals(context2Annotation)){
            System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
            if(context1Annotation.equals(context3Annotation)){
                System.out.println("The context variables in the first,second and third calls to context.send() injected are using identical annotations.");
                if(context1Annotation.equals(context4Annotation)){
                    System.out.println("The context variables in the first,second,third and fourth calls to context.send() injected are using identical annotations.");
                    if(context1Annotation.equals(context5Annotation)){
                        System.out.println("The context variables in the first,second,third,fourth and fifth calls to context.send() injected are using identical annotations.");
                    }else{
                        System.out.println("The context variables in the first and fifth calls to context.send() injected are not using identical annotations.");
                        return false;
                    }
                }else{
                    System.out.println("The context variables in the first and fourth calls to context.send() injected are not using identical annotations.");
                    return false;
                }
            }else{
                System.out.println("The context variables in the first and third calls to context.send() injected are not using identical annotations.");
                return false;
            }
        }else{
            System.out.println("The context variables in the first and second calls to context.send() injected are not using identical annotations.");
            return false;
        }

        if (context1.substring(context1.indexOf(requestScope)).equals(context2.substring(context2.indexOf(requestScope)))){
            System.out.println("The context variables used in the first and second calls to context.send() take place in the same request.");
        }else{
            System.out.println("The context variables used in the first and second calls to context.send() take place in the different request.");
            return false;
        }
        
        if (context4.substring(context4.indexOf(requestScope)).equals(context5.substring(context5.indexOf(requestScope)))){
            System.out.println("The context variables used in the fourth and fifth calls to context.send() take place in the same request.");
        }else{
            System.out.println("The context variables used in the fourth and fifth calls to context.send() take place in the different request.");
            return false;
        }

        if (context1.substring(context1.indexOf(requestScope)).equals(context4.substring(context4.indexOf(requestScope)))){
            System.out.println("The context variables used in the first,second,fourth and fifth calls to context.send() take place in the same request.");
        }else{
            System.out.println("The context variables used in the first and fourth calls to context.send() take place in the different request.");
            return false;
        }

        return true;
    }
}
