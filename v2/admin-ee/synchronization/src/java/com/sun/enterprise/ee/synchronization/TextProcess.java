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
package com.sun.enterprise.ee.synchronization;

import java.util.Properties;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Config;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

public class TextProcess {

    private static Logger _logger = Logger.getLogger(EELogDomains.
        SYNCHRONIZATION_LOGGER);

    public TextProcess() {
    }
    
    public static String tokenizeConfig(String str, String instName, 
            Properties env) 
    {
        if (str == null) return str;

        String[] result = str.split(DEF_DELIM_TOKEN);
        StringBuffer s = new StringBuffer();
        for  (int i= 0; i < result.length; i++)
        {
            if (result[i].startsWith(DEF_START_TOKEN) == true )
            {
                int idx       = result[i].indexOf(DEF_END_TOKEN);
                String key    = result[i].substring(1, idx);
                String value  = lookupConfigVar(key, instName, env);
                s.append(value);
                s.append(result[i].substring(idx+1));
            }
            else
            {
                s.append(result[i]);
            }    
        }
        return s.toString();
    }

    public static String lookupConfigVar(String s, String instName, 
            Properties env) 
    {

        if ( s.equals("config.name") ) // should only match in DAS
        {
            assert(instName != null);
            assert(env != null);

            // if the key exists in the environment obj, use it from there
            String x = env.getProperty(s);

            if (x == null) {
                x = lookupConfigName(instName);
                assert(x != null);

                // adds the token key/value to the environment properties obj 
                env.put("config.name", x);
            }
            return x;
        }
        else 
        {
            String x = System.getProperty(s);

            // lookup from the environment if not found in the system 
            if (x == null) {
                x = env.getProperty(s);
            }

            assert(x != null);
            return x;
        }
    }

    public static String lookupConfigName(String instName) 
    {
        String configName = null;

        try {
            ConfigContext ctx = AdminService.getAdminService().
                        getAdminContext().getAdminConfigContext();
            Config config = ServerHelper.getConfigForServer(ctx,instName);

            if ( config != null )
                configName = config.getName();

        } catch (Exception ce) {
            _logger.log(Level.FINE,
                  "synchronization.config_not_found",instName);
        }

        assert(configName != null);

        return configName;

    }

    public static void transformDASConfig(SynchronizationRequest[] requests) 
    {

        for (int i=0; i<requests.length; i++) 
        {
            String serverName = requests[i].getServerName();
            Properties env = requests[i].getEnvironmentProperties();

            // the request src file path is converted on demand in DAS and cache

            // converts the timestamp file path in DAS
            String srcTs = requests[i].getTimestampFileName();
            requests[i].setTimestampFileName( tokenizeConfig(srcTs, 
                                            serverName, env));

            // converts the destination/target directory
            String targetDir = requests[i].getTargetDirectory();
            requests[i].setTargetDirectory( tokenizeConfig(targetDir, 
                                            serverName, env));
        }
    }

    // ---- VARIABLE(S) - PRIVATE --------------------------------
    static final String DEF_DELIM_TOKEN  = "[$]";
    static final String DEF_START_TOKEN  = "{";
    static final String DEF_END_TOKEN    = "}";
}
