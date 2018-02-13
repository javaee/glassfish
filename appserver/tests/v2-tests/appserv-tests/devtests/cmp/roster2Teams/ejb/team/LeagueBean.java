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

package team;

import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import util.Debug;

public abstract class LeagueBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields

    public abstract String getLeagueId();
    public abstract void setLeagueId(String id);
    
    public abstract String getName();
    public abstract void setName(String name);

    public abstract String getSport();
    public abstract void setSport(String sport);


    // Access methods for relationship fields

    public abstract Collection getTeams();
    public abstract void setTeams(Collection teams);

    // Select methods

    public abstract Set ejbSelectTeamsCity(LocalLeague league)
        throws FinderException;

    public abstract Set ejbSelectRemoteTeams(League league)
        throws FinderException;

        
    public abstract LocalTeam ejbSelectTeamByCity(String city)
        throws FinderException;

    public abstract Team ejbSelectRemoteTeamByCity(String city)
        throws FinderException;

    public abstract String ejbSelectTeamsNameByCity(String city)
        throws FinderException;

        
    public abstract Set ejbSelectPlayersByLeague(LocalLeague league)
        throws FinderException;

    public abstract Collection ejbSelectRemotePlayersByLeague(League league)
        throws FinderException;
        
    // Business methods

    public Set getCitiesOfThisLeague() throws FinderException {
         
         LocalLeague league = 
             (team.LocalLeague)context.getEJBLocalObject();
         
         return ejbSelectTeamsCity(league); 
    }


    public Set getRemoteTeamsOfThisLeague() throws FinderException {
         
         League league =  (team.League)context.getEJBObject();
         
         return ejbSelectRemoteTeams(league); 
    }

    
    public LocalTeam getTeamByCity(String city) throws FinderException {
        
        return ejbSelectTeamByCity(city);
    }

    public Team getRemoteTeamByCity(String city) throws FinderException {
        
        return ejbSelectRemoteTeamByCity(city);
    }
    
    public String getTeamsNameByCity(String city) throws FinderException {
        
        return ejbSelectTeamsNameByCity(city);
    }

    
    public Set getPlayersFromLeague() throws FinderException{
        
        LocalLeague league = (team.LocalLeague)context.getEJBLocalObject();
        
        return ejbSelectPlayersByLeague(league);
    } 
    
    public Collection getRemotePlayersFromLeague() throws FinderException{
        
        League league = (team.League)context.getEJBObject();
        
        return ejbSelectRemotePlayersByLeague(league);
    } 
        
    
    public void addTeam(LocalTeam team) {

        Debug.print("TeamBean addTeam");
        try {
            Collection teams = getTeams();
            teams.add(team);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropTeam(LocalTeam team) {

        Debug.print("TeamBean dropTeam");
        try {
            Collection teams = getTeams();
            teams.remove(team);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    // EntityBean  methods

    public String ejbCreate (String id, String name, String sport)
        throws CreateException {

        Debug.print("LeagueBean ejbCreate");
        setLeagueId(id);
        setName(name);
        setSport(sport);
        return null;
    }
         
    public void ejbPostCreate (String id, String name, String sport)
        throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }
    
    public void unsetEntityContext() {
        context = null;
    }
    
    public void ejbRemove() {
        Debug.print("LeagueBean ejbRemove");
    }
    
    public void ejbLoad() {
        Debug.print("LeagueBean ejbLoad");
    }
    
    public void ejbStore() {
        Debug.print("LeagueBean ejbStore");
    }
    
    public void ejbPassivate() { }
    
    public void ejbActivate() { }

} // LeagueBean class
