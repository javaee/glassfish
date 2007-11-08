/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.enterprise.iiop;

// NOTE: This is the EXACT same file as in corba/folb unit test,
// except for parts commented out.
// REVISIT: Need to use from a direct copy of AS version.
// Just update Ant to build and use it AND provide dummy logging code.

//package corba.folb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;

import com.sun.corba.ee.pept.transport.ContactInfo;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.CorbaContactInfoList;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.corba.ee.impl.orbutil.ORBUtility;

/**
 * This is the "sticky manager" - based on the 7.1 EE concept.
 * @author Harold Carr
 */
public class IIOPPrimaryToContactInfoImpl 
    implements IIOPPrimaryToContactInfo
{

    // REVISIT - log messages must be internationalized.

    private static Logger _logger = null;
    static {
       _logger = LogDomains.getLogger(LogDomains.CORBA_LOGGER);
    }

    /*
    private static MyLogger _logger = new MyLogger();
    */

    public final String baseMsg = IIOPPrimaryToContactInfoImpl.class.getName();

    private Map map;
    private boolean debugChecked;
    private boolean debug;

    public IIOPPrimaryToContactInfoImpl()
    {
	map = new HashMap();
	debugChecked = false;
	debug = false;
    }

    public synchronized void reset(ContactInfo primary)
    {
	try {
	    if (debug) {
		dprint(".reset: " + getKey(primary));
	    }
	    map.remove(getKey(primary));
	} catch (Throwable t) {
            _logger.log(Level.WARNING,
			"Problem in " + baseMsg + ".reset",
			t);
	    RuntimeException rte =
		new RuntimeException(baseMsg + ".reset error");
	    rte.initCause(t);
	    throw rte;
	}
    }

    public synchronized boolean hasNext(ContactInfo primary,
					ContactInfo previous,
					List contactInfos)
    {
	try {
	    if (! debugChecked) {
		debugChecked = true;
		debug = ((ORB)primary.getBroker()).transportDebugFlag 
		        || _logger.isLoggable(Level.FINE);
	    }

	    if (debug) {
		dprint(".hasNext->: " 
		       + formatKeyPreviousList(getKey(primary),
					       previous,
					       contactInfos));
	    }
	    boolean result;
	    if (previous == null) {
		result = true;
	    } else {
		int previousIndex = contactInfos.indexOf(previous);
		int contactInfosSize = contactInfos.size();
		if (debug) {
		    dprint(".hasNext: " 
			   + previousIndex + " " + contactInfosSize);
		}
		if (previousIndex < 0) {
		    // This SHOULD not happen.
		    // It would only happen if the previous is NOT
		    // found in the current list of contactInfos.
		    RuntimeException rte = new RuntimeException(


			"Problem in " + baseMsg + ".hasNext: previousIndex: "
			+ previousIndex);

		    _logger.log(Level.SEVERE, 
			"Problem in " + baseMsg + ".hasNext: previousIndex: "
			+ previousIndex, rte);
		    throw rte;
		} else {
		    // Since this is a retry, ensure that there is a following
		    // ContactInfo for .next
		    result = (contactInfosSize - 1) > previousIndex;
		}
	    }
	    if (debug) {
		dprint(".hasNext<-: " + result);
	    }
	    return result;
	} catch (Throwable t) {
            _logger.log(Level.WARNING, 
			"Problem in " + baseMsg + ".hasNext",
			t);
	    RuntimeException rte =
		new RuntimeException(baseMsg + ".hasNext error");
	    rte.initCause(t);
	    throw rte;
	}
    }

    public synchronized ContactInfo next(ContactInfo primary,
					 ContactInfo previous,
					 List contactInfos)
    {
	try {
	    String debugMsg = null;

	    if (debug) {
		debugMsg = "";
		dprint(".next->: " 
		       + formatKeyPreviousList(getKey(primary),
					       previous,
					       contactInfos));
		dprint(".next: map: " + formatMap(map));
	    }

	    Object result = null;

	    if (previous == null) {
		// This is NOT a retry.
		result = map.get(getKey(primary));
		if (result == null) {
		    if (debug) {
			debugMsg = ".next<-: initialize map: ";
		    }
		    // NOTE: do not map primary to primary.
		    // In case of local transport we NEVER use primary.
		    result = contactInfos.get(0);
		    map.put(getKey(primary), result);
		} else {
		    if (debug) {
			dprint(".next: primary mapped to: " + result);
		    }
		    int position = contactInfos.indexOf(result);
		    if (position == -1) {
			// It is possible that communication to the key
			// took place on SharedCDR, then a corbaloc to 
			// same location uses a SocketOrChannelContactInfo
			// and vice versa.
			if (debug) {
			    dprint(".next: cannot find mapped entry in current list.  Removing mapped entry and trying .next again.");
			}
			reset(primary);
			return next(primary, previous, contactInfos);
		    }
		    // NOTE: This step is critical.  You do NOT want to
		    // return contact info from the map.  You want to find
		    // it, as a SocketInfo, in the current list, and then
		    // return that ContactInfo.  Otherwise you will potentially
		    // return a ContactInfo pointing to an incorrect IOR.
		    result = contactInfos.get(position);
		    if (debug) {
			debugMsg = ".next<-: mapped: ";
		    }
		}
	    } else {
		// This is a retry.
		// If previous is last element then .next is not called
		// because hasNext will return false.
		result = contactInfos.get(contactInfos.indexOf(previous) + 1);
		map.put(getKey(primary), result);

		_logger.log(Level.INFO, "IIOP failover to: " + result);

		if (debug) {
		    debugMsg = ".next<-: update map: " 
			+ " " + contactInfos.indexOf(previous)
			+ " " + contactInfos.size() + " ";
		}
	    }
	    if (debug) {
		dprint(debugMsg + result);
	    }
	    return (ContactInfo) result;
	} catch (Throwable t) {
            _logger.log(Level.WARNING,
			"Problem in " + baseMsg + ".next",
			t);
	    RuntimeException rte =
		new RuntimeException(baseMsg + ".next error");
	    rte.initCause(t);
	    throw rte;
	}
    }

    private Object getKey(ContactInfo contactInfo)
    {
	if (((SocketInfo)contactInfo).getPort() == 0) {
	    // When CSIv2 is used the primary will have a zero port.
	    // Therefore type/host/port will NOT be unique.
	    // So use the entire IOR for the key in that case.
	    return ((CorbaContactInfoList)contactInfo.getContactInfoList())
		.getEffectiveTargetIOR();
	} else {
	    return contactInfo;
	}
    }

    private String formatKeyPreviousList(Object key,
					 ContactInfo previous, List list)
    {
	String result =
	      "\n  key     : " + key
	    + "\n  previous: " + previous
	    + "\n  list:";
	Iterator i = list.iterator();
	int count = 1;
	while (i.hasNext()) {
	    result += "\n    " + count++ + "  " + i.next();
	}
	return result;
    }

    private String formatMap(Map map)
    {
	String result = "";
	synchronized (map) {
	    Iterator i = map.entrySet().iterator();
	    if (! i.hasNext()) {
		return "empty";
	    }
	    while (i.hasNext()) {
		Map.Entry entry = (Map.Entry) i.next();
		result += 
		      "\n    key  : " + entry.getKey()
		    + "\n    value: " + entry.getValue()
		    + "\n";
	    }
	}
	return result;
    }

    private void dprint(String msg)
    {
	/*
	ORBUtility.dprint("IIOPPrimaryToContactInfoImpl", msg);
	*/
	_logger.log(Level.FINE, msg);
    }
}

/*
class MyLogger
{
    void log(Level level, String msg, Throwable t)
    {
	ORBUtility.dprint("IIOPPrimaryToContactInfoImpl.MyLogger.log", msg);
	t.printStackTrace(System.out);
    }
}
*/

// End of file.
