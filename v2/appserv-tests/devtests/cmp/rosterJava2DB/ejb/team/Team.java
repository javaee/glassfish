package team;

import java.util.Collection;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Team extends  javax.ejb.EJBObject   {

    public String getTeamId() throws RemoteException;
    public String getName() throws RemoteException;
    public String getCity() throws RemoteException;
//    public Collection getPlayers() throws RemoteException;
//    public League getLeague() throws RemoteException;

    public ArrayList getCopyOfPlayers() throws RemoteException;
//    public void addPlayer(Player player) throws RemoteException;
//    public void dropPlayer(Player player) throws RemoteException;
    public double getSalaryOfPlayer(String playerName) throws RemoteException;
}