package team;

import java.rmi.RemoteException;
import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface TeamHome extends  javax.ejb.EJBHome   {

    public Team create (String id, String name, String city)
        throws CreateException, RemoteException;
    
    public Team findByPrimaryKey (String id)
        throws FinderException, RemoteException;

    public Collection findAll() 
        throws FinderException, RemoteException;
    
    public Collection findByPlayerAndLeagueViaRemote(Player player, 
                                            League league)
                                            throws FinderException, RemoteException;

}