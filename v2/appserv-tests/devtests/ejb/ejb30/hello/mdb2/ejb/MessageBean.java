package com.sun.s1asdev.ejb.ejb30.hello.mdb2;

import javax.ejb.MessageDriven;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.MessageDrivenContext;

import javax.naming.InitialContext;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.jms.Session;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.annotation.Resource;

import java.util.Collection;

@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@MessageDriven(messageListenerInterface=MessageListener.class) 
public class MessageBean implements javax.ejb.MessageDrivenBean {

    @EJB(name="hello1") private Hello1 hello1;
    @EJB private Hello2 hello2;
    
    boolean onMessageInterceptorCalled = false;
    boolean timeoutInterceptorCalled = false;

    @Resource javax.ejb.MessageDrivenContext ctx;
    @Resource javax.ejb.TimerService injectedTimerService;

    @Resource(name="jms/MyQueueConnectionFactory") 
        QueueConnectionFactory qcFactory;

    // Values for these will be specified in the standard deployment descriptor
    // and the values will be different than the defaults.
    @Resource String stringValue1 = "undefined";
    @Resource(name="intValue1") int intValue1 = 1;
    @Resource(name="integerValue1") Integer integerValue1 = new Integer(1);
    short sv1 = 1;

    // Value specified in deployment descriptor so this should be called.
    @Resource void setShortValue1(short s) {
        sv1 = s;
    }


    // Values for these will not be specified in the standard deployment
    // descriptor so their defaults should apply.
    @Resource String stringValue2 = "undefined";
    @Resource int intValue2 = 1;
    @Resource Integer integerValue2 = new Integer(1);

    // corresponding env-entry is specified in ejb-jar.xml, 
    // but there is no value so no injection should happen and
    // the default value should be used.
    int intValue3 = 3;

    // No value specified in deployment descriptor so this should never be
    // called.
    @Resource(name="shortValue2") void setShortValue2(short s) {
        throw new IllegalStateException("setShortValue1 param = " + s +
                                        " shouldn't be called");
    }

