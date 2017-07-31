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

package com.sun.s1asdev.ejb.timer.timertests.client;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
//import javax.jms.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb.timer.timertests.Bar;
import com.sun.s1asdev.ejb.timer.timertests.BarHome;
import com.sun.s1asdev.ejb.timer.timertests.BarPrimaryKey;
import com.sun.s1asdev.ejb.timer.timertests.FooHome;
import com.sun.s1asdev.ejb.timer.timertests.Foo;
import com.sun.s1asdev.ejb.timer.timertests.TimerStuff;
import com.sun.s1asdev.ejb.timer.timertests.StatefulHome;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private InitialContext context;

    /**
    private TopicConnection topicCon;
    private TopicSession topicSession;
    private TopicPublisher topicPublisher;
    private TopicSubscriber topicSubscriber;

    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    */

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) { 
        boolean doJms = false; // TODO (args.length == 1) && (args[0].equalsIgnoreCase("jms"));

        stat.addDescription("ejb-timer-timertests");


        new Client().doFooTest("ejbs/Foo_CMT", doJms);

        new Client().doFooTest("ejbs/Foo_UNSPECIFIED_TX", doJms);
        TimerStuff foo = new Client().doFooTest("ejbs/Foo_BMT", doJms);


	/** TODO
        new Client().doMessageDrivenTest("jms/TimerMDBQueue_CMT", foo, doJms);
        new Client().doMessageDrivenTest("jms/TimerMDBQueue_BMT", foo, doJms);
	**/

	new Client().doStatefulTest("ejbs/Stateful_CMT", foo,doJms);
	new Client().doStatefulTest("ejbs/Stateful_BMT", foo, doJms);

        new Client().doBarTest("ejbtimer/Bar_CMT", doJms);

        try {
	     ((Foo) foo).remove();
        } catch(Exception e) {
            e.printStackTrace();
        }

        stat.printSummary("ejb-timer-timertests");
    }

    // when running this class through the appclient infrastructure
    public Client() {
        try {
            context = new InitialContext();
        } catch(Exception e) {
            System.out.println("Client : new InitialContext() failed");
            e.printStackTrace();
            stat.addStatus("Client() ", stat.FAIL);
        }
    }

    public void doMessageDrivenTest(String jndiName, TimerStuff foo,
                                    boolean jms) {
	/**
        if( jms ) { return; }

        try {
System.out.println("********PG-> in doMessageDrivenTest() for jndiName = " + jndiName );
           
            setup();
System.out.println("********PG-> in doMessageDrivenTest() after setup");
            Context ic = new InitialContext();
	     Queue messageDrivenDest = (Queue) ic.lookup("java:comp/env/" + jndiName);

            System.out.println("Doing message driven tests for" + jndiName);

            String testName;
            int numTests = 6;
            TimerHandle ths[] = new TimerHandle[numTests];
            for(int i = 1; i < numTests; i++) {
                testName = "test" + i;
                System.out.println("Doing " + testName);
                ths[i] = foo.createTimer(1000000, testName);
                ObjectMessage objMsg = queueSession.createObjectMessage(ths[i]);
                sendMsgs(messageDrivenDest, objMsg, 1);
            }

            long sleepTime = 30000;
            System.out.println("Sleeping for " + sleepTime / 1000 + " seconds");
            Thread.sleep(sleepTime);
            
            // at this point, all foo timers should have been cancelled
            // by the message bean.
            foo.assertNoTimers();

            System.out.println("Message-driven test " + jndiName + " passed!!");
            stat.addStatus("Message-driven test: " + jndiName, stat.PASS);
        } catch(Exception e) {
            System.out.println("Message-driven test " + jndiName + " failed!!");
            e.printStackTrace();
            stat.addStatus("Message-driven test: " + jndiName, stat.FAIL);
        } finally {
            cleanup();
        }
	**/
    }

    public void doBarTest(String jndiName, boolean jms) {
        try {
	    Object barObjref = context.lookup(jndiName);

            System.out.println("doBarTest(): Doing bar timer test for " + jndiName);

            BarHome  barHome = (BarHome)PortableRemoteObject.narrow
                (barObjref, BarHome.class);

            System.out.println("doBarTest(): Bar lookup succeeded: " + jndiName );

            Long id = new Long(1);
            String value2 = id.toString();
            TimerStuff bar;
            try {
                try {
                    System.out.println("Calling findbyprimarykey");
                    barHome.findByPrimaryKey(new BarPrimaryKey(id, value2));
                } catch(FinderException fe) {
                    System.out.println("caught expected finder ex for " + id +
                                       " " + fe);
                }
                bar = barHome.create(id, value2);
            System.out.println("doBarTest(): Bar after create " );
            } catch(CreateException ce) {
                bar = barHome.findByPrimaryKey(new BarPrimaryKey(id, value2));
            System.out.println("doBarTest(): Bar after find " );
            }
            if (jms) {
                doJmsTest(bar);    
            } else {
                doTest(bar);
                ((EJBObject)bar).remove();
                doEntityTests(barHome);
            }

            System.out.println("doBarTest(): Bar : " + jndiName + " test passed!!");
            stat.addStatus("Bar: " + jndiName, stat.PASS);
        } catch(Exception e) {
            System.out.println("Bar : " + jndiName + " test failed");
            e.printStackTrace();
            stat.addStatus("Bar: " + jndiName, stat.FAIL);
        }
    }

    public void setup() throws Exception {
	/**
//PG->        context = new InitialContext();
        
        TopicConnectionFactory topicConFactory = 
            (TopicConnectionFactory) context.lookup
                ("java:comp/env/jms/MyTopicConnectionFactory");
                
System.out.println("********PG-> setup(): after  lookup");
        topicCon = topicConFactory.createTopicConnection();

System.out.println("********PG-> setup(): after  createTopicConnection");
        topicSession = 
            topicCon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
System.out.println("********PG-> setup(): after  createTopicSession");
            
        // Producer will be specified when actual msg is published.
        topicPublisher = topicSession.createPublisher(null);
System.out.println("********PG-> setup(): after createPublisher");

        topicCon.start();
        System.out.println("********PG-> setup(): after start");

        QueueConnectionFactory queueConFactory = 
            (QueueConnectionFactory) context.lookup
            ("java:comp/env/jms/MyQueueConnectionFactory");

        queueCon = queueConFactory.createQueueConnection();

        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE); 

        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(null);        

        queueCon.start();
	**/

    }

    public void cleanup() {
	/**
        try {
            if( topicCon != null ) {
                topicCon.close();
            }
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
        **/
    }
    /**
    public void publishMsgs(Topic topic, Message msg, int num) 
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Publishing message " + i + " to " + topic);
            topicPublisher.publish(topic, msg);
        }
    }

    public void sendMsgs(Queue queue, Message msg, int num) 
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Publishing message " + i + " to " + queue);
            queueSender.send(queue, msg);
        }
    }
    */

    public void doStatefulTest(String jndiName, TimerStuff foo, boolean jms) {
        if( jms ) return;

        try {
            Context ic = new InitialContext();
            Object sfObjref = ic.lookup("java:comp/env/" + jndiName);

            System.out.println("Doing stateful timer test for " + jndiName);

            StatefulHome  sfHome = (StatefulHome)PortableRemoteObject.narrow
                (sfObjref, StatefulHome.class);
            TimerHandle th = foo.createTimer(10000000, "test1");
            TimerStuff sf = sfHome.create(th);
            Serializable info = sf.getInfo(th);
            sf.getNextTimeoutTest2(5, th);
            sf.getTimeRemainingTest2(5, th);
            sf.cancelTimerAndRollback(th);
            sf.cancelTimer(th);
            ((EJBObject)sf).remove();

            System.out.println("Stateful : " + jndiName + " test passed!!");
            stat.addStatus("Stateful: " + jndiName, stat.PASS);
        } catch(Exception e) {
            System.out.println("Stateful : " + jndiName + " test failed");
            e.printStackTrace();
            stat.addStatus("Stateful: " + jndiName, stat.FAIL);
        }
    }

    public TimerStuff doFooTest(String jndiName, boolean jms) {
        TimerStuff foo = null;
        try {
            Context ic = new InitialContext();
            Object fooObjref = ic.lookup("java:comp/env/" + jndiName);

            System.out.println("Doing foo timer test for " + jndiName);
            FooHome  fooHome = (FooHome)PortableRemoteObject.narrow
                (fooObjref, FooHome.class);

            foo = fooHome.create();
            if( jms ) {
                doJmsTest(foo);
            } else {
                doTest(foo);
            }

            System.out.println("Foo : " + jndiName + " test passed!!");
            stat.addStatus("Foo: " + jndiName, stat.PASS);
        } catch(Exception e) {
            System.out.println("Foo : " + jndiName + " test failed");
            e.printStackTrace();
            stat.addStatus("Foo: " + jndiName, stat.FAIL);
        }
        return foo;
    }

    private void doJmsTest(TimerStuff timerStuff) throws Exception {
        timerStuff.sendMessageAndCreateTimer();
        timerStuff.recvMessageAndCreateTimer(true);
        timerStuff.sendMessageAndCreateTimerAndRollback();
        timerStuff.recvMessageAndCreateTimerAndRollback(false);
    }

    public void doTest(TimerStuff timerStuff) throws Exception {

        System.out.println("doTest(): creating the runtimeExTimer handle");
        TimerHandle runtimeExTimer =
            timerStuff.createTimer(1, "RuntimeException");

        System.out.println("doTest(): creating the timerhandle");
        TimerHandle timerHandle = timerStuff.createTimer(1, 1);
        
        //
        System.out.println("doTest(): creating the timerhandle2");
        TimerHandle timerHandle2 = timerStuff.createTimer(10000, 10000);
        
        //
        System.out.println("doTest(): creating the timerhandle3");
        TimerHandle timerHandle3 = timerStuff.createTimer(new Date());
        
        //
        System.out.println("doTest(): creating the timerhandle4");
        TimerHandle timerHandle4 = timerStuff.createTimer(new Date(new Date().getTime() + 2000));
        
        //
        System.out.println("doTest(): creating the timerhandle5");
        TimerHandle timerHandle5 = timerStuff.createTimer(new Date(new Date().getTime() + 20000), 10000);

        System.out.println("doTest(): creating the createTimerAndRollback");
        timerStuff.createTimerAndRollback(20000);
        
        //
        System.out.println("doTest(): creating the createTimerAndCancel");
        timerStuff.createTimerAndCancel(20000);
        
        // @@@ reevaluate double cancel logic
        //timerStuff.createTimerAndCancelAndCancel(20000);
        
        //
        System.out.println("doTest(): creating the createTimerAndCancelAndRollback");
        timerStuff.createTimerAndCancelAndRollback(20000);
        
        //
        System.out.println("doTest(): creating the cancelTimer(timerhandle2)");
        timerStuff.cancelTimer(timerHandle2);
        System.out.println("doTest(): assertTimerNotactive(timerhandle2)");
        timerStuff.assertTimerNotActive(timerHandle2);

        //
        timerStuff.cancelTimerAndRollback(timerHandle5);
        // @@@ reevaluate double cancel logic
        //timerStuff.cancelTimerAndCancelAndRollback(timerHandle6);
        
        TimerHandle timerHandle7 = 
            timerStuff.createTimer(1, 1, "cancelTimer");
        TimerHandle timerHandle8 = 
            timerStuff.createTimer(1, 1, "cancelTimerAndRollback");
        TimerHandle timerHandle9 =         
            timerStuff.createTimer(1, "cancelTimerAndRollback");

        TimerHandle timerHandle11 = timerStuff.getTimeRemainingTest1(20);
        timerStuff.getTimeRemainingTest2(20, timerHandle11);
        timerStuff.getTimeRemainingTest2(20, timerHandle);
        
        TimerHandle timerHandle12 = timerStuff.getNextTimeoutTest1(20);
        timerStuff.getNextTimeoutTest2(20, timerHandle12);
        timerStuff.getNextTimeoutTest2(20, timerHandle);

        System.out.println("cancelling timerHandle");
        timerStuff.cancelTimer(timerHandle);

        System.out.println("cancelling timerHandle5");
        timerStuff.cancelTimer(timerHandle5);

        System.out.println("cancelling timerHandle11");
        timerStuff.cancelTimer(timerHandle11);

        System.out.println("cancelling timerHandle12");
        timerStuff.cancelTimer(timerHandle12);

        // It's possible that the following timers haven't expired yet
        try {
            timerStuff.cancelTimerNoError(timerHandle8);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timerHandle3);
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            timerStuff.cancelTimerNoError(timerHandle4);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timerHandle7);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(runtimeExTimer);
        } catch(Exception e) {
            e.printStackTrace();
        }

        timerStuff.assertNoTimers();

    }

    public void doEntityTests(BarHome barHome) 
        throws Exception {
        TimerStuff bar1 = barHome.create(new Long(1), "1");
        TimerHandle timerHandle1 = bar1.createTimer(100000000);

        ((EJBObject)bar1).remove();

        barHome.newTimerAndRemoveBean(new Long(2), "2");
        barHome.newTimerAndRemoveBeanAndRollback(new Long(3), "3");

        Bar bar4 = barHome.create(new Long(4), "4");
        TimerHandle timerHandle4 = bar4.createTimer(1, 1);
        barHome.nixBeanAndRollback(bar4);
        // Since bean and timer still exist after rollback, getInfo
        // should work.
        Serializable info = bar4.getInfo(timerHandle4);
        System.out.println("after rollback, bar4 info = " + info);

        Long bar5Id = new Long(5);
        TimerStuff bar5 = barHome.create(bar5Id, "5");
        TimerHandle timerHandle5 = bar5.createTimer(10000000);
        info = bar5.getInfo(timerHandle5);
        System.out.println("Info = " + info);
        barHome.remove(((EJBObject)bar5).getPrimaryKey());
        // At this point timer should not exist since we removed its 
        // timed object
        try {
            info = bar5.getInfoNoError(timerHandle5);
        } catch(Exception e) {
            System.out.println("Caught expected exception for bar5");
        }

        // Create entity with same pkey as before.
        bar5 = barHome.create(bar5Id, "5");

        // Timer should still *not* be accessible.
        try {
            info = bar5.getInfoNoError(timerHandle5);
        } catch(Exception e) {
        }
        barHome.remove(new BarPrimaryKey(bar5Id, "5"));

        bar4.cancelTimer(timerHandle4);
        bar4.assertNoTimers();
        ((EJBObject) bar4).remove();
    }

}
