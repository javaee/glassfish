package team;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface LeagueHome extends  javax.ejb.EJBHome   {

    public League create (String id, String name, String sport)
        throws CreateException, RemoteException;
    
    public League findByPrimaryKey (String id)
        throws FinderException, RemoteException;

    public Collection findAll() 
        throws FinderException, RemoteException;

    public League findByName(String name)
        throws FinderException, RemoteException;					
        
}