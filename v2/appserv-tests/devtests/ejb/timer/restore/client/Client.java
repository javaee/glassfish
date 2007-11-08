package com.sun.s1asdev.ejb.timer.restore.client;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;
import java.util.HashSet;
import java.util.Set;
import com.sun.s1asdev.ejb.timer.restore.TimerSession;
import com.sun.s1asdev.ejb.timer.restore.TimerSessionHome;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    String jndiName = "ejb/ejb_timer_sessiontimer_TimerSession";
    boolean afterRestart = false;


    public static void main(String args[]) {

        stat.addDescription("ejb-timer-sessiontimer");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-timer-sessiontimer");
    }

    public Client(String args[]) {
        for (int i=0; i<args.length; i++) {
            System.err.println("ARGS: " + args[i]);
        }

        if( args.length == 1) {
            afterRestart = args[0].equalsIgnoreCase("restore");
        } 
    }

    public String doTest() {
        String result = kTestPassed;
        QueueConnection connection = null; 
        TimerSession remoteObj = null;

        String testName = jndiName;
        String ejbName = "ejbs/Timer";

        try {
            Context ic = new InitialContext();
            
            System.out.println("Looking up ejb ref " + ejbName);
            System.out.println("Doing timer test for " + jndiName);


            java.lang.Object objref = ic.lookup("java:comp/env/" + ejbName);

            System.out.println("---ejb stub---" + 
                objref.getClass().getClassLoader());
            System.out.println("---ejb classname---" + 
                objref.getClass().getName());
            System.out.println("---TimerSessionHome---" + 
                TimerSessionHome.class.getClassLoader());
            System.err.println("Looked up home!!");



            TimerSessionHome home = (TimerSessionHome)
                PortableRemoteObject.narrow(objref, TimerSessionHome.class);
            
            remoteObj = home.create();
            String otherServer1 = "foo";

            if( afterRestart ) {

                testName = "afterRestart";
                System.out.println("Migrating timers from " + otherServer1);
                remoteObj.migrateTimersFrom(otherServer1);
            
            } else {
                remoteObj.createTimer(5000, 0, "timer1");
                remoteObj.createTimer(30000, 0, "timer2");
                remoteObj.createTimer(60000000, 0, "timer3");
                remoteObj.createTimer(5000, 12000, "timer4");
                remoteObj.createTimer(5000, 30000, "timer5");
                remoteObj.createTimer(5000, 100000000, "timer6");           
                
                // simulate creation of timers within another server
                // instance.  none of these should timeout until we
                // do a migration
                

                remoteObj.createTimerInOtherServer
                    (otherServer1, "other_timer7",
                     1000, 0, 
                     "other_timer7");

                remoteObj.createTimerInOtherServer
                    (otherServer1, "other_timer8",
                     10000000000L, 0, 
                     "other_timer8"); 
                
                remoteObj.createTimerInOtherServer
                    (otherServer1, "other_timer9",
                     1000, 10000000000L, 
                     "other_timer9");


                remoteObj.createTimerInOtherServer
                    (otherServer1, "other_timer10",
                     100000000, 1000, 
                     "other_timer10");

                remoteObj.createTimerInOtherServer
                    (otherServer1, "other_timer11",
                     30000, 100000000, 
                     "other_timer11");

                // create a couple of timers for another simulated
                // server.  we will not be migrating these timers, so
                // by verifying that we don't get notifications from these
                // timers we test that the migration only happens for
                // the specified server.  

                remoteObj.createTimerInOtherServer
                    ("bar", "bar_timer12", 1000, 0, "bar_timer12");
                remoteObj.createTimerInOtherServer
                    ("bar", "bar_timer13", 1000, 1000, "bar_timer13");
                
            }
            
            QueueConnectionFactory qcFactory = (QueueConnectionFactory)
                      ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            System.out.println (" qcFactory = " + qcFactory);

            Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
            System.out.println (" queue = " + queue);

            connection = qcFactory.createQueueConnection();
            QueueSession session = 
                connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueReceiver receiver = session.createReceiver(queue);
            connection.start();

            Set toReceive = new HashSet();
            Set received = new HashSet();
            if( afterRestart ) {
                toReceive.add("timer2");
                toReceive.add("timer4");
                toReceive.add("timer5");           

                // migrated timers
                toReceive.add("other_timer7");
                toReceive.add("other_timer9");
                toReceive.add("other_timer11");

            } else {
                toReceive.add("timer1");
                toReceive.add("timer4");
                toReceive.add("timer5");
                toReceive.add("timer6");
            }

            long waitTime = afterRestart ? 
                40000 : 10000;
                
            while(true) {
                System.out.println("Waiting for message");
                Message message = receiver.receive(waitTime);
                TextMessage textMsg = (TextMessage)message;
                
                if ( (message == null) || 
                     (! toReceive.contains(textMsg.getText())) ) {
                    throw new Exception("Received a invalid message " + 
                                        message + " TimeOut failed!!");
                } 

                received.add(textMsg.getText());

                System.out.println("Received Message : " + 
                                   textMsg.getText());
                System.out.println("Messages received so far : " + 
                                   received);                
                if( received.equals(toReceive) ) {
                    System.out.println("Got all expected messages");
                    break;
                }
            }
            
            stat.addStatus("sessiontimer " + testName, stat.PASS);

            if( afterRestart ) {
                remoteObj.deleteTimers();
            } 
        } catch(Exception e) {
            System.out.println("TimerSession : " + testName + " test failed");
            e.printStackTrace();
            result = kTestFailed;
            stat.addStatus("sessiontimer " + testName, stat.FAIL);
        }
        finally {
            try {
                if(connection != null)
                    connection.close();
                ((EJBObject)remoteObj).remove();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
