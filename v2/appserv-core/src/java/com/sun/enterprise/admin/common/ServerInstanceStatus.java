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

package com.sun.enterprise.admin.common;

// i18n import 
import com.sun.enterprise.admin.util.SOMLocalStringsManager;

/**
	A class that represents the status of a Server Instance.
*/
public class ServerInstanceStatus extends Status
{
    /* javac 1.3 generated serialVersionUID */
    public static final long serialVersionUID	=  7309765796199182533L;	

    private boolean mDebug = false;
    private int     mDebugPort = -1;
	// i18n SOMLocalStringsManager
	private static SOMLocalStringsManager localizedStrMgr =
		SOMLocalStringsManager.getManager( ServerInstanceStatus.class );

    /** 
        Creates new Status that represents state of a running Server Instance.
    */
    public ServerInstanceStatus ()
    {
        super(Status.kInstanceRunningCode, Status.kInstanceRunningMsg);
    }

    /**
     * Create new status that represents specified status. If specified status
     * code is invalid the method throws an IllegalArgumentException
     * @param statusCode status code, one of <code>Status.kInstanceStartingCode,
     *     Status.kInstanceRunningCode, Status.kInstanceStoppingCode and
     *     Status.kInstanceNotRunningCode</code>.
     * @throws IllegalArgumentException if specified status code is invalid.
     */
    public ServerInstanceStatus (int statusCode)
    {
        super();
        setStatusCodeAndStr(statusCode);
    }

    public ServerInstanceStatus(int code, String str)
    {
        super(code, str);
    }

    public boolean isRunning ()
    {
        return (mStatusCode == Status.kInstanceRunningCode);
    }

    /**
     * Convenience method for setting the current status code
     * to running.
     */
    public void setRunning()
    {
        mStatusCode     = kInstanceRunningCode;
        mStatusString   = kInstanceRunningMsg;
    }

    /**
     * Returns true if the status code is set to kInstanceStartingCode, 
     * false otherwise.
     */
    public boolean isStarting()
    {
        return (mStatusCode == kInstanceStartingCode);
    }

    /**
     * Convenience method for setting the current status code
     * to starting.
     */
    public void setStarting()
    {
        mStatusCode     = kInstanceStartingCode;
        mStatusString   = kInstanceStartingMsg;
    }

    /**
     * Returns true if the status code is set to kInstanceStoppingCode, 
     * false otherwise.
     */
    public boolean isStopping()
    {
        return (mStatusCode == kInstanceStoppingCode);
    }

    /**
     * Convenience method for setting the current status code
     * to stopping.
     */
    public void setStopping()
    {
        mStatusCode     = kInstanceStoppingCode;
        mStatusString   = kInstanceStoppingMsg;
    }

    /**
     * Returns true if the status code is set to kInstanceNotRunning, false
     * otherwise.
     */
    public boolean isNotRunning()
    {
        return (mStatusCode == Status.kInstanceNotRunningCode);
    }

    /**
     * Convenience method for setting the current status code
     * to not running.
     */
    public void setNotRunning()
    {
        mStatusCode     = kInstanceNotRunningCode;
        mStatusString   = kInstanceNotRunningMsg;
    }

    /**
     * Returns true if the instance is set to run in debug mode, 
     * false otherwise.
     */
    public boolean isDebug()
    {
        return mDebug;
    }

    /**
     */
    public void setDebug(boolean debug)
    {
        mDebug = debug;
    }
    
    /**
     * Returns the port on which JPDA port in admin-server JVM listens.
     * Returns -1 in case the debug flag is not enabled.
    */ 
    public int getDebugPort()
    {
        return ( mDebugPort );
    }
    /**
     * Sets the JPDA debugger port. This is the port to which the debuggers can
     * connect in case the JVM is to be debugged.
     * @param port integer representing port
    */
    public void setDebugPort(int port)
    {
        mDebugPort = port;
    }

    /**
     * Set status code and string to appropriate values using specified status
     * code. An IllegalArgumentException is thrown if status code is invalid
     * for server instance.
     */
    private void setStatusCodeAndStr(int statusCode) {
        switch (statusCode) {
          case Status.kInstanceStartingCode:
            this.mStatusCode = statusCode;
            this.mStatusString = Status.kInstanceStartingMsg;
            break;
          case Status.kInstanceRunningCode:
            this.mStatusCode = statusCode;
            this.mStatusString = Status.kInstanceRunningMsg;
            break;
          case Status.kInstanceStoppingCode:
            this.mStatusCode = statusCode;
            this.mStatusString = Status.kInstanceStoppingMsg;
            break;
          case Status.kInstanceNotRunningCode:
            this.mStatusCode = statusCode;
            this.mStatusString = Status.kInstanceNotRunningMsg;
            break;
          default:
			String msg = localizedStrMgr.getString( "admin.common.invalid_server_instance_status_code", new String( statusCode + "" ) );
            throw new IllegalArgumentException( msg );
        }
    }
}
