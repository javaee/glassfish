/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1asdev.jdbc.stress1.ejb;

import java.util.*;
import javax.ejb.*;
import javax.ejb.*;
import javax.naming.*;
import com.sun.s1asdev.jdbc.stress1.util.*;

public class RosterBean implements SessionBean {

    private  LocalPlayerHome playerHome = null;
    private  LocalTeamHome teamHome = null;
    private  LocalLeagueHome leagueHome = null;

    // Player business methods
    public ArrayList testFinder(String parm1, String parm2,
        String parm3) {

        //Debug.print("RosterBean testFinder");
        Collection players = null;

        try {
            players = playerHome.findByTest(parm1, parm2, parm3);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    }

    public void createPlayer(PlayerDetails details) { 
        //Debug.print("RosterBean createPlayer");
        try {
            LocalPlayer player = playerHome.create(details.getId(), 
                details.getName(), details.getPosition(), details.getSalary());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }
 
    public void addPlayer(String playerId, String teamId) { 
        //Debug.print("RosterBean addPlayer");
        try {
            LocalTeam team = teamHome.findByPrimaryKey(teamId);
            LocalPlayer player = playerHome.findByPrimaryKey(playerId);
            team.addPlayer(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void removePlayer(String playerId) { 
        //Debug.print("RosterBean removePlayer");
        try {
            LocalPlayer player = playerHome.findByPrimaryKey(playerId);
            player.remove();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }
 
    public void dropPlayer(String playerId, String teamId) {
        //Debug.print("RosterBean dropPlayer");
        try {
            LocalPlayer player = playerHome.findByPrimaryKey(playerId);
            LocalTeam team = teamHome.findByPrimaryKey(teamId);
            team.dropPlayer(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public PlayerDetails getPlayer(String playerId) {
        //Debug.print("RosterBean getPlayer");
        PlayerDetails playerDetails = null;
        try {
            LocalPlayer player = playerHome.findByPrimaryKey(playerId);
            playerDetails = new PlayerDetails(playerId,
                player.getName(), player.getPosition(), player.getSalary());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return playerDetails;
    }

    public ArrayList getPlayersOfTeam(String teamId) { 
        //Debug.print("RosterBean getPlayersOfTeam");
        Collection players = null;

        try {
            LocalTeam team = teamHome.findByPrimaryKey(teamId);
            players = team.getPlayers();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    }


    public ArrayList getPlayersOfTeamCopy(String teamId) { 
        //Debug.print("RosterBean getPlayersOfTeamCopy");
        ArrayList playersList = null;

        try {
            LocalTeam team = teamHome.findByPrimaryKey(teamId);
            playersList = team.getCopyOfPlayers();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return playersList;
    }


    public ArrayList getTeamsOfLeague(String leagueId) { 
        //Debug.print("RosterBean getTeamsOfLeague");

        ArrayList detailsList = new ArrayList();
        Collection teams = null;
        try {
            LocalLeague league = leagueHome.findByPrimaryKey(leagueId);
            teams = league.getTeams();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        Iterator i = teams.iterator();
        while (i.hasNext()) {
            LocalTeam team = (LocalTeam) i.next();
            TeamDetails details = new TeamDetails(team.getTeamId(),
                team.getName(), team.getCity());
            detailsList.add(details);
        }
        return detailsList;
    }


    public ArrayList getPlayersByPosition(String position) {
        //Debug.print("RosterBean getPlayersByPosition");
        Collection players = null;

        try {
            players = playerHome.findByPosition(position);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    }


    public ArrayList getPlayersByHigherSalary(String name) { 
        //Debug.print("RosterBean getPlayersByByHigherSalary");
        Collection players = null;

        try {
            players = playerHome.findByHigherSalary(name);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    } // getPlayersByHigherSalary

    public ArrayList getPlayersBySalaryRange(double low, double high) { 
        //Debug.print("RosterBean getPlayersBySalaryRange");
        Collection players = null;

        try {
            players = playerHome.findBySalaryRange(low, high);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    } // getPlayersBySalaryRange

    public ArrayList getPlayersByLeagueId(String leagueId) { 
        //Debug.print("RosterBean getPlayersByLeagueId");
        Collection players = null;

        try {
            LocalLeague league = leagueHome.findByPrimaryKey(leagueId);
            players = playerHome.findByLeague(league);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    } // getPlayersByLeagueId

    public ArrayList getPlayersBySport(String sport) { 
        //Debug.print("RosterBean getPlayersBySport");
        Collection players = null;

        try {
            players = playerHome.findBySport(sport);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    } // getPlayersBySport

    public ArrayList getPlayersByCity(String city) { 
        //Debug.print("RosterBean getPlayersByCity");
        Collection players = null;

        try {
            players = playerHome.findByCity(city);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    } // getPlayersByCity

    public ArrayList getAllPlayers() { 
        //Debug.print("RosterBean getAllPlayers");
        Collection players = null;

        try {
            players = playerHome.findAll();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }

        return copyPlayersToDetails(players);
    } // getAllPlayers

    public ArrayList getPlayersNotOnTeam() { 
        //Debug.print("RosterBean getPlayersNotOnTeam");
        Collection players = null;

        try {
            players = playerHome.findNotOnTeam();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    } // getPlayersNotOnTeam

    public ArrayList getPlayersByPositionAndName(String position, 
        String name) { 
        //Debug.print("RosterBean getPlayersByPositionAndName");
        Collection players = null;

        try {
            players = playerHome.findByPositionAndName(position, name);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return copyPlayersToDetails(players);
    } // getPlayersByPositionAndName

    public ArrayList getLeaguesOfPlayer(String playerId) { 
        //Debug.print("RosterBean getLeaguesOfPlayer");
        ArrayList detailsList = new ArrayList();
        Collection leagues = null;

        try {
            LocalPlayer player = playerHome.findByPrimaryKey(playerId);
            leagues = player.getLeagues();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
      
        Iterator i = leagues.iterator();
        while (i.hasNext()) {
            LocalLeague league = (LocalLeague) i.next();
            LeagueDetails details = new LeagueDetails(league.getLeagueId(),
                                                      league.getName(),
                                                      league.getSport());
            detailsList.add(details);
        }
        return detailsList;
    } // getLeaguesOfPlayer

    public ArrayList getSportsOfPlayer(String playerId) { 
        //Debug.print("RosterBean getSportsOfPlayer");
        ArrayList sportsList = new ArrayList();
        Collection sports = null;

        try {
            LocalPlayer player = playerHome.findByPrimaryKey(playerId);
            sports = player.getSports();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
      
        Iterator i = sports.iterator();
        while (i.hasNext()) {
            String sport = (String) i.next();
            sportsList.add(sport);
        }
        return sportsList;
    } // getSportsOfPlayer

    // Team business methods
    public void createTeamInLeague(TeamDetails details, String leagueId) { 
        //Debug.print("RosterBean createTeamInLeague");
        try {
            LocalLeague league = leagueHome.findByPrimaryKey(leagueId);
            LocalTeam team = teamHome.create(details.getId(),
                                             details.getName(),
                                             details.getCity());
            league.addTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }
 
    public void removeTeam(String teamId) { 
        //Debug.print("RosterBean removeTeam");
        try {
            LocalTeam team = teamHome.findByPrimaryKey(teamId);
            team.remove();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }
 
    public TeamDetails getTeam(String teamId) {
        //Debug.print("RosterBean getTeam");
        TeamDetails teamDetails = null;
        try {
            LocalTeam team = teamHome.findByPrimaryKey(teamId);
            teamDetails = new TeamDetails(teamId,
                                          team.getName(), team.getCity());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return teamDetails;
    }

    // League business methods
    public void createLeague(LeagueDetails details) { 
        //Debug.print("RosterBean createLeague");
        try {
            LocalLeague league = leagueHome.create(details.getId(), 
                                                   details.getName(),
                                                   details.getSport());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }
 
    public void removeLeague(String leagueId) { 
        //Debug.print("RosterBean removeLeague");
        try {
            LocalLeague league = leagueHome.findByPrimaryKey(leagueId);
            league.remove();
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }
 
    public LeagueDetails getLeague(String leagueId) {
        //Debug.print("RosterBean getLeague");
        LeagueDetails leagueDetails = null;
        try {
            LocalLeague league = leagueHome.findByPrimaryKey(leagueId);
            leagueDetails = new LeagueDetails(leagueId,
                                              league.getName(),
                                              league.getSport());
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
        return leagueDetails;
    }
 
    // SessionBean methods
    public void ejbCreate() throws CreateException {
        //Debug.print("RosterBean ejbCreate");
        try {
            playerHome = lookupPlayer();
            teamHome = lookupTeam();
            leagueHome = lookupLeague();
        } catch (NamingException ex) {
            throw new CreateException(ex.getMessage());
        }
    }

    public void ejbActivate() {
        //Debug.print("RosterBean ejbActivate");
        try {
            playerHome = lookupPlayer();
            teamHome = lookupTeam();
            leagueHome = lookupLeague();
        } catch (NamingException ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void ejbPassivate() {
        playerHome = null;
        teamHome = null;
        leagueHome = null;
    }

    public RosterBean() {}
    public void ejbRemove() {}
    public void setSessionContext(SessionContext sc) {}
 
    // Private methods
    private LocalPlayerHome lookupPlayer() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimplePlayer");
        return (LocalPlayerHome) objref;
    }

   private LocalTeamHome lookupTeam() throws NamingException {
       Context initial = new InitialContext();
       Object objref = initial.lookup("java:comp/env/ejb/SimpleTeam");
       return (LocalTeamHome) objref;
   }

   private LocalLeagueHome lookupLeague() throws NamingException {
       Context initial = new InitialContext();
       Object objref = initial.lookup("java:comp/env/ejb/SimpleLeague");
       return (LocalLeagueHome) objref;
   }

   private ArrayList copyPlayersToDetails(Collection players) {
       ArrayList detailsList = new ArrayList();
       Iterator i = players.iterator();

       while (i.hasNext()) {
           LocalPlayer player = (LocalPlayer) i.next();
           PlayerDetails details = new PlayerDetails(player.getPlayerId(),
                                                     player.getName(),
                                                     player.getPosition(),
                                                     player.getSalary());
           detailsList.add(details);
       }
       return detailsList;
   }
}
