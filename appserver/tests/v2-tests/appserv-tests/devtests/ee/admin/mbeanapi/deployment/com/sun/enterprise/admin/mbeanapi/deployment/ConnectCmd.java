/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;

/**
 */
public class ConnectCmd extends BaseCmd implements SourceCmd
{
    public static final String kHost        = "host";
    public static final String kPort        = "port";
    public static final String kUser        = "user";
    public static final String kPassword    = "password";
    public static final String kUseTLS      = "useTLS";

    public ConnectCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        if (isConnected())
        {
            return getConnectionSource();
        }

        final String    host        = (String)getCmdEnv().get(kHost);
        final Integer   port        = (Integer)getCmdEnv().get(kPort);
        final String    user        = (String)getCmdEnv().get(kUser);
        final String    password    = (String)getCmdEnv().get(kPassword);

        final AppserverConnectionSource cs = new AppserverConnectionSource(
            AppserverConnectionSource.PROTOCOL_RMI, host, port.intValue(), 
            user, password, getTLSParams(), null);

        getCmdEnv().put(kConnectionSource, cs);

        return cs;
    }
}
