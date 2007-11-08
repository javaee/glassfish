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
package com.sun.enterprise.util;

import java.io.IOException;
import java.util.*;
import javax.transaction.SystemException;
import com.sun.enterprise.Switch;
import com.sun.enterprise.J2EETransactionManager;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.InvocationException;
import com.sun.enterprise.SecurityManager;
import org.apache.catalina.Realm;
import org.apache.catalina.Context;
import com.sun.web.security.RealmAdapter;
import com.sun.ejb.Container;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742


/**
 * Implementation of InvocationManager. Use ThreadLocal variable
 * to keep track of per thread data
 *
 * @author Tony Ng
 * @author Harpreet Singh
 */
public class InvocationManagerImpl implements InvocationManager {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    // END OF IASRI 4660742 

    // START OF IASRI 4679641
    // static public boolean debug = false;
    static public boolean debug = com.sun.enterprise.util.logging.Debug.enabled;
    // END OF IASRI 4679641

    static private LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(InvocationManagerImpl.class);

    // This TLS variable stores an ArrayList. 
    // The ArrayList contains ComponentInvocation objects which represent
    // the stack of invocations on this thread. Accesses to the ArrayList
    // dont need to be synchronized because each thread has its own ArrayList.
    private InheritableThreadLocal frames;   

    private J2EETransactionManager tm;

    public InvocationManagerImpl() {
        frames = new InheritableThreadLocal() {
            protected Object initialValue() {
                return new InvocationArray();
            }

            // if this is a thread created by user in servlet's service method
            // create a new ComponentInvocation with transaction
            // set to null and instance set to null
            // so that the resource won't be enlisted or registered
            protected Object childValue(Object parentValue) {
                // always creates a new ArrayList
                InvocationArray result = new InvocationArray();
                InvocationArray v = (InvocationArray) parentValue;
                if (v.size() > 0 && v.outsideStartup()) {
                    // get current invocation
                    ComponentInvocation parentInv = 
                        (ComponentInvocation) v.get(v.size()-1);
                    if (parentInv.getInvocationType() == 
                        parentInv.SERVLET_INVOCATION) {

                        ComponentInvocation inv = 
                            new ComponentInvocation(null,
                                            parentInv.getContainerContext());
                        result.add(inv);
                    } else if (parentInv.getInvocationType() != parentInv.EJB_INVOCATION) {
			// Push a copy of invocation onto the new result ArrayList
			ComponentInvocation cpy = 
			    new ComponentInvocation 
			    ( parentInv.getInstance(), 
			      parentInv.getContainerContext());
			cpy.setTransaction (parentInv.getTransaction());
			result.add(cpy);
		    }
		    
                }
                return result;
            }
        };
    }

    public void preInvoke(ComponentInvocation inv) throws InvocationException {

        // START OF IASRI 4660742
        if (debug && _logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"IM: preInvoke" + inv.instance);
        }
        // END OF IASRI 4660742

	int invType = inv.getInvocationType();

	// Get this thread's ArrayList
        InvocationArray v = (InvocationArray) frames.get();
        if (invType == ComponentInvocation.SERVICE_STARTUP) {
            v.setInvocationAttribute(ComponentInvocation.SERVICE_STARTUP);
            return;
        }

	// if ejb call EJBSecurityManager, for servlet call RealmAdapter
	if (invType  == inv.EJB_INVOCATION) {
	    SecurityManager sm =
		((Container)inv.getContainerContext()).getSecurityManager();
	    sm.preInvoke(inv);
	} else if (invType == inv.SERVLET_INVOCATION){
	    Realm rlm = ((Context)inv.getContainerContext()).getRealm();
	    if (rlm instanceof RealmAdapter) {
		RealmAdapter rad = (RealmAdapter) rlm;
		rad.preSetRunAsIdentity(inv);
	    }
	}

	// push this invocation on the stack
        v.add(inv);

	// Get the previous invocation on the stack
        int size = v.size();
	ComponentInvocation prev;
        if (size < 2) 
	    prev = null;
	else 
	    prev = (ComponentInvocation) v.get(size - 2);

	// Call the TM
        if (tm == null)
            tm = Switch.getSwitch().getTransactionManager();
        tm.preInvoke(prev);
    }

    public void postInvoke(ComponentInvocation inv) throws InvocationException
    {
        // START OF IASRI 4660742
        if (debug && _logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"IM: postInvoke" + inv.instance);
        }
        // END OF IASRI 4660742

        int invType = inv.getInvocationType();

	// Get this thread's ArrayList
	InvocationArray v = (InvocationArray) frames.get();
        if (invType == ComponentInvocation.SERVICE_STARTUP) {
            v.setInvocationAttribute(ComponentInvocation.UN_INITIALIZED);
            return;
        }

	int size = v.size();
	if (size == 0) 
	    throw new InvocationException();

        try {
	    // if ejb call EJBSecurityManager, for servlet call RealmAdapter
	    if (invType == inv.EJB_INVOCATION){
		SecurityManager sm = 
		    ((Container)inv.getContainerContext()).getSecurityManager();
		
		sm.postInvoke(inv);
	    } else if (invType == inv.SERVLET_INVOCATION){
		Realm rlm = ((Context)inv.getContainerContext()).getRealm();
		if (rlm instanceof RealmAdapter) {
		    RealmAdapter rad = (RealmAdapter) rlm;
		    rad.postSetRunAsIdentity (inv);
		}// else {
// 		    throw new InvocationException();
// 		}
	    }

	    // Get current and previous ComponentInvocation objects
	    ComponentInvocation prev, curr;
	    if (size < 2) 
		prev = null;
	    else 
		prev = (ComponentInvocation)v.get(size - 2);
	    curr = (ComponentInvocation)v.get(size - 1);

	    tm.postInvoke(curr, prev);

            Switch.getSwitch().getPoolManager().postInvoke();
	} 
	finally {
	    // pop the stack
	    v.remove(size - 1);
	}
    }

    /**
     * return true iff no invocations on the stack for this thread
     */
    public boolean isInvocationStackEmpty() {
        ArrayList v = (ArrayList) frames.get();
        return (v.size() == 0);
    }

    // BEGIN IASRI# 4646060
    /**
     * return the Invocation object of the component
     * being called
     */
    public ComponentInvocation getCurrentInvocation() {
    // END IASRI# 4646060

        ArrayList v = (ArrayList) frames.get();
	int size = v.size();
        // BEGIN IASRI# 4646060
        if (size == 0) return null;
        // END IASRI# 4646060
        return (ComponentInvocation) v.get(size-1);
    }

    /**
     * return the Inovcation object of the caller
     * return null if none exist (e.g. caller is from
     * another VM)
     */
    public ComponentInvocation getPreviousInvocation() 
        throws InvocationException {

        ArrayList v = (ArrayList) frames.get();
        int i = v.size();
        if (i < 2) return null;
        return (ComponentInvocation) v.get(i - 2);
    }

    public List getAllInvocations() {
        return (ArrayList) frames.get();
    }

    public boolean isStartupInvocation() {
        if (frames != null) {
            InvocationArray v = (InvocationArray) frames.get();
            if (v != null) {
                  return v.outsideStartup() == false;
            }
        }

        return false;
    }


    class InvocationArray extends java.util.ArrayList {
        private int invocationAttribute;
        
        public void setInvocationAttribute (int attribute) {
            this.invocationAttribute = attribute;
        }

        public int getInvocationAttribute() {
            return invocationAttribute;
        }

        public boolean outsideStartup() {
            return getInvocationAttribute() 
            != ComponentInvocation.SERVICE_STARTUP;
        }
    }
}






