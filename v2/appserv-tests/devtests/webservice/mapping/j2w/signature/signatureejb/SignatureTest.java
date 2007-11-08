package signatureejb;

import java.util.Date;

public interface SignatureTest extends java.rmi.Remote {

/*
    public void SetTestDate(java.util.Date testDate) throws java.rmi.RemoteException;
    public java.util.Date GetTestDate() throws java.rmi.RemoteException;
*/


/*
    public void setMyDateValueType(MyDateValueType myDate) throws java.rmi.RemoteException;
    public MyDateValueType getMyDateValueType() throws java.rmi.RemoteException;
    public void setMyDateValueTypes(MyDateValueType[] myDate) throws java.rmi.RemoteException;
    public MyDateValueType[] getMyDateValueTypes() throws java.rmi.RemoteException;
*/

    public String SayHello(String hello) throws java.rmi.RemoteException;
}
