// AccountImpl.java

public class AccountImpl extends Bank.AccountPOA {
  
  public AccountImpl(Bank.AccountData data) {
    _name = data.getName();
    _balance = data.getBalance();
  }

  public String name () throws java.rmi.RemoteException {
    return _name;
  }

  public float getBalance () throws java.rmi.RemoteException {
    return _balance;
  }

  public void setBalance (float balance) throws java.rmi.RemoteException {
    _balance = balance;
  }

  private float _balance;
  private String _name;
}

