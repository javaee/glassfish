/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.web.plugin.common;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Superclass of all web module config-related commands.
 * 
 * All of these commands have the app-name (with perhaps /module-name appended)
 * as a required argument.
 * 
 * @author tjquinn
 */
public abstract class WebModuleConfigCommand implements AdminCommand {

    private final static String WEB_SNIFFER_TYPE = "web";

    @Param(primary=true)
    private String appNameAndOptionalModuleName;

    @Inject
    private Applications apps;

    final protected LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(WebModuleConfigCommand.class);

    protected WebModuleConfig webModuleConfig(final ActionReport report) {
        Module m = module(report);
        if (m == null) {
            return null;
        }

        WebModuleConfig config = (WebModuleConfig) engine(report).getApplicationConfig();
        return config;
    }

    /**
     * Returns the Application corresponding to the app specified in the
     * command arguments.
     *
     * @return Application object for the app
     */
    private Application application() {
        final Application result = apps.getModule(Application.class,
                appName());

        return result;
    }

    private Module module(final ActionReport report) {
        final Application app = application();
        if (app == null) {
            fail(report, "appNotReg","Application {0} not registered",
                    appName());
            return null;
        }
        
        final Module module = app.getModule(moduleName());
        if (module == null) {
            fail(report, "noSuchModule","Application {0} does not contain module {1}",
                    appName(),
                    moduleName());
        }
        return module;
    }

    protected Engine engine(final ActionReport report) {
        Module module = module(report);
        if (module == null) {
            return null;
        }

        Engine e = module.getEngine(WEB_SNIFFER_TYPE);
        if (e == null) {
            fail(report, "noSuchEngine","Application {0}/module {1} does not contain engine {2}",
                    appName(),
                    moduleName(),
                    WEB_SNIFFER_TYPE);
        }
        return e;
    }
    
    /**
     * Returns either the explicit module name (if the command argument
     * specified one) or the app name.  An app can contain a module with the
     * same name.
     *
     * @return module name inferred from the command arguments
     */
    protected String moduleName() {
        final int endOfAppName = endOfAppName();
        return (endOfAppName == appNameAndOptionalModuleName.length()) ?
            appNameAndOptionalModuleName :
            appNameAndOptionalModuleName.substring(endOfAppName);
    }
    
    protected String appName() {
        return appNameAndOptionalModuleName.substring(0, endOfAppName());
    }
    
    private int endOfAppName() {
        final int slash = appNameAndOptionalModuleName.indexOf('/');
        return (slash == -1 ? appNameAndOptionalModuleName.length() : slash);
    }

    protected ActionReport fail(final ActionReport report,
            final Exception e,
            final String msgKey,
            final String defaultFormat, Object... args) {
        report.setFailureCause(e);
        return fail(report, msgKey, defaultFormat, args);

    }
    protected ActionReport fail(final ActionReport report, final String msgKey,
            final String defaultFormat, Object... args) {
        return finish(report, ActionReport.ExitCode.FAILURE,
                msgKey, defaultFormat, args);
    }

    protected ActionReport succeed(final ActionReport report,
            final String msgKey, final String defaultFormat, Object... args) {
        return finish(report, ActionReport.ExitCode.SUCCESS,
                msgKey, defaultFormat, args);
    }

    private ActionReport finish(final ActionReport report,
            final ActionReport.ExitCode exitCode,
            final String msgKey, final String defaultFormat, Object... args) {
        String msg = localStrings.getLocalString(msgKey, defaultFormat, args);
        report.setActionDescription(msg);
        report.setActionExitCode(exitCode);
        return report;
    }
}
