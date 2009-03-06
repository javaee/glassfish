/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package roster;

import java.util.ArrayList;
import javax.ejb.EJBLocalObject;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import util.LeagueDetails;
import util.PlayerDetails;
import util.TeamDetails;
import java.util.Set;

public interface Roster extends EJBLocalObject {
 
    // Players

    public void createPlayer(PlayerDetails details) 
        ;

    public void addPlayer(String playerId, String teamId) 
        ;

    public void removePlayer(String playerId) 
        ;

    public void dropPlayer(String playerId, String teamId) 
        ;

    public PlayerDetails getPlayer(String playerId) 
        ;

    public ArrayList getPlayersOfTeam(String teamId) 
        ;

    public ArrayList getPlayersOfTeamCopy(String teamId) 
        ;

    public ArrayList getPlayersByPosition(String position) 
        ;

    public ArrayList getPlayersByHigherSalary(String name) 
        ;

    public ArrayList getPlayersBySalaryRange(double low, double high) 
        ;

    public ArrayList getPlayersByLeagueId(String leagueId) 
        ;

    public ArrayList getPlayersBySport(String sport) 
        ;

    public ArrayList getPlayersByCity(String city) 
        ;

    public ArrayList getAllPlayers() 
        ;

    public ArrayList getPlayersNotOnTeam() 
        ;

    public ArrayList getPlayersByPositionAndName(String position, 
        String name) ;

    public ArrayList getLeaguesOfPlayer(String playerId)
        ;

    public ArrayList getSportsOfPlayer(String playerId)
        ;
        
    public double getSalaryOfPlayerFromTeam(String teamID, String playerName)
        ;

    public ArrayList getPlayersOfLeague(String leagueId)
        ;   
        

    public ArrayList getPlayersWithPositionsGoalkeeperOrDefender()
        ;   

    public ArrayList getPlayersWithNameEndingWithON() 
        ;

    public ArrayList getPlayersWithNullName()
        ;   

    public ArrayList getPlayersWithTeam(String teamId)
        ;
        
    public ArrayList getPlayersWithSalaryUsingABS(double salary)
        ; 

    public ArrayList getPlayersWithSalaryUsingSQRT(double salary) 
        ;
    
           
    // Teams

    public ArrayList getTeamsOfLeague(String leagueId) 
        ;

    public void createTeamInLeague(TeamDetails details, String leagueId) 
        ;

    public void removeTeam(String teamId) 
        ;

    public TeamDetails getTeam(String teamId) 
        ;

    public ArrayList getTeamsByPlayerAndLeague(String playerKey,
                                               String leagueKey)
                                               ;	
 
    public Set getCitiesOfLeague(String leagueKey) ;

    public TeamDetails getTeamOfLeagueByCity(String leagueKey, String city)
        ;	   

    public String getTeamsNameOfLeagueByCity(String leagueKey, String city)
        ;	   

    public  String getTeamNameVariations(String teamId) ;

    // Leagues

    public void createLeague(LeagueDetails details) 
        ;

    public void removeLeague(String leagueId) 
        ;

    public LeagueDetails getLeague(String leagueId) 
        ;

    public LeagueDetails getLeagueByName(String name)
        ;
        
    // Test

    public ArrayList getPlayersByLeagueIdWithNULL(String leagueId)  ;

    public ArrayList testFinder(String parm1, String parm2, String parm3)
        ;
        
    public void cleanUp() throws FinderException, RemoveException;
        
}
