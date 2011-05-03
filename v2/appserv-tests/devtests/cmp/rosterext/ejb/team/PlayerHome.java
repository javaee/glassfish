package team;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface PlayerHome extends  javax.ejb.EJBHome   {

    public Player create (String id, String name, String position,
        double salary)
        throws CreateException, RemoteException;
    
    public Player findByPrimaryKey (String id)
        throws FinderException, RemoteException;
    
    public Collection findByPosition(String position) 
        throws FinderException, RemoteException;

    public Collection findByHigherSalary(String name) 
        throws FinderException, RemoteException;

    public Collection findBySalaryRange(double low, double high) 
        throws FinderException, RemoteException;

//    public Collection findByLeague(League league) 
//        throws FinderException, RemoteException;

    public Collection findBySport(String sport) 
        throws FinderException, RemoteException;

    public Collection findByCity(String city) 
        throws FinderException, RemoteException;

    public Collection findAll() 
        throws FinderException, RemoteException;

    public Collection findNotOnTeam() 
        throws FinderException, RemoteException;

    public Collection findByPositionAndName(String position, 
        String name) throws FinderException, RemoteException;

    public Collection findByTest (String parm1, String parm2, String parm3)
        throws FinderException, RemoteException;



}