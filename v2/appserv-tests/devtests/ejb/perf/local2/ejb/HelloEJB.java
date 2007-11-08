

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local2;

import javax.ejb.*;
import javax.naming.*;
import java.util.*;

public class HelloEJB implements SessionBean {
    private int iterations;

    private SessionContext context;

    private SfulHome sfulHome;
    private Sful sful;

    private SlessHome slessHome;
    private Sless sless;

    private String pkey = "A BMP Bean";
    private BmpHome bmpHome;
    private Bmp bmp;

    private long overhead;

    javax.transaction.UserTransaction ut;

    public HelloEJB(){}

    public void ejbCreate(int numIterations) {

	try {
            iterations = numIterations;
            System.out.println("Num iterations set to " + iterations);
            Context ic = new InitialContext();
	    sfulHome = (SfulHome) ic.lookup("java:comp/env/ejb/Sful");
            sful = sfulHome.create();

	    System.out.println("Created Stateful bean.");

	   	     
	    slessHome = (SlessHome) ic.lookup("java:comp/env/ejb/Sless");
            sless = slessHome.create();
	    System.out.println("Created Stateless bean.");


	    bmpHome = (BmpHome) ic.lookup("java:comp/env/ejb/Bmp");
            
            bmp = bmpHome.create(pkey);
	    System.out.println("Created BMP bean.");
	   	     
	    ut = context.getUserTransaction();

	} catch (Exception ex) {
	    System.out.println("couldn't get sful bean");
	    ex.printStackTrace();
	}
    }


    public float createAccessRemove(int type, boolean tx) 
        throws Exception {

       	long begin = System.currentTimeMillis();

        for ( int i=0; i<iterations; i++ ) {
            Common bean = preLocal(type, tx, true);
            bean.required();
            bean.remove();
        }
        
        long end = System.currentTimeMillis();
        return post(begin, end, false);
    }

    public void warmup(int type) throws Exception {
        warmupLocal(type, true);
        warmupLocal(type, false);

	// Measure looping and timing overhead 
	long begin = System.currentTimeMillis();
	for ( int i=0; i<iterations; i++ ) {
	}
	long end = System.currentTimeMillis();
	overhead = end - begin;
    }

    private void warmupLocal(int type, boolean tx) throws Exception {
	// get Hotspot warmed up	
	Common bean = preLocal(type, tx);
	for ( int i=0; i<iterations; i++ ) {
	    bean.requiresNew();
	    bean.notSupported();
	}
	for ( int i=0; i<iterations; i++ ) {
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
        return preLocal(type, tx, false);
    }

    private Common preLocal(int type, boolean tx, boolean createNew) 
    {
	if ( tx ) try { ut.begin(); } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        try {
            if ( type == Common.STATELESS ) {
                return createNew ? slessHome.create() : sless;
            } else if ( type == Common.STATEFUL ) {
                return createNew ? sfulHome.create() : sful;
            } else {
                return createNew ? bmpHome.create(pkey) : bmp;
            }
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }

    private float post(long begin, long end, boolean tx)
    {
	if ( tx ) try { ut.commit(); } catch ( Exception ex ) {
            ex.printStackTrace();
        }
	return (float)( ((double)(end-begin-overhead))/((double)iterations) * 1000.0 );
    }

    public float requiresNew(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<iterations; i++ ) {
	    bean.requiresNew();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float notSupported(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<iterations; i++ ) {
	    bean.notSupported();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float required(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<iterations; i++ ) {
	    bean.required();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float mandatory(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<iterations; i++ ) {
	    bean.mandatory();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float never(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<iterations; i++ ) {
	    bean.never();
	}
	long end = System.currentTimeMillis();
	return post(begin, end, tx);
    }

    public float supports(int type, boolean tx) 
    {
	Common bean = preLocal(type, tx);
	long begin = System.currentTimeMillis();
	for ( int i=0; i<iterations; i++ ) {
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
