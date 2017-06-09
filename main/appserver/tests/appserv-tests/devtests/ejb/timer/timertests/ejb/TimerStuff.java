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

package com.sun.s1asdev.ejb.timer.timertests;

import java.io.Serializable;
import java.util.Date;
import java.rmi.RemoteException;
import javax.ejb.*;

public interface TimerStuff {

    TimerHandle createTimer(long duration, String info) throws RemoteException, Exception;

    TimerHandle createTimer(long duration) throws RemoteException, Exception;
    TimerHandle createTimer(long duration, long interval) throws RemoteException, Exception;
    TimerHandle createTimer(long duration, long interval, String info) throws RemoteException, Exception;
    TimerHandle createTimer(Date expirationTime) throws RemoteException, Exception;
    TimerHandle createTimer(Date expirationTime, long interval) throws RemoteException, Exception;

    void createTimerAndRollback(long duration) throws RemoteException, Exception;

    void createTimerAndCancel(long duration) throws RemoteException, Exception;

    void createTimerAndCancelAndCancel(long duration) throws RemoteException, Exception;

    void createTimerAndCancelAndRollback(long duration) throws RemoteException, Exception;

    void cancelTimerNoError(TimerHandle timerHandle) throws RemoteException, Exception;
    void cancelTimer(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndRollback(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndCancel(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndCancelAndRollback(TimerHandle timerHandle) throws RemoteException, Exception;

    void getTimersTest() throws RemoteException, Exception;

    TimerHandle getTimeRemainingTest1(int numIterations) throws RemoteException, Exception;

    void  getTimeRemainingTest2(int numIterations, TimerHandle th) throws RemoteException, Exception;

    TimerHandle getNextTimeoutTest1(int numIterations) throws RemoteException, Exception;

    void  getNextTimeoutTest2(int numIterations, TimerHandle th) throws RemoteException, Exception;

    void assertNoTimers() throws RemoteException, Exception;

    Serializable getInfo(TimerHandle handle) throws RemoteException, Exception;
    Serializable getInfoNoError(TimerHandle handle) throws RemoteException, Exception;

    void assertTimerNotActive(TimerHandle handle) throws RemoteException;

    void sendMessageAndCreateTimer() throws RemoteException, Exception;
    void recvMessageAndCreateTimer(boolean expectMessage) 
        throws RemoteException, Exception;
    void sendMessageAndCreateTimerAndRollback() 
        throws RemoteException, Exception;
    void recvMessageAndCreateTimerAndRollback(boolean expectMessage) 
        throws RemoteException, Exception;
}
