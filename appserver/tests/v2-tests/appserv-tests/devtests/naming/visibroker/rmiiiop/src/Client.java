// Client.java

public class Client {

  public static void main(String[] args) {
    try {
      // Initialize the ORB.
      org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
      // Get the manager Id
      byte[] managerId = "RMIBankManager".getBytes();
      // Locate an account manager. Give the full POA name and the servant ID.
      Bank.AccountManager manager = 
	Bank.AccountManagerHelper.bind(orb, "/rmi_bank_poa", managerId);
      // Use any number of argument pairs to indicate name,balance of accounts to create
      if (args.length == 0 || args.length % 2 != 0) {
        args = new String[2];
        args[0] = "Jack B. Quick";
        args[1] = "123.23";

      }
      int i = 0;
      while (i < args.length) {
        String name = args[i++];
        float balance;
        try {
          balance = new Float(args[i++]).floatValue();
        } catch (NumberFormatException n) {
          balance = 0;
        }
        Bank.AccountData data = new Bank.AccountData(name, balance);
        Bank.Account account = manager.create(data);
        System.out.println
          ("Created account for " + name + " with opening balance of $" + balance);
      } 
    
      java.util.Hashtable accounts = manager.getAccounts();
    
      for (java.util.Enumeration e = accounts.elements(); e.hasMoreElements();) {
        Bank.Account account = Bank.AccountHelper.narrow((org.omg.CORBA.Object)e.nextElement());
        String name = account.name();
        float balance = account.getBalance();
        System.out.println("Current balance in " + name + "'s account is $" + balance);
        System.out.println("Crediting $10 to " + name + "'s account.");
        account.setBalance(balance + (float)10.0);
        balance = account.getBalance();
        System.out.println("New balance in " + name + "'s account is $" + balance);
      }
    } catch (java.rmi.RemoteException e) {
      System.err.println(e);
    }
  }
}
