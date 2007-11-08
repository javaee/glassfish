/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.jdbc.stress1.ejb;

import java.util.*;
import javax.ejb.*;

public interface LocalLeague extends EJBLocalObject {
    public String getLeagueId();
    public String getName();
    public String getSport();
    public Collection getTeams();

    public void addTeam(LocalTeam team);
    public void dropTeam(LocalTeam team);
}
