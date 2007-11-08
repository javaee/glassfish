package com.sun.s1asdev.ejb.perf.timer1;

import javax.ejb.*;
import javax.jms.*;
import javax.annotation.*;
import java.util.Date;

@Stateless
public class TimerSessionEJB implements TimerSession
{

    private static int timeoutCount = 0;
    private static int maxTimeouts = 0;
    private static Date startTime;

    @Resource TimerService timerSvc;

    @Resource(mappedName="jms/ejb_perf_timer1_TQCF")
    private QueueConnectionFactory qcFactory;

    private QueueConnection connection;

    private QueueSession session;
    
    @Resource(mappedName="jms/ejb_perf_timer1_TQueue")
    private Queue queue;

    private QueueSender sender;


    // business method to create a timer
    public void createTimer(int ms, int maxDeliveries) {

        timerSvc.createTimer(1, ms, "created timer");
        maxTimeouts = maxDeliveries;
        timeoutCount = 0;
        startTime = new Date();

    }

    @Timeout
    public void ejbTimeout(Timer timer) {

        timeoutCount++;
        
        if( timeoutCount <= maxTimeouts ) {
            return;
        }

        try {

            timer.cancel();
            Date endTime = new Date();
            long elapsed = endTime.getTime() - startTime.getTime();

            System.out.println("It took " + elapsed + " milliseconds " +
                               "for " + maxTimeouts + " timeouts");

            connection = qcFactory.createQueueConnection();

            QueueSession session = 
                connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            sender  = session.createSender(queue);

            TextMessage message = session.createTextMessage();
            message.setText("ejbTimeout() invoked");
            System.out.println("Sending time out message");
            sender.send(message);
            System.out.println("Time out message sent");

        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
	}

    }

}
