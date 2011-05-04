/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 *
 */

package team;

import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface LocalTeamHome extends EJBLocalHome {
    
    public LocalTeam create (String id, String name, String city)
        throws CreateException;
    
    public LocalTeam findByPrimaryKey (String id)
        throws FinderException;

    public Collection findAll() 
        throws FinderException;
    
    public Collection findByPlayerAndLeague(LocalPlayer player, 
                                            LocalLeague league)
                                            throws FinderException;
}
