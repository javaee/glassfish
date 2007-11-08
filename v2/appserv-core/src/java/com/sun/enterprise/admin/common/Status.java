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

import com.sun.enterprise.util.LocalStringsImpl;
//JDK imports
import java.io.Serializable;

/**
	Base class for status of a Server side entity. Entities with specific
	semantics for status are expected to extend this class.
*/
public class Status implements Serializable
{
    /* javac 1.3 generated serialVersionUID */
    public static final long serialVersionUID			= -211313166471337363L;
    public static final int kInstanceRunningCode		= 0;
    public static final int kInstanceStartingCode		= 1;
    public static final int kInstanceStoppingCode		= 2;
    public static final int kInstanceNotRunningCode		= 3;
    public static final int kEntityEnabledCode			= 4;
    public static final int kEntityDisabledCode			= 5;
    public static final int kClusterPartiallyRunningCode        = 6;
    public static final int kInstanceSynchronizingCode          = 7;
    public static final int kInstanceFailedCode                 = 8;

    public static final String kInstanceRunningMsg;
    public static final String kInstanceStartingMsg;
    public static final String kInstanceStoppingMsg;
    public static final String kInstanceNotRunningMsg;
    public static final String kUnknownStateMsg;
    public static final String kPartiallyRunningMsg;
    public static final String kInstanceSynchronizingMsg;
    public static final String kInstanceFailedMsg;    

    protected int mStatusCode;
    protected String mStatusString;
   
    static
    {
        // use localized strings...
        String pre = "admin.common.status.";
        LocalStringsImpl ls = new LocalStringsImpl();
        
        kInstanceRunningMsg         = ls.get(pre + "0");
        kInstanceStartingMsg        = ls.get(pre + "1");
        kInstanceStoppingMsg        = ls.get(pre + "2");
        kInstanceNotRunningMsg      = ls.get(pre + "3");
        kUnknownStateMsg            = ls.get(pre + "4");
        kPartiallyRunningMsg        = ls.get(pre + "6");
        kInstanceSynchronizingMsg   = ls.get(pre + "7");
        kInstanceFailedMsg          = ls.get(pre + "8");
    }
   	
    /**
     * Create new status. The sub-classes must set protected instance
     * variables mStatusCode and mStatusString appropriately.
     */
    protected Status()
    {
    }

    /** 
            Creates new Status.
    */
    public Status (int code, String str)
    {
        mStatusCode = code;
        mStatusString = str;
    }
    
    public Status (int code)
    {
        mStatusCode = code;
        mStatusString = getStatusString(code);
    }
	
    /**
            Returns the status code for this Status.

            @return status code
    */
    public int getStatusCode()
    {
            return mStatusCode;
    }

    /**
            Returns the status string for this Status.

            @return string representing status
    */
    public String getStatusString()
    {
            return mStatusString;
    }

    /**
     * Returns statusCode : statusString
     */ 
    public String toString()
    {
        return (mStatusCode + " : " + mStatusString);
    }

    public static String getStatusString(int code)
    {
        String status = kUnknownStateMsg;
        switch (code)
        {
            case kInstanceNotRunningCode :
                status = kInstanceNotRunningMsg;
                break;
            case kInstanceRunningCode :
                status = kInstanceRunningMsg;
                break;
            case kInstanceStartingCode :
                status = kInstanceStartingMsg;
                break;
            case kInstanceFailedCode :
                status = kInstanceFailedMsg;
                break;
            case kInstanceStoppingCode :
                status = kInstanceStoppingMsg;
                break;
            case kClusterPartiallyRunningCode :
                status = kPartiallyRunningMsg;
                break;
            case kInstanceSynchronizingCode :
                status = kInstanceSynchronizingMsg;
                break;
            default :
                break;
        }
        return status;
    }
}
