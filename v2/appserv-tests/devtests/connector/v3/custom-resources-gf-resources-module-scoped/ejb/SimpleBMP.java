package com.sun.s1asdev.jdbc.CustomResourceFactories.ejb;

import javax.ejb.*;
import java.rmi.*;
import java.util.Properties;

public interface SimpleBMP
    extends EJBObject {
    public boolean testJavaBean(String testValue) throws RemoteException;
    public boolean testPrimitives(String type, String value, String resourceName) throws RemoteException;
    public boolean testProperties(Properties properties, String resourceName) throws RemoteException;
    public boolean testURL(String url, String resourceName) throws RemoteException;
}
