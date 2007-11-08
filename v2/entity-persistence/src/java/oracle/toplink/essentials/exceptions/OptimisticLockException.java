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

import java.util.Vector;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;
import oracle.toplink.essentials.sessions.SessionProfiler;

/**
 * <p><b>Purpose</b>:  This exception is used when TopLink's optimistic locking feature is used.
 * It will be raised if the object being updated or deleted was changed or deleted from the database since
 * it as last read.
 */
public class OptimisticLockException extends TopLinkException {

    /** Store the query that raised the optimistic violation. */
    protected transient ObjectLevelModifyQuery query;

    // ERROR CODES
    public final static int NO_VERSION_NUMBER_WHEN_DELETING = 5001;
    public final static int OBJECT_CHANGED_SINCE_LAST_READ_WHEN_DELETING = 5003;
    public final static int NO_VERSION_NUMBER_WHEN_UPDATING = 5004;
    public final static int OBJECT_CHANGED_SINCE_LAST_READ_WHEN_UPDATING = 5006;
    public final static int MUST_HAVE_MAPPING_WHEN_IN_OBJECT = 5007;
    public final static int NEED_TO_MAP_JAVA_SQL_TIMESTAMP = 5008;
    public final static int UNWRAPPING_OBJECT_DELETED_SINCE_LAST_READ = 5009;
    public final static int OBJECT_CHANGED_SINCE_LAST_MERGE = 5010;

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected OptimisticLockException(String theMessage) {
        super(theMessage);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected OptimisticLockException(String theMessage, ObjectLevelModifyQuery query) {
        super(theMessage);
        this.query = query;
        query.getSession().incrementProfile(SessionProfiler.OptimisticLockException);

    }

    /**
     * PUBLIC:
     * Return the object for which the problem was detected.
     */
    public Object getObject() {
        return getQuery().getObject();
    }

    /**
     * PUBLIC:
     * Return the query in which the problem was detected.
     */
    public ObjectLevelModifyQuery getQuery() {
        return query;
    }

    public static OptimisticLockException mustHaveMappingWhenStoredInObject(Class aClass) {
        Object[] args = { aClass };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, MUST_HAVE_MAPPING_WHEN_IN_OBJECT, args));
        optimisticLockException.setErrorCode(MUST_HAVE_MAPPING_WHEN_IN_OBJECT);
        return optimisticLockException;

    }

    public static OptimisticLockException noVersionNumberWhenDeleting(Object object, ObjectLevelModifyQuery query) {
        Vector key = new Vector();
        if (query.getSession() != null) {
            key = query.getSession().keyFromObject(object);
        }
        Object[] args = { object, object.getClass().getName(), key, CR };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, NO_VERSION_NUMBER_WHEN_DELETING, args), query);
        optimisticLockException.setErrorCode(NO_VERSION_NUMBER_WHEN_DELETING);
        return optimisticLockException;
    }

    public static OptimisticLockException noVersionNumberWhenUpdating(Object object, ObjectLevelModifyQuery query) {
        Vector key = new Vector();
        if (query.getSession() != null) {
            key = query.getSession().keyFromObject(object);
        }
        Object[] args = { object, object.getClass().getName(), key, CR };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, NO_VERSION_NUMBER_WHEN_UPDATING, args), query);
        optimisticLockException.setErrorCode(NO_VERSION_NUMBER_WHEN_UPDATING);
        return optimisticLockException;
    }

    public static OptimisticLockException objectChangedSinceLastReadWhenDeleting(Object object, ObjectLevelModifyQuery query) {
        Vector key = new Vector();
        if (query.getSession() != null) {
            key = query.getSession().keyFromObject(object);
        }
        Object[] args = { object, object.getClass().getName(), key, CR };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, OBJECT_CHANGED_SINCE_LAST_READ_WHEN_DELETING, args), query);
        optimisticLockException.setErrorCode(OBJECT_CHANGED_SINCE_LAST_READ_WHEN_DELETING);
        return optimisticLockException;
    }

    public static OptimisticLockException objectChangedSinceLastReadWhenUpdating(Object object, ObjectLevelModifyQuery query) {
        Vector key = new Vector();
        if (query.getSession() != null) {
            key = query.getSession().keyFromObject(object);
        }
        Object[] args = { object, object.getClass().getName(), key, CR };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, OBJECT_CHANGED_SINCE_LAST_READ_WHEN_UPDATING, args), query);
        optimisticLockException.setErrorCode(OBJECT_CHANGED_SINCE_LAST_READ_WHEN_UPDATING);
        return optimisticLockException;
    }
    
    public static OptimisticLockException objectChangedSinceLastMerge(Object object) {        
        Object[] args = { object, object.getClass().getName(), CR };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, OBJECT_CHANGED_SINCE_LAST_MERGE, args));
        optimisticLockException.setErrorCode(OBJECT_CHANGED_SINCE_LAST_MERGE);
        return optimisticLockException;
    }

    public static OptimisticLockException unwrappingObjectDeletedSinceLastRead(Vector pkVector, String className) {
        Object[] args = { pkVector, className };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, UNWRAPPING_OBJECT_DELETED_SINCE_LAST_READ, args));
        optimisticLockException.setErrorCode(UNWRAPPING_OBJECT_DELETED_SINCE_LAST_READ);
        return optimisticLockException;
    }

    //For CR#2281
    public static OptimisticLockException needToMapJavaSqlTimestampWhenStoredInObject() {
        Object[] args = {  };

        OptimisticLockException optimisticLockException = new OptimisticLockException(ExceptionMessageGenerator.buildMessage(OptimisticLockException.class, NEED_TO_MAP_JAVA_SQL_TIMESTAMP, args));
        optimisticLockException.setErrorCode(NEED_TO_MAP_JAVA_SQL_TIMESTAMP);
        return optimisticLockException;
    }

    /**
     * INTERNAL:
     * Set the query in which the problem was detected.
     */
    public void setQuery(ObjectLevelModifyQuery query) {
        this.query = query;
    }
}
