/**
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.ejb.stateless.converter.ejb;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.TimerService;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.EJBException;
import javax.xml.parsers.*;

/**
 * A simple stateless bean for the Converter application. This bean implements all
 * business method as declared by the remote interface, <code>Converter</code>.
 *
 * @see Converter
 * @see ConverterHome
 */
public class ConverterBean implements SessionBean, TimedObject {
    
    SessionContext sessionContext_;

    public String getParserFactoryClassName() {
       String parserFactoryClassName = (SAXParserFactory.newInstance()).getClass().getName();
       System.out.println("ParserFactoryClassName" + parserFactoryClassName);
       return parserFactoryClassName;
    }


    /**
     * Required by EJB spec.
     */
    public ConverterBean() {
    }

    /**
     * Creates a bean. Required by EJB spec.
     * @exception throws CreateException.
     */
    public void ejbCreate() {}

    /**
     * Removes the bean. Required by EJB spec.
     */
    public void ejbRemove() {}

    /**
     * Loads the state of the bean from secondary storage. Required by EJB spec.
     */
    public void ejbActivate() {}
    
    /**
     * Keeps the state of the bean to secondary storage. Required by EJB spec.
     */
    public void ejbPassivate() {}

    /**
     * Sets the session context. Required by EJB spec.
     * @param ctx A SessionContext object.
     */
   public void setSessionContext(SessionContext sc) {
       sessionContext_ = sc;
   }

   public void ejbTimeout(Timer t) {}

} // ConverterBean
