package test.webservice;

import java.rmi.*;

public interface WebServiceTest extends Remote {

    public String doTest(String[] params) throws RemoteException;
}

