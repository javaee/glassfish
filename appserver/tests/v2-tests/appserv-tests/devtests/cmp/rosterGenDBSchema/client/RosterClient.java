/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2018 Oracle and/or its affiliates. All rights reserved.
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

package client;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import util.*;
import roster.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class RosterClient {

    private static SimpleReporterAdapter stat =
	new SimpleReporterAdapter("appserv-tests");

   public static void main(String[] args) {
       try {
	   System.out.println("START");
           stat.addDescription("RosterGenDBSchema");

           Context initial = new InitialContext();
           Object objref = initial.lookup("java:comp/env/ejb/SimpleRosterExt");

           RosterHome home = 
               (RosterHome)PortableRemoteObject.narrow(objref, 
                                            RosterHome.class);

           Roster myRoster = home.create();

           insertInfo(myRoster);
           getSomeInfo(myRoster);

           // getMoreInfo(myRoster);

	   stat.addStatus("ejbclient RosterGenDBSchema", stat.PASS);
	   stat.printSummary("RosterGenDBSchema");
           System.exit(0);

       } catch (Exception ex) {
           System.err.println("Caught an exception:");
           ex.printStackTrace();
           stat.addStatus("ejbclient RosterGenDBSchema", stat.FAIL);
       }
         stat.printSummary("RosterGenDBSchema");
   } // main


    private static void getSomeInfo(Roster myRoster) {

       try {

           ArrayList playerList;
           ArrayList teamList;
           ArrayList leagueList;

           playerList = myRoster.getPlayersOfTeam("T2");
           printDetailsList(playerList);

           teamList = myRoster.getTeamsOfLeague("L1");
           printDetailsList(teamList);

           playerList = myRoster.getPlayersByPosition("defender");
           printDetailsList(playerList);


           leagueList = myRoster.getLeaguesOfPlayer("P28");
           printDetailsList(leagueList);

       } catch (Exception ex) {
           System.err.println("Caught an exception:");
           ex.printStackTrace();
  	   stat.addStatus("ejbclient RosterGenDBSchema", stat.FAIL);
       }

    } // getSomeInfo

    private static void getMoreInfo(Roster myRoster) {

       try {

           LeagueDetails leagueDetails;
           TeamDetails teamDetails;
           PlayerDetails playerDetails;
           ArrayList playerList;
           ArrayList teamList;
           ArrayList leagueList;
           ArrayList sportList;

           leagueDetails = myRoster.getLeague("L1");
           System.out.println(leagueDetails.toString());
           System.out.println();

           teamDetails = myRoster.getTeam("T3");
           System.out.println(teamDetails.toString());
           System.out.println();

           playerDetails = myRoster.getPlayer("P20");
           System.out.println(playerDetails.toString());
           System.out.println();

           playerList = myRoster.getPlayersOfTeam("T2");
           printDetailsList(playerList);

           teamList = myRoster.getTeamsOfLeague("L1");
           printDetailsList(teamList);

           playerList = myRoster.getPlayersByPosition("defender");
           playerList = myRoster.getAllPlayers();
           playerList = myRoster.getPlayersNotOnTeam();
           playerList = myRoster.getPlayersByPositionAndName("power forward", 
               "Jack Patterson");
           playerList = myRoster.getPlayersByCity("Truckee");
           playerList = myRoster.getPlayersBySport("Soccer");
           playerList = myRoster.getPlayersByLeagueId("L1");
           playerList = myRoster.getPlayersByHigherSalary("Ian Carlyle");
           playerList = myRoster.getPlayersBySalaryRange(500.00, 800.00);
           playerList = myRoster.getPlayersOfTeamCopy("T5");

           leagueList = myRoster.getLeaguesOfPlayer("P28");
           printDetailsList(leagueList);

           sportList = myRoster.getSportsOfPlayer("P28");
           printDetailsList(sportList);

       } catch (Exception ex) {
           System.err.println("Caught an exception:");
           ex.printStackTrace();
   	   stat.addStatus("ejbclient RosterGenDBSchema", stat.FAIL);
       }

    } // getMoreInfo

    private static void printDetailsList(ArrayList list) {

        Iterator i = list.iterator();
        while (i.hasNext()) {
            Object details = (Object)i.next();
            System.out.println(details.toString());
        }
        System.out.println();
    } // printDetailsList


    private static void insertInfo(Roster myRoster) {

       try {
           // Leagues

           myRoster.createLeague(new LeagueDetails(
              "L1", "Mountain", "Soccer"));

           myRoster.createLeague(new LeagueDetails(
              "L2", "Valley", "Basketball"));

           // Teams

           myRoster.createTeamInLeague(new TeamDetails(
              "T1", "Honey Bees", "Visalia"), "L1");

           myRoster.createTeamInLeague(new TeamDetails(
              "T2", "Gophers", "Manteca"), "L1");
           
           myRoster.createTeamInLeague(new TeamDetails(
              "T3", "Deer", "Bodie"), "L2");

           myRoster.createTeamInLeague(new TeamDetails(
              "T4", "Trout", "Truckee"), "L2");

           myRoster.createTeamInLeague(new TeamDetails(
              "T5", "Crows", "Orland"), "L1");

           // Players, Team T1

           myRoster.createPlayer(new PlayerDetails(
              "P1", "Phil Jones", "goalkeeper", 100.00));
           myRoster.addPlayer("P1", "T1");

           myRoster.createPlayer(new PlayerDetails(
              "P2", "Alice Smith", "defender", 505.00));
           myRoster.addPlayer("P2", "T1");

           myRoster.createPlayer(new PlayerDetails(
              "P3", "Bob Roberts", "midfielder", 65.00));
           myRoster.addPlayer("P3", "T1");

           myRoster.createPlayer(new PlayerDetails(
              "P4", "Grace Phillips", "forward", 100.00));
           myRoster.addPlayer("P4", "T1");

           myRoster.createPlayer(new PlayerDetails(
              "P5", "Barney Bold", "defender", 100.00));
           myRoster.addPlayer("P5", "T1");

           // Players, Team T2

           myRoster.createPlayer(new PlayerDetails(
              "P6", "Ian Carlyle", "goalkeeper", 555.00));
           myRoster.addPlayer("P6", "T2");

           myRoster.createPlayer(new PlayerDetails(
              "P7", "Rebecca Struthers", "midfielder", 777.00));
           myRoster.addPlayer("P7", "T2");

           myRoster.createPlayer(new PlayerDetails(
              "P8", "Anne Anderson", "forward", 65.00));
           myRoster.addPlayer("P8", "T2");

           myRoster.createPlayer(new PlayerDetails(
              "P9", "Jan Wesley", "defender", 100.00));
           myRoster.addPlayer("P9", "T2");

           myRoster.createPlayer(new PlayerDetails(
              "P10", "Terry Smithson", "midfielder", 100.00));
           myRoster.addPlayer("P10", "T2");

           // Players, Team T3

           myRoster.createPlayer(new PlayerDetails(
              "P11", "Ben Shore", "point guard", 188.00));
           myRoster.addPlayer("P11", "T3");

           myRoster.createPlayer(new PlayerDetails(
              "P12", "Chris Farley", "shooting guard", 577.00));
           myRoster.addPlayer("P12", "T3");

           myRoster.createPlayer(new PlayerDetails(
              "P13", "Audrey Brown", "small forward", 995.00));
           myRoster.addPlayer("P13", "T3");

           myRoster.createPlayer(new PlayerDetails(
              "P14", "Jack Patterson", "power forward", 100.00));
           myRoster.addPlayer("P14", "T3");

           myRoster.createPlayer(new PlayerDetails(
              "P15", "Candace Lewis", "point guard", 100.00));
           myRoster.addPlayer("P15", "T3");

           // Players, Team T4

           myRoster.createPlayer(new PlayerDetails(
              "P16", "Linda Berringer", "point guard", 844.00));
           myRoster.addPlayer("P16", "T4");

           myRoster.createPlayer(new PlayerDetails(
              "P17", "Bertrand Morris", "shooting guard", 452.00));
           myRoster.addPlayer("P17", "T4");

           myRoster.createPlayer(new PlayerDetails(
              "P18", "Nancy White", "small forward", 833.00));
           myRoster.addPlayer("P18", "T4");

           myRoster.createPlayer(new PlayerDetails(
              "P19", "Billy Black", "power forward", 444.00));
           myRoster.addPlayer("P19", "T4");

           myRoster.createPlayer(new PlayerDetails(
              "P20", "Jodie James", "point guard", 100.00));
           myRoster.addPlayer("P20", "T4");

           // Players, Team T5

           myRoster.createPlayer(new PlayerDetails(
              "P21", "Henry Shute", "goalkeeper", 205.00));
           myRoster.addPlayer("P21", "T5");

           myRoster.createPlayer(new PlayerDetails(
              "P22", "Janice Walker", "defender", 857.00));
           myRoster.addPlayer("P22", "T5");

           myRoster.createPlayer(new PlayerDetails(
              "P23", "Wally Hendricks", "midfielder", 748.00));
           myRoster.addPlayer("P23", "T5");

           myRoster.createPlayer(new PlayerDetails(
              "P24", "Gloria Garber", "forward", 777.00));
           myRoster.addPlayer("P24", "T5");

           myRoster.createPlayer(new PlayerDetails(
              "P25", "Frank Fletcher", "defender", 399.00));
           myRoster.addPlayer("P25", "T5");

           // Players, no team

           myRoster.createPlayer(new PlayerDetails(
              "P26", "Hobie Jackson", "pitcher", 582.00));
          
           myRoster.createPlayer(new PlayerDetails(
              "P27", "Melinda Kendall", "catcher", 677.00));

           // Players, multiple teams

           myRoster.createPlayer(new PlayerDetails(
              "P28", "Constance Adams", "substitue", 966.00));
           myRoster.addPlayer("P28", "T1");
           myRoster.addPlayer("P28", "T3");

       } catch (Exception ex) {
           System.err.println("Caught an exception:");
           ex.printStackTrace();
	   stat.addStatus("ejbclient RosterGenDBSchema", stat.FAIL);
       }

    } // insertInfo


} // class
