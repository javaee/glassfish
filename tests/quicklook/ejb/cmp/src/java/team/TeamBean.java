/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 *
 */

package team;

import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import util.Debug;
import util.PlayerDetails;

public abstract class TeamBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields

    public abstract String getTeamId();
    public abstract void setTeamId(String id);
    
    public abstract String getName();
    public abstract void setName(String name);

    public abstract String getCity();
    public abstract void setCity(String city);


    // Access methods for relationship fields
             
    public abstract Collection getPlayers();
    public abstract void setPlayers(Collection players);

    public abstract LocalLeague getLeague();
    public abstract void setLeague(LocalLeague league);

    // Select methods

    public abstract double ejbSelectSalaryOfPlayerInTeam(LocalTeam team, String playerName)
        throws FinderException;

    public abstract String ejbSelectByNameWithCONCAT(String part1, String part2)
        throws FinderException;	
                            
    public abstract String ejbSelectByNameSubstring(String substring)
        throws FinderException;	

    public abstract String ejbSelectNameLocate(String substring)
        throws FinderException;	

                            
    // Business methods

    public double getSalaryOfPlayer(String playerName) throws FinderException {
        LocalTeam team = (team.LocalTeam)context.getEJBLocalObject();
        
        return ejbSelectSalaryOfPlayerInTeam(team, playerName);
    }
    
    
    public String getTeamNameWithStringfunctionTests1() throws FinderException {
                                                        
        StringBuffer out = new StringBuffer();
//        LocalTeam team = (team.LocalTeam) context.getEJBLocalObject();
//        out.append("<BR>Name of Team : " + team.getName());
        out.append("<BR>");		
        out.append(ejbSelectByNameWithCONCAT("Cr", "ows"));
        out.append("<BR>");
        
        return out.toString();
    }
    
    public String getTeamNameWithStringfunctionTests2() throws FinderException {
                                                        
        StringBuffer out = new StringBuffer();
        out.append(ejbSelectByNameSubstring("aaaaCrowsaaaaa"));
        out.append("<BR>");

        return out.toString();
    }
                                
    public String getTeamNameWithStringfunctionTests3() throws FinderException {
                                                        
        StringBuffer out = new StringBuffer();
        out.append(ejbSelectNameLocate("row"));
        out.append("<BR>");
        
        return out.toString();
    }

                                  
    public ArrayList getCopyOfPlayers() {

        Debug.print("TeamBean getCopyOfPlayers");
        ArrayList playerList = new ArrayList();
        Collection players = getPlayers();

        Iterator i = players.iterator();
        while (i.hasNext()) {
            LocalPlayer player = (LocalPlayer) i.next();
            PlayerDetails details = new PlayerDetails(player.getPlayerId(),
                player.getName(), player.getPosition(), 0.00);
            playerList.add(details);
        }

        return playerList;
    }

    public void addPlayer(LocalPlayer player) {

        Debug.print("TeamBean addPlayer");
        try {
            Collection players = getPlayers();
            players.add(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropPlayer(LocalPlayer player) {

        Debug.print("TeamBean dropPlayer");
        try {
            Collection players = getPlayers();
            players.remove(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    // EntityBean  methods

    public String ejbCreate (String id, String name, String city)
        throws CreateException {

        Debug.print("TeamBean ejbCreate");
        setTeamId(id);
        setName(name);
        setCity(city);
        return null;
    }
         
    public void ejbPostCreate (String id, String name, String city)
        throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }
    
    public void unsetEntityContext() {
        context = null;
    }
    
    public void ejbRemove() {
        Debug.print("TeamBean ejbRemove");
    }
    
    public void ejbLoad() {
        Debug.print("TeamBean ejbLoad");
    }
    
    public void ejbStore() {
        Debug.print("TeamBean ejbStore");
    }
    
    public void ejbPassivate() { }
    public void ejbActivate() { }


} // TeamBean class
