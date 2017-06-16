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

public interface LocalTeamHome extends EJBLocalHome {
    public LocalTeam create (String id, String name, String city)
        throws CreateException;
    
    public LocalTeam findByPrimaryKey (String id)
        throws FinderException;
}
