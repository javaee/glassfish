package team;

import java.util.Collection;
import java.rmi.RemoteException;
import java.util.Set;
import javax.ejb.FinderException;


public interface League extends  javax.ejb.EJBObject   {

    public String getLeagueId() throws RemoteException;
    public String getName() throws RemoteException;
    public String getSport() throws RemoteException;
    public Collection getTeams() throws RemoteException;

    public Team getRemoteTeamByCity(String city) throws FinderException, 
                                                        RemoteException;
    
    public Set getRemoteTeamsOfThisLeague() throws FinderException, 
                                                    RemoteException;

    public Collection getRemotePlayersFromLeague() throws FinderException, 
                                                   RemoteException;
    
//    public void addTeam(Team team) throws RemoteException;
//    public void dropTeam(Team team) throws RemoteException;

//    public Set getCitiesOfThisLeague() throws FinderException;
//    public String getTeamsNameByCity(String city) throws FinderException;
}