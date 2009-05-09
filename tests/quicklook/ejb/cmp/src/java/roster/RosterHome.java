/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package roster;

import java.io.Serializable;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface RosterHome extends EJBLocalHome {
 
    Roster create() throws CreateException;
}
