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
package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.embedded.admin.EmbeddedAdminContainer;
import org.glassfish.api.embedded.admin.CommandExecution;
import org.glassfish.api.embedded.admin.CommandParameters;
import org.glassfish.api.embedded.Port;
import org.glassfish.api.embedded.BindException;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.ActionReport;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import com.sun.enterprise.v3.common.PlainTextActionReporter;

/**
 * Implementation of the embedded command execution
 *
 * @author Jerome Dochez
 */
@Service
public class EmbeddedAdminCtrImpl implements EmbeddedAdminContainer {

    @Inject
    CommandRunner runner;

    private final static List<Sniffer> empty = new ArrayList<Sniffer>();

    public List<Sniffer> getSniffers() {
        return empty;
    }

    public void bind(Port port, String protocol) {

    }

    public void start() throws LifecycleException {

    }

    public void stop() throws LifecycleException {

    }

    public CommandExecution execute(String commandName, CommandParameters params) {
        ParameterMap props = params.getOptions();
        if (params.getOperands().size() > 0) {
            for (String op : params.getOperands())
                props.add("DEFAULT", op);
        }
        final ActionReport report = new PlainTextActionReporter();
        CommandExecution ce = new CommandExecution() {

            public ActionReport getActionReport() {
                return report;
            }

            public ActionReport.ExitCode getExitCode() {
                return report.getActionExitCode();
            }

            public String getMessage() {
                return report.getMessage();
            }
        };
        runner.getCommandInvocation(commandName, report).parameters(props).execute();
        return ce;
    }

    public void bind(Port port) {

    }

    public void bindJmxTo(Port port) throws BindException {

    }
}
