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
 */

package org.glassfish.admin.cli.util;


/**
 * Extracts port from the given host:port entry. host:port is passed as
 * an argument to the main. Input argument provided can be just host without
 * port value. In this case, default value of 4848 is used for the port.
 *
 * @author Rajeshwar Patil 
 */

public class GetPort {

    public String extractPort(String hostport) {
        String port = null;
        if ((hostport != null) && (!hostport.equals(""))) {
            if (hostport.contains(":")) {
                //input value contains host as well as port value
                String [] sArr = hostport.split(":");
                port = sArr[1];
            } else {
                //input value contains only the host name.
                //using default value for port.
                port = "4848";
            }
            if ((port != null) && (!port.equals(""))) {
                System.out.print(port);
            }

            return port;
        }
        return null;
    }


    public static void main (String args[]) {
        try {
            GetPort util = new GetPort();
            util.extractPort(args[0]); 
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
