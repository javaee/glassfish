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

public interface LocalTeam extends EJBLocalObject {
    public String getTeamId();
    public String getName();
    public String getCity();
    public Collection getPlayers();
    public LocalLeague getLeague();

    public ArrayList getCopyOfPlayers();
    public void addPlayer(LocalPlayer player);
    public void dropPlayer(LocalPlayer player);
}
