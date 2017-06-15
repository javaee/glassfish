package examples.sfsb;


import javax.ejb.*;

import java.rmi.RemoteException;


public interface TraderRemote extends EJBObject {

  public  TradeResult buy(String customerName, String stockSymbol, int shares)
    throws ProcessingErrorException, RemoteException;

  public TradeResult sell(String customerName, String stockSymbol, int shares)
    throws ProcessingErrorException, RemoteException;

  public double getBalance() 
    throws RemoteException;

}
