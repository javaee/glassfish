/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.gms.admin;

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.gms.tools.MulticastTester;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.util.ArrayList;
import java.util.List;

/**
 * asadmin local command that wraps the multicast validator tool
 * in shoal-gms-impl.jar
 */
@Service(name="validate-multicast")
@Scoped(PerLookup.class)
public final class ValidateMulticastCommand extends CLICommand {

    // todo: after next integration of shoal, get these values from MulticastTester
    private static final String DASH = "--";
    private static final String PORT_OPTION = "multicastport";
    private static final String ADDRESS_OPTION = "multicastaddress";
    private static final String BIND_INT_OPTION = "bindinterface";
    private static final String PERIOD_OPTION = "sendperiod";
    private static final String TIMEOUT_OPTION = "timeout";
    private static final String DEBUG_OPTION = "debug";

    @Param(name=PORT_OPTION, optional=true)
    private String port;

    @Param(name=ADDRESS_OPTION, optional=true)
    private String address;

    @Param(name=BIND_INT_OPTION, optional=true)
    private String bindInterface;

    @Param(name=PERIOD_OPTION, optional=true)
    private String period;

    @Param(name=TIMEOUT_OPTION, optional=true)
    private String timeout;

    @Param(name="verbose", optional=true)
    private String debug;

    @Override
    protected int executeCommand() throws CommandException {
        // todo: see if we can have exceptions come back to us for decent return value
        MulticastTester.main(createArgs());
        return 0;
    }

    private String [] createArgs() {
        List<String> argList = new ArrayList<String>();
        if (port != null && !port.isEmpty()) {
            argList.add(DASH + PORT_OPTION);
            argList.add(port);
        }
        if (address != null && !address.isEmpty()) {
            argList.add(DASH + ADDRESS_OPTION);
            argList.add(address);
        }
        if (bindInterface != null && !bindInterface.isEmpty()) {
            argList.add(DASH + BIND_INT_OPTION);
            argList.add(bindInterface);
        }
        if (period != null && !period.isEmpty()) {
            argList.add(DASH + PERIOD_OPTION);
            argList.add(period);
        }
        if (timeout != null && !timeout.isEmpty()) {
            argList.add(DASH + TIMEOUT_OPTION);
            argList.add(timeout);
        }
        if (debug != null && !debug.isEmpty()) {
            argList.add(DASH + DEBUG_OPTION);
        }
        return argList.toArray(new String[0]);
    }
}