    @Resource
    private void setMDC(javax.ejb.MessageDrivenContext context) {

        try {
            context.getTimerService();
            throw new RuntimeException("Should have gotten IllegalStateEx");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when accessing " +
                               "context.getTimerService() in " +
                               "setContext method");
        }

    }

    public void setMessageDrivenContext(javax.ejb.MessageDrivenContext mdc) {
        System.out.println("In MDB:setMessageDrivenContext");
    }

    public void ejbCreate() {
        System.out.println("In MDB:ejbCreate");
    }

    public void ejbRemove() {
        System.out.println("In MDB:ejbRemove");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message) {
        System.out.println("Got message!!!");


        try {

            if( !onMessageInterceptorCalled ) {
                throw new Exception("Error : interceptor wasn't invoked");
            }

            if( intValue3 == 3 ) {
                // Also make sure env-entry is not visible in component env
                try {
                    ctx.lookup("someIntValue3");
                    throw new Exception("shouldn't have found someIntValue3");
                } catch(IllegalArgumentException ise) {
                    System.out.println("Successfully verified that " +
                                       "env-entry someIntValue3 is not " +
                                       "visible in java:comp/env");
                }
            } else {
                throw new Exception("wrong value for intValue3 = " +
                                    intValue3);
            } 

            if( stringValue1.equals("undefined") ||
                (intValue1 == 1) || (integerValue1.intValue() == 1) ||
                (sv1 == 1) ) {
                throw new Exception("env-entry @Resource override error");
            } else {
                System.out.println("stringValue1 = " + stringValue1 +
                                   " intValue1 = " + intValue1 +
                                   " integerValue1 = " + integerValue1 +
                                   " shortValue1 = " + sv1);
            }

            if( !stringValue2.equals("undefined") ||
                (intValue2 != 1) || (integerValue2.intValue() != 1) ) {
                throw new Exception("env-entry @Resource override error");
            }
            
            // Proprietary way to look up tx manager.  
            TransactionManager tm = (TransactionManager)
                new InitialContext().lookup("java:appserver/TransactionManager");
            // Use an implementation-specific check to ensure that there
            // is no tx.  A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.
            int txStatus = tm.getStatus();
            if( txStatus == Status.STATUS_NO_TRANSACTION ) {
                System.out.println("Successfully verified tx attr = " +
                                   "TX_NOT_SUPPORTED in onMessage()");
            } else {
                throw new Exception("Invalid tx status for TX_NOT_SUPPORTED" +
                                    " method " + txStatus);
            }
            
            System.out.println("Calling hello1 stateless bean");
            hello1.hello("local ejb3.0 stateless");

            System.out.println("Calling hello11 stateless bean");

            // Get context through direct "java:comp/EJBContext" lookup
            MessageDrivenContext ctx2 = (MessageDrivenContext)
                new InitialContext().lookup("java:comp/EJBContext");
            Hello1 hello11 = (Hello1) ctx2.lookup("hello1");
            hello11.hello("local ejb3.0 stateless 2");

            System.out.println("Calling hello2 stateful bean");
            hello2.hello("local ejb3.0 stateful");
            hello2.removeMethod();

            try {
                hello2.hello("this call should not go through");
                throw new Exception("bean should have been removed " +
                                    "after removeMethod()");
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully caught EJBException after " +
                                   " accessing removed SFSB");
            }


            System.out.println("creating timer");
            TimerService timerService = (TimerService) 
                new InitialContext().lookup("java:comp/TimerService");
                
            timerService.createTimer(7000, "created timer");
            
            Collection timers = injectedTimerService.getTimers();
            if( timers.size() != 1 ) {
                throw new IllegalStateException("invalid timer count = " +
                                                timers.size());
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Timeout private void timeout(javax.ejb.Timer t) {

        QueueConnection connection = null;        

        try {
            System.out.println("In MessageBean.  Got timeout callback");

            if( HelloStatelessSuper.timeoutHappened ) {
                System.out.println("Verified that stateless session bean " +
                                   " timeout happened");
            } else {
                throw new Exception("Stateless session bean timeout " +
                                    "never happened");
            }

            if( timeoutInterceptorCalled ) {
                throw new Exception("Error : interceptor was invoked." +
                                    "Interceptors should not apply to timeout"
                                    + "methods");
            } else {
                System.out.println("Interceptor was successfully ignored for "
                                   + "timeout method");
            }

            InitialContext ic = new InitialContext();

            // Proprietary way to look up tx manager.  
            TransactionManager tm = (TransactionManager)
                ic.lookup("java:appserver/TransactionManager");
            // Use an implementation-specific check to ensure that there
            // is no tx.  A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.
            int txStatus = tm.getStatus();
            if( txStatus == Status.STATUS_NO_TRANSACTION ) {
                System.out.println("Successfully verified tx attr = " +
                                   "TX_NOT_SUPPORTED in timeout method");
            } else {
                throw new Exception("Invalid tx status for TX_NOT_SUPPORTED" +
                                    " method " + txStatus);
            }

            Queue queue = (Queue) ic.lookup("java:comp/env/jms/ClientQueue");
            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);
            TextMessage tmessage = session.createTextMessage();
            tmessage.setText("mdb() invoked");
            System.out.println("Sending message");
            sender.send(tmessage);
            System.out.println("message sent");

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    @AroundInvoke
    public Object intercept(InvocationContext inv)
	throws Exception
    {
	System.out.println("[mdb] Interceptor invoked...");
        System.out.println("method = " + inv.getMethod());
        System.out.println("params = " + inv.getParameters());
        int i = 0;
        for(Object o : inv.getParameters()) {
            System.out.println("param" + i + " = " + o);
            i++;
        }

        if( inv.getMethod().getName().equals("onMessage") ) {
            onMessageInterceptorCalled = true;
        } else if( inv.getMethod().getName().equals("timeout") ) {
            // This shouldn't happen.  Interceptors don't apply to timeout
            // methods.
            timeoutInterceptorCalled = true;
        }


	Object o = inv.proceed();
	System.out.println("[mdb] Interceptor after proceed()...");

        return o;
    }

}
