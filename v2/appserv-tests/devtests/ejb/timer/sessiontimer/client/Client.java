package com.sun.s1asdev.ejb.timer.sessiontimer.client;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.timer.sessiontimer.TimerSession;
import com.sun.s1asdev.ejb.timer.sessiontimer.TimerSessionHome;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    String jndiName = "ejb/ejb_timer_sessiontimer_TimerSession";
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
            jndiName = args[0];
        }
    }

    public String doTest() {
        String result = kTestPassed;
        QueueConnection connection = null; 
        TimerSession remoteObj = null;

        String ejbName = "ejbs/Timer";

        try {
            Context ic = new InitialContext();
            
            System.out.println("Looking up ejb ref " + ejbName);
            System.out.println("Doing timer test for " + jndiName);
//PG->            Object objref = ic.lookup(jndiName);


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
            TimerHandle handle = remoteObj.createTimer(5000);
            
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
            System.out.println("Waiting for message");
            Message message = receiver.receive(45000);
            TextMessage textMsg = (TextMessage)message;

	    	if ( (message == null) || 
                 (! textMsg.getText().equals("ejbTimeout() invoked")))
				throw new Exception("Received a null message ... TimeOut failed!!");
	    	System.out.println("Message : " + message);
            
            System.out.println("TimerSession : jndi lookup for -> " + 
                jndiName + " <- test passed!!");
            
            stat.addStatus("sessiontimer " + jndiName, stat.PASS);
        } catch(Exception e) {
            System.out.println("TimerSession : " + jndiName + " test failed");
            e.printStackTrace();
            result = kTestFailed;
            stat.addStatus("sessiontimer " + jndiName, stat.FAIL);
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
