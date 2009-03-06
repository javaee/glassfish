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

public interface LocalTeam extends EJBLocalObject {

    public String getTeamId();
    public String getName();
    public String getCity();
    public Collection getPlayers();
    public LocalLeague getLeague();

    public ArrayList getCopyOfPlayers();
    public void addPlayer(LocalPlayer player);
    public void dropPlayer(LocalPlayer player);
    public double getSalaryOfPlayer(String playerName);
    public String getTeamNameWithStringfunctionTests1(); 
    public String getTeamNameWithStringfunctionTests2(); 
    public String getTeamNameWithStringfunctionTests3(); 
    
}
