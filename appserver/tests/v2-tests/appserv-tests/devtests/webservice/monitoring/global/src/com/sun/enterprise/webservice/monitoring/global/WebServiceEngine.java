/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */


package com.sun.enterprise.webservice.monitoring.global;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the web service interface to access most of the 
 * WebService engine functionality
 *
 * @author dochez
 */
public interface WebServiceEngine extends Remote {
  
    public EndpointInfo getEndpoint(String selector) throws RemoteException;
    
    public int getEndpointsCount() throws RemoteException; 
    
    public String getEndpointsSelector(int i) throws RemoteException;
    
    public int getTraceCount() throws RemoteException;
    
    public InvocationTrace getTrace(int i) throws RemoteException;
}
