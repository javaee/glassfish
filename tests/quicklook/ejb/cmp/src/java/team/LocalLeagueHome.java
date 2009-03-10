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

public interface LocalLeagueHome extends EJBLocalHome {
    
    public LocalLeague create (String id, String name, String sport)
        throws CreateException;
    
    public LocalLeague findByPrimaryKey (String id)
        throws FinderException;

    public Collection findAll() 
        throws FinderException;

    public LocalLeague findByName(String name)
        throws FinderException;					
        
}