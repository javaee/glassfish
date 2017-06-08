/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.perf.local;

import javax.ejb.*;
import javax.naming.*;
import java.util.*;
import javax.rmi.PortableRemoteObject;

public class HelloEJB implements SessionBean {
    private static final int ITERATIONS = 10000;
    //private static final int ITERATIONS = 1;
    private SessionContext context;
    private Sful sful;
    private SfulRemote sfulRemote;
    private SfulRemoteHome sfulRemoteHome;


    private Sless sless;
    private SlessRemote slessRemote;
    private SlessRemoteHome slessRemoteHome;

    private Bmp bmp;
    private BmpRemote bmpRemote;
    private BmpRemoteHome bmpRemoteHome;

    private long overhead;

    javax.transaction.UserTransaction ut;

    public HelloEJB(){}

    public void ejbCreate() {

	try {
            System.out.println("Num iterations set to " + ITERATIONS);
            Context ic = new InitialContext();
	    SfulHome sfulHome = (SfulHome) ic.lookup("java:comp/env/ejb/Sful");
            sful = sfulHome.create();


            Object obj = ic.lookup("java:comp/env/ejb/SfulRemote");
            sfulRemoteHome = (SfulRemoteHome)
                PortableRemoteObject.narrow(obj, SfulRemoteHome.class);
            sfulRemote = sfulRemoteHome.create();
	    System.out.println("Created Stateful bean.");

	   	     
	    SlessHome slessHome = (SlessHome) ic.lookup("java:comp/env/ejb/Sless");
            sless = slessHome.create();
            obj = ic.lookup("java:comp/env/ejb/SlessRemote");
            slessRemoteHome = (SlessRemoteHome)
                PortableRemoteObject.narrow(obj, SlessRemoteHome.class);
            slessRemote = slessRemoteHome.create();
	    System.out.println("Created Stateless bean.");


	    BmpHome bmpHome = (BmpHome) ic.lookup("java:comp/env/ejb/Bmp");
            String pkey = "A BMP Bean";
            bmp = bmpHome.create(pkey);
            obj = ic.lookup("java:comp/env/ejb/BmpRemote");
            bmpRemoteHome = (BmpRemoteHome)
                PortableRemoteObject.narrow(obj, BmpRemoteHome.class);
            bmpRemote = (BmpRemote)
                bmpRemoteHome.findByPrimaryKey(pkey);
	    System.out.println("Created BMP bean.");
	   	     
	    ut = context.getUserTransaction();

	} catch (Exception ex) {
	    System.out.println("couldn't get sful bean");
	    ex.printStackTrace();
	}
    }


    public void warmup(int type, boolean local) throws Exception {
        if( local ) {
            warmupLocal(type, true);
            warmupLocal(type, false);
        } else {
            warmupRemote(type, true);
            warmupRemote(type, false);
        }

	// Measure looping and timing overhead 
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	}
	long end = System.currentTimeMillis();
	overhead = end - begin;
    }

    private void warmupLocal(int type, boolean tx) throws Exception {
	// get Hotspot warmed up	
	Common bean = preLocal(type, tx);
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.requiresNew();
	    bean.notSupported();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.required();
	    if ( tx )
		bean.mandatory();
	    else
		bean.never();
	    bean.supports();
	}
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {
          ex.printStackTrace();
        }
    }

    private void warmupRemote(int type, boolean tx) throws Exception {
	// get Hotspot warmed up	
	CommonRemote bean = preRemote(type, tx);
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.requiresNew();
	    bean.notSupported();
	}
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.required();
	    if ( tx )
		bean.mandatory();
	    else
		bean.never();
	    bean.supports();
	}
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {
          ex.printStackTrace();
        }
    }

    private Common preLocal(int type, boolean tx) 
    {
	if ( tx ) try { ut.begin(); } catch ( Exception ex ) {
            ex.printStackTrace();
        }
	if ( type == Common.STATELESS )
	    return sless;
	else if ( type == Common.STATEFUL )
	    return sful;
	else
	    return bmp;
    }

    private CommonRemote preRemote(int type, boolean tx) 
    {
	if ( tx ) try { ut.begin(); } catch ( Exception ex ) {
            ex.printStackTrace();
        }
	if ( type == Common.STATELESS )
	    return slessRemote;
	else if ( type == Common.STATEFUL )
	    return sfulRemote;
	else
	    return bmpRemote;
    }


    private float post(long begin, long end, boolean tx)
    {
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {
            ex.printStackTrace();
        }
	return (float)( ((double)(end-begin-overhead))/((double)ITERATIONS) * 1000.0 );
    }

    public float requiresNew(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.requiresNew();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float notSupported(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.notSupported();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float required(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.required();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float mandatory(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.mandatory();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float never(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.never();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float supports(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.supports();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }


    public float requiresNewRemote(int type, boolean tx) throws Exception
    {
	CommonRemote bean = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.requiresNew();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float notSupportedRemote(int type, boolean tx) throws Exception
    {
	CommonRemote bean = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.notSupported();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float requiredRemote(int type, boolean tx) throws Exception
    {
	CommonRemote bean = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.required();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float mandatoryRemote(int type, boolean tx) throws Exception
    {
	CommonRemote bean = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.mandatory();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float neverRemote(int type, boolean tx) throws Exception
    {
	CommonRemote bean = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.never();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float supportsRemote(int type, boolean tx) throws Exception
    {
	CommonRemote bean = preRemote(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<ITERATIONS; i++ ) {
	    bean.supports();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public void setSessionContext(SessionContext sc) {
	context = sc;
    }

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
