package Bank;

public interface AccountManagerOperations {
  Account create(AccountData data);
  Account get (String name);
  java.util.Hashtable getAccounts();
}
