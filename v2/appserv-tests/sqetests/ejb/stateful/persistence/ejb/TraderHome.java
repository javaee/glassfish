package examples.sfsb;


import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface TraderHome extends EJBHome {

  
  TraderRemote create() throws CreateException, RemoteException;
}
