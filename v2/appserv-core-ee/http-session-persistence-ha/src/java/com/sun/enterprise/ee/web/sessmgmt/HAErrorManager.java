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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * HAErrorManager.java
 *
 * Created on April 22, 2002, 4:49 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

/**
* This class is a helper class to let an application correctly work with the
* HA Store.  It supports retrying a certain class of SQLExceptions called
* <i>retryable</i> exceptions.  These exceptions are due to transient failures
* which will require the transaction to be rolled back, but do not prevent the
* transaction from being retried again.  It is important for an application
* to do this so that the HA failover features of the HA Store are taken advantage
* of.
*
* There is also support for specifying how long one should attempt to retry
* a transaction until it is a hard error.
*/
/**
 *
 * @author  lwhite
 * @version 
 */    
public class HAErrorManager
{
  //different error codes for retryable errors
  protected static final Hashtable retryables = new Hashtable();
  
  //different error codes for retryable errors
  protected static final Hashtable retryablesWithHealthCheckIssues = new Hashtable();  
  
  //contains the primary key violation code
  protected static final Hashtable keyViolations = new Hashtable();  
  
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;   

  static
  {
    retryables.put("208",  "208 - Transaction Aborted");
    retryables.put("216",  "216 - Abort Requested By Slave");
    retryables.put("224",  "225 - Operation Timed Out");
    retryables.put("1552", "1552 - Too Many Transactions");
    retryables.put("2080", "2080 - Out of Locks");
    retryables.put("2097", "2097 - Upgrade From Shared to Exclusive Lock Failed");
    retryables.put("2168", "2168 - Execution Terminated at Slave Involved in Takeover");
    retryables.put("2320", "2320 - TCON Does Not Accept Clients");
    retryables.put("3104", "3104 - Bad Slave");
    retryables.put("3504", "3504 - Node Failed");
    retryables.put("4192", "4192 - Reply Lost");
    retryables.put("4576", "4576 - Client Held Transaction Open Too Long");
    retryables.put("4624", "4624 - Cursor Failure");
    retryables.put("25018", "25018 - Lost Connection");
    retryables.put("25017", "25017 - No Connection");
    retryables.put("25012", "25012 - RPC Connection failed");
    retryables.put("25005", "25005 - RPC is closed");
    retryables.put("2304",  "2304 - Session Disconnected");
    retryables.put("20001", "20001 - Internal Error");
    //retryables.put("20004", "20004 - Connection closed");
    retryables.put("20005", "20005 - RPC closed");
    retryables.put("25013", "25013 - RPC Execute failed");
    retryables.put("12522", "12522 - no connection is created");
    retryables.put("1040", "1040 - No such file in file directory");
    retryables.put("1680","1680");
    retryables.put("2078","2078");
    retryables.put("1986","1986");
    
    retryablesWithHealthCheckIssues.put("25012", "25012 - RPC Connection failed");
    retryablesWithHealthCheckIssues.put("25017", "25017 - No Connection");
    retryablesWithHealthCheckIssues.put("25018", "25018 - Lost Connection");    
    
    keyViolations.put("11939","11939 - Primary key constraint violation"); 
  }


  /**
  * How long to wait in ms until we throw a TimeoutException.  Default 
  * is 5 minutes.
  */
  private long timeoutMsecs = 1000 * 60 * 5;

  /** 
  * When the transaction started
  */
  private long txStartTime;

  /**
  * How long the transaction took (valid only when txCompleted is true)
  */
  private long txDuration = 0;

  /**
  * identifier for this manager
  */
  protected String id;

  /**
  * state information about the transaction
  */
  private static final int TX_IDLE = 1;
  private static final int TX_STARTED = 2;
  private static final int TX_COMPLETED = 3;

  private int txState = TX_IDLE;

  /**
   * Constructor
   *
   * @param timeoutSecs 
   *   How long to wait in seconds before we decide the transaction
   *   is unable to complete and a TimeoutException is
   *   thrown.
   * 
   * @param id
   *   A string to identify the current thread/instance/whatever
   *
   */  
  public HAErrorManager(long timeoutSecs, String id)
  {
    this.timeoutMsecs = timeoutSecs * 1000;
    this.id = id;
    if (_logger == null) {
        _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    }        
  }

  /** Return true if this is a retryable error */
  private boolean isRetryable(SQLException e)
  {
    String msgnum = new Integer(e.getErrorCode()).toString();
    String err = (String)retryables.get(msgnum);

    return err != null;
  }
  
