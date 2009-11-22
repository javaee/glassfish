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

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.ActionReport;

import com.sun.common.util.logging.LoggingConfigImpl;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import java.io.IOException;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Aug 26, 2009
 * Time: 5:32:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="list-logger-levels")
public class ListLoggerLevels implements AdminCommand {

    @Inject
    LoggingConfigImpl loggingConfig;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            HashMap<String,String>  props = (HashMap)loggingConfig.getLoggingProperties();

            ArrayList keys = new ArrayList();
            keys.addAll(props.keySet());
            Collections.sort(keys);
            Iterator it2 = keys.iterator();
            while (it2.hasNext())  {
                String name = (String)it2.next();
                if (name.endsWith(".level") && !name.equals(".level")) {
                final ActionReport.MessagePart part = report.getTopMessagePart()
                    .addChild();
                String n = name.substring(0,name.lastIndexOf(".level"));
                part.setMessage(n + ": "+ (String)props.get(name));
                }
            }


        } catch (IOException ex){
          report.setMessage("Unable to get the logger names");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);


    }
}
