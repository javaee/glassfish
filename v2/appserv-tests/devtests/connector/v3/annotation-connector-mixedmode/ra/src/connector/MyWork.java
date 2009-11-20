/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import java.lang.reflect.Method;
import java.util.Iterator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.ExecutionContext;

/**
 *
 * @author	Qingqing Ouyang
 */
public class MyWork implements Work {

    private String name;
    private boolean stop = false;
    private MessageEndpointFactory factory;
    private WorkManager wm;
    
    public MyWork(
            String name, MessageEndpointFactory factory, WorkManager wm) {
        this.factory = factory;
        this.name = name;
        this.wm = wm;
    }

    public void run() {

        debug("ENTER...");

        //try 3 times to create endpoint (in case of failure)
        for (int i = 0; i < 3; i++) {

            try {

                Method onMessage = getOnMessageMethod();
                System.out.println("isDeliveryTransacted = " + 
                                      factory.isDeliveryTransacted(onMessage));

                /*
                  MessageEndpoint ep = factory.createEndpoint(null);
                  ep.beforeDelivery(onMessage);
                  ((MyMessageListener) ep).onMessage(name + ": TEST MSG ONE");
                  ((MyMessageListener) ep).onMessage(name + ": TEST MSG TWO");
                  ((MyMessageListener) ep).onMessage(name + ": TEST MSG THREE");
                  ep.afterDelivery();
                  break;
                */

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        debug("LEAVE...");
    }

    public void release() {}

    public void stop() {
        this.stop = true;
    }

    public String toString() {
       return name;
    }

    public Method getOnMessageMethod() {
        
        Method onMessageMethod = null;
        try {
            Class msgListenerClass = connector.MyMessageListener.class;
            Class[] paramTypes = { java.lang.String.class };
            onMessageMethod = 
                msgListenerClass.getMethod("onMessage", paramTypes);
            
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return onMessageMethod;
    }

    private void debug(String mesg) {
        System.out.println("MyWork[" + name + "] --> " + mesg);
    }
}
