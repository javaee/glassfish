package samples.ejb.installed_libraries_embedded.ejb;


public interface Customer extends javax.ejb.EJBLocalObject
{
  public String getLastName() ;

  public String getFirstName() ;

  public String getAddress1() ;

  public String getAddress2() ;

  public String getCity() ;

  public String getState() ;

  public String getZipCode() ;

  public String getSSN() ;

  public long getSavingsBalance() ;

  public long getCheckingBalance() ;

  public void doCredit(long amount, String accountType) ;

  public void doDebit(long amount, String accountType) ;

}

  
