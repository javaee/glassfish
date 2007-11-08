package examples.sfsb;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * This class illustrates calling a stateful SessionBean and performing
 * the following exercises:
 * <ul>
 * <li> Create a Trader
 * <li> Buy some shares using the Trader
 * <li> Sell some shares using the Trader
 * <li> Remove the Trader
 * </ul>
 *
 */
public class Client {

    private SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

  private static final String JNDI_NAME = "MyTraderBean";

  private String url;
  private TraderHome home;
	private String testId = null;

  public Client(String url)
  throws NamingException {
      
      this.url       = url;
      
 //     home = lookupHome();
  }
  
  public Client () throws javax.naming.NamingException{
//      home = lookupHome();
  }

  /**
   * Runs this example from the command line. Example:
   * <p>
   * <p>
   * The parameters are optional, but if any are supplied,
   * they are interpreted in this order:
   * <p>
   */
  public static void main(String[] args) {
    log("\nBeginning statefulSession.Client...\n");
	try{
	Client client = new Client();
	client.run();
	}catch(NamingException ne){
	}
}

	public void run(){

	testId = "stateful-persistence::Sample Standalone-ejb-jar";
  stat.addDescription("This is to test the persistence sample deploed as an standalone ejb-jar."); 

    try {
	home = lookupHome();
    } catch (NamingException ne) {
	stat.addStatus(testId, stat.FAIL);
    }

    try {
      example();
	stat.addStatus(testId, stat.PASS);
    } catch (Exception e) {
      log("There was an exception while creating and using the Trader.");
      log("This indicates that there was a problem communicating with the server: "+e);
        stat.addStatus(testId, stat.FAIL);
    }finally {
            stat.printSummary(testId);
        }

    log("\nEnd statefulSession.Client...\n");
  }

  /**
   * Runs the example.
   */
  public void example()
    throws CreateException, RemoteException,RemoveException,ProcessingErrorException
  {
    String customerName = "Matt"; 

    // Create a Trader
    log("Creating trader\n");
    TraderRemote trader = (TraderRemote) narrow(home.create(), TraderRemote.class);

    // Sell some stock
    String stockName   = "MSFT";
    int numberOfShares = 200;
    log("Selling " + numberOfShares + " of " + stockName);
    TradeResult tr = trader.sell(customerName, stockName, numberOfShares);
    log(tr.toString());

    // Buy some stock
    stockName      = "BEAS";
    numberOfShares = 250;
    log("Buying " + numberOfShares + " of " + stockName);
    tr = trader.buy(customerName, stockName, numberOfShares);
    log(tr.toString());

    // Get change in Cash Account from EJBean
    log("Change in Cash Account: $" + trader.getBalance()
      + "\n");
    log("Removing trader\n");
    trader.remove();

  }

  /**
   * RMI/IIOP clients should use this narrow function
   */
  private Object narrow(Object ref, Class c) {
    return PortableRemoteObject.narrow(ref, c);
  }

  /**
   * Lookup the EJBs home in the JNDI tree
   */
  private TraderHome lookupHome()
    throws NamingException
  {
    // Lookup the beans home using JNDI



    Context ctx = null;
   
    try {
                // Initialize the Context with JNDI specific properties
                ctx = new InitialContext();
      Object home = ctx.lookup(JNDI_NAME);
      return (TraderHome) narrow(home, TraderHome.class);
    } catch (NamingException ne) {
      log("The client was unable to lookup the EJBHome.  Please make sure ");
      log("that you have deployed the ejb with the JNDI name "+JNDI_NAME+" on the appserver at "+url);
      throw ne;
    }
  }


  private static void log(String s) {
    System.out.println(s);
  }

}
