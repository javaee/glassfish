package team;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.FinderException;

public interface Player extends  javax.ejb.EJBObject   {

    public String getPlayerId() throws RemoteException;
    public String getName() throws RemoteException;
    public String getPosition() throws RemoteException;
    public double getSalary() throws RemoteException;
//    public Collection getTeams() throws RemoteException;
//    public Collection getLeagues() throws FinderException, RemoteException;
//    public Collection getSports() throws FinderException, RemoteException;

}