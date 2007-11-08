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

//
// Copyright 2001 iPlanet/ Sun Microsystems, Inc. All Rights Reserved.
//
// Author : darpan
// Module : Utils
//
package com.sun.enterprise.util.scheduler;

/**
* PeriodicallyServicable inherits from the <I>Servicable</I> interface and
* enable utilities such as PeriodicEventScheduler to give a callback to the
* objects on their service methods.<br>
* Ensure that the objects implementing PeriodicallyServicable do not hog the
* threads for execution. Logic like inserting a task onto the writer thread
* queues is best suited for this purpose.
* <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/scheduler/PeriodicallyServicable.java,v $</I>
* @author     $Author: tcfujii $
* @version    1.0 $Revision: 1.3 $ $Date: 2005/12/25 04:12:29 $
* @see com.sun.enterprise.util.threadpool.Servicable
* @see PeriodicEventScheduler
*/
public interface PeriodicallyServicable
    extends com.sun.enterprise.util.threadpool.Servicable
{
    /**
    * Get the frequency (time interval) at which service() method will be invoked.
    * @return time in milli seconds.
    */
    public long getFrequency();
    
    /**
    * Determine to execute the service method of this object even if it has 
    * missed the right schedule.
    */
    public boolean getExecuteIfMissed();
    
    /**
    * Determine to execute the service method of this object when the 
    * schedule is delayed by 'missedByMillis' milli seconds.
    */
    public boolean getExecutionTolerance(long missedByMillis);
    
    /**
    * Print an identification for the object.
    */
    public String toString();
}
