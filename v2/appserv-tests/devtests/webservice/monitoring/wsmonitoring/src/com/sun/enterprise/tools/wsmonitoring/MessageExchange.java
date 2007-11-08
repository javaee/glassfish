/*
 * MessageExchange.java
 *
 * Created on March 16, 2005, 2:42 PM
 */

package com.sun.enterprise.tools.wsmonitoring;

import java.util.Date;

import com.sun.enterprise.webservice.monitoring.MessageTrace;
/**
 * 
 * @author dochez
 */
public class MessageExchange {
   
    MessageTrace request;
    
    MessageTrace response;
    
    final Date timeStamp = new Date();
           
    /** Creates a new instance of MessageExchange */
    public MessageExchange() {
    }
    
    public Date getTimeStamp() {
        return timeStamp;
    }
}
