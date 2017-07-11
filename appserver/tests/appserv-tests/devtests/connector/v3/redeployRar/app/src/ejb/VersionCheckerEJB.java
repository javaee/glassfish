/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.naming.*;
import com.sun.jdbcra.spi.JdbcSetupAdmin;

public class VersionCheckerEJB implements SessionBean {
	private InitialContext initContext = null;
	private SessionContext sessionContext = null;


    public VersionCheckerEJB() {
    debug("Constructor");
    }

    public void ejbCreate() 
        throws CreateException {
		debug("ejbCreate()");
    }


    public int getVersion(){
	    try {
	      initContext = new javax.naming.InitialContext();
	    } catch (Exception e) {
	      System.out.println("Exception occured when creating InitialContext: " + e.toString());
	      return -1;
	    }
	
	    try {
	      JdbcSetupAdmin ja = (JdbcSetupAdmin) initContext.lookup("eis/jdbcAdmin");
	      int versionno =  ja.getVersion();
	      debug("Version number is " + versionno);
	      return versionno;
	    } catch (Exception e) {
	      e.printStackTrace();
	      throw new RuntimeException(e.getMessage());
	    }
    }


    public void setSessionContext(SessionContext context) {
	    debug(" bean removed");
        sessionContext = context;
    }

    public void ejbRemove() {
	    debug(" bean removed");
    }

    public void ejbActivate() {
            debug(" bean activated");
    }

    public void ejbPassivate() {
            debug(" bean passivated");
    }


    private void debug(String msg) {
        debug("[VersionCheckerEJB]:: -> " + msg);
    }
}
