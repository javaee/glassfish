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

import java.util.Set;
import java.util.Map;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;

/**
 */
public class VirtualServerCmd extends BaseCmd implements SourceCmd
{
    public static final String kCreateMode      = "CreateVirtualServer";
    public static final String kDeleteMode      = "DeleteVirtualServer";

    public static final String kName            = "Name";
    public static final String kHosts           = "Hosts";
    public static final String kConfigName      = "ConfigName";
    public static final String kOptional        = "Optional";

    private final String mode;

    public VirtualServerCmd(CmdEnv cmdEnv, String mode)
    {
        super(cmdEnv);
        this.mode = mode;
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        Object ret = new Integer(0);
        if (mode.equals(kCreateMode))
        {
            ret = create();
        }
        else if (mode.equals(kDeleteMode))
        {
            remove();
        }
        else
        {
            throw new Exception("Unknown mode");
        }
        return ret;
    }

    protected VirtualServerConfig create() throws Exception
    {
        final HTTPServiceConfig mgr = getHTTPServiceConfig(
            getConfigName());
        return mgr.createVirtualServerConfig(getName(), getHosts(), getOptional());
    }

    protected void remove() throws Exception
    {
        final String configName = getConfigName();
        final String vsName     = getName();

        getHTTPServiceConfig(configName).removeVirtualServerConfig(vsName);
    }

    private HTTPServiceConfig getHTTPServiceConfig(
        final String configName) throws Exception
    {
        final String j2eeTypeProp = Util.makeJ2EETypeProp(
                HTTPServiceConfig.J2EE_TYPE);
        final String configProp = Util.makeProp(
                ConfigConfig.J2EE_TYPE, configName);
        final String props = Util.concatenateProps(j2eeTypeProp, configProp);

        final Set s = getQueryMgr().queryPropsSet(props);
        assert s != null && s.size() == 1;

        return (HTTPServiceConfig)s.iterator().next();
    }

    private String getHosts()
    {
        return (String)getCmdEnv().get(kHosts);
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }

    private String getConfigName()
    {
        return (String)getCmdEnv().get(kConfigName);
    }

    private Map getOptional()
    {
        return (Map)getCmdEnv().get(kOptional);
    }
}
