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

//NOTE: Tabs are used instead of spaces for indentation. 
//  Make sure that your editor does not replace tabs with spaces. 
//  Set the tab length using your favourite editor to your 
//  visual preference.

/*
 * Filename: ApproximateClock.java	
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
 
/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/ApproximateClock.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:00 $
 */
 
package com.sun.enterprise.util;

/**
 * ApproximateClock returns the approximate time instead of
 *	calling (the expensive) System.currentTimeMillis().
 *	The method getTime() returns the approximate time. The correctness
 *	of the time depends on the delay that is passed in the constructor.
 *	A background thread updates the time every 'delay' milliseconds.
 *	The exact time can still be obtained by using the getActualTime()
 *	method.
 */
//Bug 
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 
public class ApproximateClock
	implements Runnable	
{
//Bug 
	static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug
	private long					sleepTime = 1000;
	private long					now;
	private Thread					timerThread;
	private long[]                  time;
	private int                     index = 0;
	private int 					nextIndex;
	
	/**
	 * Creates an ApproximateClock.
	 * @param delay The time interval to update time
	 */
	public ApproximateClock (long delay) {
		setDelay(delay);
		timerThread = new Thread(this, "ApproximateClock");
		timerThread.setDaemon(true);
		time = new long[2];
		time[0] = System.currentTimeMillis();
		timerThread.start();
	}
	
	/**
	 * Returns the actual/correct time. Same as calling System.currentTimeMillis()
	 * @return The current time (Same as System.currentTimeMillis())
	 */
    public long getActualTime() {
		nextIndex = 1 - index;
		time[nextIndex] = System.currentTimeMillis();
		index = nextIndex;
		return time[index];
    }
    
	/**
	 * Returns the approximate time. Returns the time that was updated
	 *	(utmost) 'delay' milliseconds earlier.
	 * @return The approximate time
	 */
    public long getTime() {
    	return time[index];
    }
    
    /**
     * Set the delay for updating the time.
     * @param The delay (in milliseconds) between updates.
     */
    public void setDelay(long delay) {
    	sleepTime = (delay < 1) ? 1 : delay;
    }
    
    /**
     * Returns the delay
     * @return The delay (in milliseconds) between updates.
     */
    public long getDelay() {
    	return sleepTime;
    }
    
    /**
     * The thread's run method to update the time periodically
     */
    public final void run() {
    	while (true) {
    		try {
    			getActualTime();
    			Thread.sleep(sleepTime);
    		} catch (InterruptedException inEx) {
    			break;
    		}
    	}
    }
    
    public static void main(String[] args)
    	throws Exception
    {
    	int count = 100000000;
    	
    	long time=0, t1=0, t2 = 0;
    	    	
    	t1 = System.currentTimeMillis();
    	for (int i=0; i<count; i++) {
    		time = System.currentTimeMillis();
    	}
    	t2 = System.currentTimeMillis();
//Bug    	System.out.println("sys.currentTimeMillis() took: " + ((t2 - t1) / 1000.0) + " seconds " + time);
	_logger.log(Level.FINE,"sys.currentTimeMillis() took: " + ((t2 - t1) / 1000.0) + " seconds " + time);
    	
    	t1 = System.currentTimeMillis();
    	ApproximateClock clock = new ApproximateClock(3000);
    	for (int i=0; i<count; i++) {
    		time = clock.getTime();
    	}
    	t2 = System.currentTimeMillis();
//Bug    	System.out.println("clcok.getTime() took: " + ((t2 - t1) / 1000.0) + " seconds " + time);
	_logger.log(Level.FINE,"clock.getTime() took: " + ((t2 - t1) / 1000.0) + " seconds " + time);
    	
    }
    
}

