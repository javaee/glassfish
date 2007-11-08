/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.stateless.simple.ejb;

import java.util.*; 
import java.io.*; 

/**
 * A simple stateless bean for the HelloWorld application. This bean implements one
 * business method as declared by the remote interface.
 */
public class GreeterEJB implements javax.ejb.SessionBean { 

    private  javax.ejb.SessionContext m_ctx = null; 
   
    /**
     * Sets the session context. Required by EJB spec.
     * @param ctx A SessionContext object.
     */
    public void setSessionContext(javax.ejb.SessionContext ctx) { 
        m_ctx = ctx; 
    } 

    /**
     * Creates a bean. Required by EJB spec.
     * @exception throws CreateException.
     */
    public void ejbCreate() throws javax.ejb.EJBException, javax.ejb.CreateException { 
        System.out.println("ejbCreate() on obj " + this); 
    } 

    /**
     * Removes the bean. Required by EJB spec.
     */
    public void ejbRemove() { 
        System.out.println("ejbRemove() on obj " + this); 
    } 

    /**
     * Loads the state of the bean from secondary storage. Required by EJB spec.
     */
    public void ejbActivate() { 
        System.out.println("ejbActivate() on obj " + this); 
    } 

    /**
     * Serializes the state of the bean to secondary storage. Required by EJB spec.
     */
    public void ejbPassivate() { 
        System.out.println("ejbPassivate() on obj " + this); 
    } 

    /**
     * Required by EJB spec.
     */
    public void Greeter() { 
    } 


    /**
     * Returns a greeting, based on the time of the day.
     * @return returns a greeting as a string.
     */
    public String getGreeting() { 
        String message = null; 
        Calendar calendar = new GregorianCalendar(); 
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); 
        if(currentHour < 12) message = "morning"; 
        else { 
          if( (currentHour >= 12) && 
            (calendar.get(Calendar.HOUR_OF_DAY) < 18)) message = "afternoon"; 
          else message = "evening"; 
        } 
        return message; 
    } 
} 
