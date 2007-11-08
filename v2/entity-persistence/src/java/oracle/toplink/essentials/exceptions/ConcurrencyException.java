/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.exceptions;

import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;

/**
 * <P><B>Purpose</B>: Concurrency deadlock or interupts will raise this exception.
 */
public class ConcurrencyException extends TopLinkException {
    public final static int WAIT_WAS_INTERRUPTED = 2001;
    public final static int WAIT_FAILURE_SERVER = 2002;
    public final static int WAIT_FAILURE_CLIENT = 2003;
    public final static int SIGNAL_ATTEMPTED_BEFORE_WAIT = 2004;
    public final static int WAIT_FAILURE_SEQ_DATABASE_SESSION = 2005;
    public final static int SEQUENCING_MULTITHREAD_THRU_CONNECTION = 2006;
    public final static int MAX_TRIES_EXCEDED_FOR_LOCK_ON_CLONE = 2007;
    public final static int MAX_TRIES_EXCEDED_FOR_LOCK_ON_MERGE = 2008;
    public final static int MAX_TRIES_EXCEDED_FOR_LOCK_ON_BUILD_OBJECT = 2009;
    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected ConcurrencyException(String theMessage) {
        super(theMessage);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected ConcurrencyException(String theMessage, Exception exception) {
        super(theMessage, exception);
    }

    public static ConcurrencyException maxTriesLockOnCloneExceded(Object objectToClone) {
        Object[] args = { objectToClone, CR };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, MAX_TRIES_EXCEDED_FOR_LOCK_ON_CLONE, args));
        concurrencyException.setErrorCode(MAX_TRIES_EXCEDED_FOR_LOCK_ON_CLONE);
        return concurrencyException;
    }

    public static ConcurrencyException maxTriesLockOnMergeExceded(Object objectToClone) {
        Object[] args = { objectToClone, CR };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, MAX_TRIES_EXCEDED_FOR_LOCK_ON_MERGE, args));
        concurrencyException.setErrorCode(MAX_TRIES_EXCEDED_FOR_LOCK_ON_MERGE);
        return concurrencyException;
    }

    public static ConcurrencyException maxTriesLockOnBuildObjectExceded(Thread cacheKeyThread, Thread currentThread) {
        Object[] args = { cacheKeyThread, currentThread, CR };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, MAX_TRIES_EXCEDED_FOR_LOCK_ON_MERGE, args));
        concurrencyException.setErrorCode(MAX_TRIES_EXCEDED_FOR_LOCK_ON_BUILD_OBJECT);
        return concurrencyException;
    }

    public static ConcurrencyException signalAttemptedBeforeWait() {
        Object[] args = { CR };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, SIGNAL_ATTEMPTED_BEFORE_WAIT, args));
        concurrencyException.setErrorCode(SIGNAL_ATTEMPTED_BEFORE_WAIT);
        return concurrencyException;
    }

    public static ConcurrencyException waitFailureOnClientSession(InterruptedException exception) {
        Object[] args = {  };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, WAIT_FAILURE_CLIENT, args), exception);
        concurrencyException.setErrorCode(WAIT_FAILURE_CLIENT);
        return concurrencyException;
    }

    public static ConcurrencyException waitFailureOnServerSession(InterruptedException exception) {
        Object[] args = {  };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, WAIT_FAILURE_SERVER, args), exception);
        concurrencyException.setErrorCode(WAIT_FAILURE_SERVER);
        return concurrencyException;
    }

    public static ConcurrencyException waitWasInterrupted(String message) {
        Object[] args = { CR, message };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, WAIT_WAS_INTERRUPTED, args));
        concurrencyException.setErrorCode(WAIT_WAS_INTERRUPTED);
        return concurrencyException;
    }

    public static ConcurrencyException waitFailureOnSequencingForDatabaseSession(InterruptedException exception) {
        Object[] args = {  };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, WAIT_FAILURE_SEQ_DATABASE_SESSION, args), exception);
        concurrencyException.setErrorCode(WAIT_FAILURE_SEQ_DATABASE_SESSION);
        return concurrencyException;
    }

    public static ConcurrencyException sequencingMultithreadThruConnection(String accessor) {
        Object[] args = { accessor };

        ConcurrencyException concurrencyException = new ConcurrencyException(ExceptionMessageGenerator.buildMessage(ConcurrencyException.class, SEQUENCING_MULTITHREAD_THRU_CONNECTION, args));
        concurrencyException.setErrorCode(SEQUENCING_MULTITHREAD_THRU_CONNECTION);
        return concurrencyException;
    }
}
