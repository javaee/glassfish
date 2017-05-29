/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import javax.ejb.TimedObject;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.rmi.RemoteException;

public class TimerSFSBEJB
    implements SessionBean 
{
	private SessionContext context;
    private String timerName;
    private Context initialCtx;
    private Timer timer;

	public void ejbCreate(String timerName) {
        this.timerName = timerName;
    }

    public String getName() {
        return this.timerName;
    }

	public void setSessionContext(SessionContext sc) {
		this.context = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
	}

	// business method to create a timer
	public void createTimer(int ms)
        throws RemoteException
    {
        try {
            InitialContext initialCtx = new InitialContext();
            TimerSLSBHome home = (TimerSLSBHome) initialCtx.lookup("java:comp/env/ejb/TimerSLSB");
		    TimerSLSB slsb = (TimerSLSB) home.create();
		    timer = slsb.createTimer(ms);
            System.out.println ("PG-> after createTimer()");
        } catch (Exception ex) {
            throw new RemoteException("Exception during TimerSFSBEJB::createTimer", ex);
        }
	}

	public long getTimeRemaining() {
            long timeRemaining = -1;
            try {
                timeRemaining = timer.getTimeRemaining();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
            return timeRemaining;
        }

	public TimerHandle getTimerHandle() {
            TimerHandle handle = null;
            try {
                handle = timer.getHandle();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
            return handle;
        }

	public void cancelTimer() {
            try {
                timer.cancel();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
    }


	public void ejbRemove() {}

	public void ejbActivate() {
        System.out.println ("In TimerSFSB.activate()");
    }

	public void ejbPassivate() {
        System.out.println ("In TimerSFSB.passivate()");
    }
}
