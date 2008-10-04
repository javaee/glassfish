/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/* CVS information
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/server/callers/GetAttributeCaller.java,v 1.3 2005/12/25 04:26:38 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:38 $
*/

package com.sun.enterprise.admin.jmx.remote.server.callers;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.message.MBeanServerRequestMessage;
import javax.management.remote.message.MBeanServerResponseMessage;

/** Invokes the method getAttribute of the MBeanServerConnection.
 * @see MBeanServerRequestMessage#GET_ATTRIBUTE
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public class GetAttributeCaller extends AbstractMethodCaller {
    
    /** Creates a new instance of CreateMBeanCaller */
    public GetAttributeCaller(MBeanServerConnection mbsc) {
        super(mbsc);
        METHOD_ID = MBeanServerRequestMessage.GET_ATTRIBUTE;
    }
    
    public MBeanServerResponseMessage call(MBeanServerRequestMessage request) {
        Object result = null;
        boolean isException = false;
        try {
            result = mbsc.getAttribute((ObjectName)request.getParams()[0],
            (String)request.getParams()[1]);
        }
        catch(Throwable e) {
            result = e;
            isException = true;
        }
        return ( new MBeanServerResponseMessage(METHOD_ID, result, isException) );
    }
}
