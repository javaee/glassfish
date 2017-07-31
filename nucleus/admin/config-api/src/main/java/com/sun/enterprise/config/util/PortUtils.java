/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.util;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.net.NetUtils;
import java.util.*;
import org.jvnet.hk2.config.TransactionFailure;
import static com.sun.enterprise.util.net.NetUtils.MAX_PORT;
import static com.sun.enterprise.config.util.PortConstants.*;

/**
 * static methods useful for dealing with ports.
 * @author Byron Nevins
 */
final class PortUtils {

    private PortUtils() {
        // no instances allowed!
    }

    /**
     * Make sure all ports that are specified by the user make sense.
     * @param server The new Server element
     * @return null if all went OK.  Otherwise return a String with the error message.
     */
    static void checkInternalConsistency(Server server) throws TransactionFailure {
        // Make sure all the system properties for ports have different numbers.
        List<SystemProperty> sysProps = server.getSystemProperty();
        Set<Integer> ports = new TreeSet<Integer>();

        for (SystemProperty sp : sysProps) {
            String name = sp.getName();

            if (PORTSLIST.contains(name)) {
                String val = sp.getValue();

                try {
                    boolean wasAdded = ports.add(Integer.parseInt(val));

                    if (!wasAdded) //TODO unit test
                        throw new TransactionFailure(Strings.get("PortUtils.duplicate_port", val, server.getName()));
                }
                catch(TransactionFailure tf) {
                    // don't re-wrap the same Exception type!
                    throw tf;
                }
                catch (Exception e) {  //TODO unit test
                    throw new TransactionFailure(Strings.get("PortUtils.non_int_port", val, server.getName()));
                }
            }
        }
        checkForLegalPorts(ports, server.getName());
    }

    /**
     *
     * @param ports
     * @param serverName
     * @return a message if error, nu8ll means A-OK
     */
    private static void checkForLegalPorts(Set<Integer> ports, String serverName) throws TransactionFailure {
        for (int port : ports)
            if (!NetUtils.isPortValid(port))
                throw new TransactionFailure(Strings.get("PortUtils.illegal_port_number", port, serverName, MAX_PORT));
    }
}
