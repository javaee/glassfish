/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.ejb.cmp.roster.ejb;

import java.util.*;
import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import com.sun.s1peqe.ejb.cmp.roster.util.*;

public interface Roster extends EJBObject {
 
    // Players

    public void createPlayer(PlayerDetails details) 
        throws RemoteException;

    public void addPlayer(String playerId, String teamId) 
        throws RemoteException;

    public void removePlayer(String playerId) 
        throws RemoteException;

    public void dropPlayer(String playerId, String teamId) 
        throws RemoteException;

    public PlayerDetails getPlayer(String playerId) 
        throws RemoteException;

    public ArrayList getPlayersOfTeam(String teamId) 
        throws RemoteException;

    public ArrayList getPlayersOfTeamCopy(String teamId) 
        throws RemoteException;

    public ArrayList getPlayersByPosition(String position) 
        throws RemoteException;

    public ArrayList getPlayersByHigherSalary(String name) 
        throws RemoteException;

    public ArrayList getPlayersBySalaryRange(double low, double high) 
        throws RemoteException;

    public ArrayList getPlayersByLeagueId(String leagueId) 
        throws RemoteException;

    public ArrayList getPlayersBySport(String sport) 
        throws RemoteException;

    public ArrayList getPlayersByCity(String city) 
        throws RemoteException;

    public ArrayList getAllPlayers() 
        throws RemoteException;

    public ArrayList getPlayersNotOnTeam() 
        throws RemoteException;

    public ArrayList getPlayersByPositionAndName(String position, 
        String name) throws RemoteException;

    public ArrayList getLeaguesOfPlayer(String playerId)
        throws RemoteException;

    public ArrayList getSportsOfPlayer(String playerId)
        throws RemoteException;

    // Teams

    public ArrayList getTeamsOfLeague(String leagueId) 
        throws RemoteException;

    public void createTeamInLeague(TeamDetails details, String leagueId) 
        throws RemoteException;

    public void removeTeam(String teamId) 
        throws RemoteException;

    public TeamDetails getTeam(String teamId) 
        throws RemoteException;

    // Leagues

    public void createLeague(LeagueDetails details) 
        throws RemoteException;

    public void removeLeague(String leagueId) 
        throws RemoteException;

    public LeagueDetails getLeague(String leagueId) 
        throws RemoteException;

    // Test

    public ArrayList testFinder(String parm1, String parm2, String parm3)
        throws RemoteException;
}
