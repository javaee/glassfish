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
import javax.naming.*;
import com.sun.s1asdev.jdbc.stress1.util.Debug;
import com.sun.s1asdev.jdbc.stress1.util.PlayerDetails;

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

    // Business methods

    public ArrayList getCopyOfPlayers() {
        //Debug.print("TeamBean getCopyOfPlayers");
        ArrayList playerList = new ArrayList();
        Collection players = getPlayers();

        Iterator i = players.iterator();
        while (i.hasNext()) {
            LocalPlayer player = (LocalPlayer) i.next();
            PlayerDetails details = new PlayerDetails(player.getPlayerId(),
                                                      player.getName(),
                                                      player.getPosition(),
                                                      0.00);
            playerList.add(details);
        }
        return playerList;
    }

    public void addPlayer(LocalPlayer player) {
        //Debug.print("TeamBean addPlayer");
        try {
            Collection players = getPlayers();
            players.add(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropPlayer(LocalPlayer player) {
        //Debug.print("TeamBean dropPlayer");
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

        //Debug.print("TeamBean ejbCreate");
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
        //Debug.print("TeamBean ejbRemove");
    }
    
    public void ejbLoad() {
        //Debug.print("TeamBean ejbLoad");
    }
    
    public void ejbStore() {
        //Debug.print("TeamBean ejbStore");
    }
    
    public void ejbPassivate() { }
    public void ejbActivate() { }
}
