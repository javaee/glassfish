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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
//----------------------------------------------------------------------------
//
// Module:      LogException.java
//
// Description: Log exception.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//
// Copyright (c):   1995-1997 IBM Corp.
//
//   The source code for this program is not published or otherwise divested
//   of its trade secrets, irrespective of what has been deposited with the
//   U.S. Copyright Office.
//
//   This software contains confidential and proprietary information of
//   IBM Corp.
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

/**A class which contains exception information for errors in the log.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
*/
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//------------------------------------------------------------------------------

class LogException extends Throwable {

    /**Constants which define error codes from the logger classes.
     */
    static final int LOG_SUCCESS = 0;
    static final int LOG_NOT_INITIALISED = 1;
    static final int LOG_OPEN_FAILURE = 2;
    static final int LOG_READ_FAILURE = 3;
    static final int LOG_CORRUPTED = 4;
    static final int LOG_INVALID_FILE_DESCRIPTOR = 5;
    static final int LOG_LOCK_FAILURE = 6;
    static final int LOG_WRITE_FAILURE = 7;
    static final int LOG_CLOSE_FAILURE = 8;
    static final int LOG_TOO_MANY_INPUT_BUFFERS = 9;
    static final int LOG_RECORD_TOO_LARGE = 10;
    static final int LOG_NO_SPACE = 11;
    static final int LOG_INSUFFICIENT_MEMORY = 12;
    static final int LOG_ERROR_FORCING_LOG = 13;
    static final int LOG_INVALID_LSN = 14;
    static final int LOG_NEW_TAIL_TOO_HIGH = 15;
    static final int LOG_NEW_TAIL_TOO_LOW = 16;
    static final int LOG_INVALID_TAIL = 17;
    static final int LOG_INTERNAL_ERROR = 18;
    static final int LOG_NO_RESTART_RECORD = 19;
    static final int LOG_INVALID_CURSOR = 20;
    static final int LOG_END_OF_CURSOR = 21;
    static final int LOG_ACCESS_FAILURE = 22;
    static final int LOG_INVALID_PROCESS = 23;
    static final int LOG_INVALID_RECORDTYPE = 24;
    static final int LOG_INVALID_WRITEMODE = 25;
    static final int LOG_OPEN_EXTENT_FAILURE = 26;
    static final int LOG_READ_ONLY_ACCESS = 27;
    static final int MAX_RESPONSE_VALUE = LOG_READ_ONLY_ACCESS;

    /**Strings which contain error messages from the log.
     */
    private static final String[] statusStrings = 
    { "LOG-000: Operation successful"/*#Frozen*/,
      "LOG-001: Log not initialised"/*#Frozen*/,
      "LOG-002: Open failure"/*#Frozen*/,
      "LOG-003: Read failure"/*#Frozen*/,
      "LOG-004: Data corrupted"/*#Frozen*/,
      "LOG-005: Invalid file descriptor"/*#Frozen*/,
      "LOG-006: Lock failure"/*#Frozen*/,
      "LOG-007: Write failure"/*#Frozen*/,
      "LOG-008: Close failure"/*#Frozen*/,
      "LOG-009: Too many input buffers"/*#Frozen*/,
      "LOG-010: Record too large"/*#Frozen*/,
      "LOG-011: No space in filesystem"/*#Frozen*/,
      "LOG-012: Insufficient memory"/*#Frozen*/,
      "LOG-013: Force failure"/*#Frozen*/,
      "LOG-014: Invalid LSN value"/*#Frozen*/,
      "LOG-015: New tail LSN too high"/*#Frozen*/,
      "LOG-016: New tail LSN too low"/*#Frozen*/,
      "LOG-017: Invalid tail LSN value"/*#Frozen*/,
      "LOG-018: Internal error"/*#Frozen*/,
      "LOG-019: No restart record present"/*#Frozen*/,
      "LOG-020: Invalid cursor value"/*#Frozen*/,
      "LOG-021: End of cursor reached"/*#Frozen*/,
      "LOG-022: Filesystem access failure"/*#Frozen*/,
      "LOG-023: Invalid process"/*#Frozen*/,
      "LOG-024: Log is read only"/*#Frozen*/,
      "LOG-025: Invalid record type specified"/*#Frozen*/,
      "LOG-026: Extent file open failure"/*#Frozen*/,
      "LOG-027: Invalid write mode specified"/*#Frozen*/,
      "LOG-028: Invalid status specified"/*#Frozen*/ };

    /**Instance members
     */
    int errorCode;
    private int throwPoint;
    private Object extraInfo;

    /**LogException constructor
     *
     * @param trc   The current trace object to allow exception trace to be made.
     * @param err   The error code.
     * @param point The throw point.
     *
     * @return
     *
     * @see
     */
    LogException(Object dummy /* COMMENT(Ram J) - used to be trace object */,
                  int   err,
                  int   point ) {
        super(new String("Log exception at point "+point+":\n"+
                         statusStrings[err>MAX_RESPONSE_VALUE?MAX_RESPONSE_VALUE+1:err]/*#Frozen*/));
        errorCode = err;
        throwPoint = point;
    }

    /**LogException constructor
     *
     * @param trc    The current trace object to allow exception trace to be made.
     * @param err    The error code.
     * @param point  The throw point.
     * @param extra  Extra information.
     *
     * @return
     *
     * @see
     */
    LogException(Object dummy /* COMMENT(Ram J) - used to be trace object */,
                  int    err,
                  int    point,
                  Object extra ) {
        super(new String("Log exception at point "+point+":\n"+
                         statusStrings[err>MAX_RESPONSE_VALUE?MAX_RESPONSE_VALUE+1:err]/*#Frozen*/));
        errorCode = err;
        throwPoint = point;
        extraInfo = extra;
    }
}
