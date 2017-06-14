package soapfault.ejb;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SimpleSoapFaultSEI extends Remote {
    
    public String simpleMethod() throws SimpleSoapException, RemoteException;
            
}