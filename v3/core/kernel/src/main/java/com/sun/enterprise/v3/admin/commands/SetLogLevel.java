/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;


/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Jul 8, 2009
 * Time: 11:48:20 AM
 * To change this template use File | Settings | File Templates.
 */
  
/*
 * Set Logger Level Command
 *
 * Updates one or more loggers' level
 *
 * Usage: set-log-level [-?|--help=false]
 * (logger_name=logging_value)[:logger_name=logging_value]*
 *
 */

@Service(name="set-log-level")
@I18n("set.log.level")
public class SetLogLevel implements AdminCommand {

    @Param(name="name_value", primary=true, separator=':')
        Properties properties;
    
    @Inject
    LoggingConfigImpl loggingConfig;

    String[] validLevels = {"SEVERE", "WARNING", "INFO", "FINE", "FINER", "FINEST"};
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(SetLogLevel.class);


    public void execute(AdminCommandContext context) {


        final ActionReport report = context.getActionReport();

        Map<String,String> m = new HashMap<String,String>();
        try {
            for (final Object key : properties.keySet()) {
                final String logger_name = (String) key;
                final String level = (String)properties.get(logger_name);
                // that is is a valid level
                boolean vlvl=false;
                for (String s: validLevels) {
                    if (s.equals(level) ) {
                        m.put(logger_name+".level", level );
                        vlvl=true;
                        break;
                    }
                }
                if (!vlvl) {
                    report.setMessage(localStrings.getLocalString("set.log.level.invalid",
                    "Invalid logger level found {0}.  Valid levels are: SEVERE, WARNING, INFO, FINE, FINER, FINEST", level));
                }


            }
            loggingConfig.updateLoggingProperties(m);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);


        }   catch (IOException e) {
            report.setMessage("Could not set logger levels ");
            report.setMessage(localStrings.getLocalString("set.log.level.failed",
                    "Could not set logger levels."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

}
