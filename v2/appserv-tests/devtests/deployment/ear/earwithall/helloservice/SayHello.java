/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
 package helloservice;
 
import java.rmi.Remote;
import java.rmi.RemoteException;

 /*
  * This is a minimum WebService interface
  */
  public interface SayHello  extends Remote {
  	
  	/*
  	 * @return a hello string
  	 */
  	 public String sayHello(String name) throws RemoteException;
  
  }
  	