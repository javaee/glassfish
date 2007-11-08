/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.utility;

import com.sun.persistence.utility.logging.Logger;
import com.sun.org.apache.jdo.util.I18NHelper;

import java.util.ResourceBundle;

/**
 * Implements a simple semaphore that does not do <em>any</em> semaphore-ing.
 * That is, the methods just immediately return.
 * @author Dave Bristor
 */
// db13166: I would rather we use Doug Lea's stuff, but don't want to
// introduce that magnitude of change at this point in time.
public class NullSemaphore implements Semaphore {
    /**
     * Where to log messages about locking operations
     */
    private static final Logger _logger = LogHelperUtility.getLogger();

    /**
     * For logging, indicates on whose behalf locking is done.
     */
    private final String _owner;

    /**
     * I18N message handler
     */
    private final static I18NHelper messages = I18NHelper.getInstance(
            SemaphoreImpl.class);

    public NullSemaphore(String owner) {
        _owner = owner;

        if (_logger.isLoggable(Logger.FINEST)) {
            Object[] items = new Object[]{_owner};
            _logger.finest("utility.nullsemaphore.constructor", items); // NOI18N
        }
    }

    /**
     * Does nothing.
     */
    public void acquire() {

        if (_logger.isLoggable(Logger.FINEST)) {
            Object[] items = new Object[]{_owner};
            _logger.finest("utility.nullsemaphore.acquire", items); // NOI18N
        }
    }

    /**
     * Does nothing.
     */
    public void release() {

        if (_logger.isLoggable(Logger.FINEST)) {
            Object[] items = new Object[]{_owner};
            _logger.finest("utility.nullsemaphore.release", items); // NOI18N
        }
    }
}
