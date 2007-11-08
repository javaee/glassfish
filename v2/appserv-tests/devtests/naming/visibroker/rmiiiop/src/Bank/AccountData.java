package Bank;

public class AccountData implements java.io.Serializable {
  private String name;
  private float balance;
  public AccountData(String name, float balance) {
    this.name = name;
    this.balance = balance;
  }
  public String getName() {
    return name;
  }
  public float getBalance() {
    return balance;
  }
}
