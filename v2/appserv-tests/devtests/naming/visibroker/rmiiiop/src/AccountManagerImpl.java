// AccountManagerImpl.java
import org.omg.PortableServer.*;

import java.util.*;

public class AccountManagerImpl extends Bank.AccountManagerPOA {

  public java.util.Hashtable getAccounts () {
    return _accounts;
  }

  public Bank.Account get (java.lang.String arg0) {
    return (Bank.Account) _accounts.get(arg0);
  }

  public synchronized Bank.Account create (Bank.AccountData arg0) {
    // Lookup the account in the account dictionary.
    Bank.Account account = (Bank.Account) _accounts.get(arg0.getName());
    // If there was no account in the dictionary, create one.
    if(account == null) {
      // Create the account implementation, given the balance.
      AccountImpl accountServant = new AccountImpl(arg0);
      try {
        // Activate it on the default POA which is root POA for this servant
        account = Bank.AccountHelper.narrow(_default_POA().servant_to_reference(accountServant));
      } catch (Exception e) {
        e.printStackTrace();
      }
      // Print out the new account.
      System.out.println("Created " + arg0.getName() + "'s account: " + account);
      // Save the account in the account dictionary.
      _accounts.put(arg0.getName(), account);
    }
    // Return the account.
    return account;
  }
  private Hashtable _accounts = new Hashtable();
}

