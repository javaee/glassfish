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
import javax.ejb.*;
import javax.naming.*;
import com.sun.s1peqe.ejb.cmp.roster.util.Debug;

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


    // Business methods
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
}
