/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.stateless.simple.ejb;

/**
 * Remote interface for the GreeterEJB. The remote interface defines all possible
 * business methods for the bean. These are the methods going to be invoked remotely
 * by clients, once they have a reference to the remote interface.
 *
 * Clients (GreeterServlet, in this case), generally take the help of JNDI to lookup
 * the bean's home interface (GreeterHome, in this case) and then use the home interface
 * to obtain references to the bean's remote interface (Greeter, in this case).
 *
 */
public interface Greeter extends javax.ejb.EJBObject { 
    /**
     * Returns a greeting.
     * @return returns a greeting as a string.
     * @exception throws a RemoteException.
     *
     */
    public String getGreeting() throws java.rmi.RemoteException; 
} 
