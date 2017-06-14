package Bank;

public interface Account extends java.rmi.Remote {
  String name () throws java.rmi.RemoteException;
  float getBalance () throws java.rmi.RemoteException;
  void setBalance (float bal) throws java.rmi.RemoteException;
}
  
