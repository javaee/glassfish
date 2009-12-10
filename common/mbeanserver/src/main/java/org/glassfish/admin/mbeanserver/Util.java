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
package org.glassfish.admin.mbeanserver;


import com.sun.logging.LogDomains;
import java.util.logging.Logger;

import org.glassfish.server.ServerEnvironmentImpl;

/**
Can't use things in amx-api, so a few methods are place here.
 */
final class Util
{
    public static String localhost()
    {
        try
        {
            return java.net.InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (java.net.UnknownHostException e)
        {
        }
        return "localhost";
    }

    public static Logger getLogger()
    {
        return LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER);
    }

}




