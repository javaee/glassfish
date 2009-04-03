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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.launcher;

/**
 *
 * @author bnevins
 */
public class GFLauncherFactory {

    /**
     * An enum for specifying the three kinds of servers.
     */
    public enum ServerType
    {
        domain, nodeAgent, instance, embedded
    }; 
    /**
     * 
     * @param type The type of server to launch.
     * @return A launcher instance that can be used for launching the specified 
     * server type.
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException 
     */
    public static GFLauncher getInstance(ServerType type) throws GFLauncherException
    {
        switch(type)
        {
            case domain:  
                return new GFDomainLauncher(
                        new GFLauncherInfo(GFLauncherFactory.ServerType.domain));
            case embedded:
                return new GFEmbeddedLauncher(
                        new GFLauncherInfo(GFLauncherFactory.ServerType.embedded));

            default:
                throw new GFLauncherException("Only domain and embedded launching is currently supported.");
        }
    }
}