  protected boolean isPrimaryKeyViolation(SQLException e) {
    String msgnum = new Integer(e.getErrorCode()).toString();
    String err = (String)keyViolations.get(msgnum);

    return err != null;
  }  

  public void printRetryableMessage(SQLException e){
    String msgnum = new Integer(e.getErrorCode()).toString();
    String err = (String)retryables.get(msgnum);
    if(_logger.isLoggable(Level.FINEST)) {
        _logger.finest("  "+ err);
    }
  }

  /**
   * Check the current error received from the Clustra database
   *
   * If the error is retryable, this method does the following things:
   *   - If we exceed transaction timeout, throw TimeoutException
   *   - Rolls back the current transaction 
   *
   * If the error is not retryable, this method rethrows the exception.
   *   
   * @param e   
   *   the SQLException that has been received
   *
   * @throws SQLException
   *   if the error is not retryable
   *
   * @throws HATimeoutException
   *   if our transaction timeout is exceeded
   */
  public void checkError(SQLException e, Connection con) throws
    SQLException, HATimeoutException
  {
    int nativeCode = e.getErrorCode();

    //begin 6374243 
    // Rethrow the exception if the thread has been interrupted
    if ( Thread.currentThread().isInterrupted() )
    {
      Thread.interrupted();
      rollbackConnection(con);
      Thread.currentThread().interrupt();
      throw e;
    }
    //end 6374243    
    
    // Rethrow the exception if it is not retryable
    if ( ! isRetryable(e) )
    {
      throw e;
    }
    
    //check the health of HADB
    //if health bad then end tx and return
    if( isRetryableWithHealthCheckIssues(e) ) {
        if( !EEHADBHealthChecker.doQuickHADBHealthCheck() ) {
            this.txEnd();
            return;
        }
    }
    this.printRetryableMessage(e);
    //System.out.println("HAErrorManager retryable code : " + nativeCode);    

    checkTimeouts();

    try
    {
      // Abort the transaction on this connection
      if ( con != null )  {
        con.rollback();
      }
    }
    catch ( SQLException abortE )
    {
    }
  }
  
  private void rollbackConnection(Connection con) {
    try
    {
      // Abort the transaction on this connection
      if ( con != null )  {
        con.rollback();
      }
    }
    catch ( SQLException abortE )
    {
    } 
  }  


  /**
   * Check to see if timeouts have been exceeded.  If timeTilError
   * is exceeded, we log an error saying we've been waiting too long
   * and this counts as an error.  If timeTillExit is exceeded, we
   * log an error and do a hard exit of the application
   */
  protected void checkTimeouts() throws HATimeoutException
  {
    if ( getElapsedTime() > timeoutMsecs )
    {
      throw new HATimeoutException("Unable to complete a transaction " +
        "after " + timeoutMsecs / 60000 + " minutes.");
    }
  }

  /**
   * Indicate that a transaction has started.   This initializes the
   * everything only the first time it is called for this transaction.
   * This allows you to safely call txStart() in a retry loop.
   */
  public void txStart()
  {
    if ( txState != TX_STARTED )
    {
      txState = TX_STARTED;
      txStartTime = System.currentTimeMillis();
      txDuration = 0;
    }
  }

  /**
  * Indicate that a transaction has ended.  This stops the timer
  * and sets a flag so that txCompleted() returns true.
  */
  public void txEnd()
  {
    txState = TX_COMPLETED;
    txDuration = System.currentTimeMillis() - txStartTime;
  } 

  /**
  * Return true if this transaction has completed
  */
  public boolean isTxCompleted()
  {
    return txState == TX_COMPLETED;
  }
    
  /**
  * Find out how long it's been since we started the transaction
  * 
  * @return the elapsed time of the transaction in milliseconds
  */
  public long getElapsedTime()
  {
    if ( txState == TX_STARTED )
    {
       return System.currentTimeMillis() - txStartTime;
    }
    else if ( txState == TX_COMPLETED )
    {
       return txDuration;
    }
    else
    {
       return 0;
    }
  }
  
  //BEGIN HEALTH-CHECK RELATED
  
  /** Return true if this is a retryable error with health check issues */
  private boolean isRetryableWithHealthCheckIssues(SQLException e)
  {
    String msgnum = new Integer(e.getErrorCode()).toString();
    String err = (String)retryablesWithHealthCheckIssues.get(msgnum);

    return err != null;
  }
  
  //END HEALTH-CHECK RELATED  

  /**
  * Get the set timeout value in milliseconds
  */
  public long getTimeoutMsecs()
  {
    return timeoutMsecs;
  }

}
