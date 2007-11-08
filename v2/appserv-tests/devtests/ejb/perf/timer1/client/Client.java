package com.sun.s1asdev.ejb.perf.timer1;

import java.io.Serializable;
import javax.jms.*;
import javax.ejb.*;
import javax.annotation.Resource;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static @EJB TimerSession timerSession;

    private static @Resource(mappedName="jms/ejb_perf_timer1_TQCF")
        QueueConnectionFactory qcFactory;

    private static @Resource(mappedName="jms/ejb_perf_timer1_TQueue") 
        Queue queue;

    public static void main(String args[]) {

        stat.addDescription("ejb-timer-sessiontimer");
        Client client = new Client(args);
        int interval = Integer.parseInt(args[0]);
        int maxTimeouts = Integer.parseInt(args[1]);
        client.doTest(interval, maxTimeouts);
        stat.printSummary("ejb-timer-sessiontimer");
    }

    public Client(String args[]) {

    }

    public String doTest(int interval, int maxTimeouts) {
        String result = kTestPassed;
        QueueConnection connection = null; 

        timerSession.createTimer(interval, maxTimeouts);

        System.out.println("Creating periodic timer with interval of " +
                           interval + " milliseconds.");
        System.out.println("Max timeouts = " + maxTimeouts);
        
        try {
            connection = qcFactory.createQueueConnection();
            QueueSession session = 
                connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueReceiver receiver = session.createReceiver(queue);
            connection.start();
            System.out.println("Waiting for message");
            Message message = receiver.receive();
            TextMessage textMsg = (TextMessage)message;
            
            if ( (message == null) || 
                 (! textMsg.getText().equals("ejbTimeout() invoked")))
                throw new Exception("Received a null message ... TimeOut failed!!");
            System.out.println("Message : " + message);
            
            
            stat.addStatus("timer1 ", stat.PASS);
        } catch(Exception e) {

            stat.addStatus("timer1 ", stat.FAIL);

        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                } catch(Exception e) {}
            }
        }

        return result;
    }
}
