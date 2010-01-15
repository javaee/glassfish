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

package com.sun.enterprise.universal.process;

import com.sun.enterprise.util.StringUtils;
import java.lang.management.ManagementFactory;

/**
 * Includes a somewhat kludgy way to get the pid for "me".
 * Another casualty of the JDK catering to the LEAST common denominator.
 * Some obscure OS might not have a pid!
 * The name returned from the JMX method is like so:
 * 12345@mycomputername where 12345 is the PID
 * @author bnevins
 */
public final class ProcessUtils {
    private ProcessUtils() {
    }

    /**
     * Try and find the Process ID of "our" process.
     * @return the process id or -1 if not known
     */
    public static final int getPid() {
        return pid;
    }

    private static final int pid;

    static {
        int tempPid = -1;

        try {
            String pids = ManagementFactory.getRuntimeMXBean().getName();
            int index = -1;

            if(StringUtils.ok(pids) && (index = pids.indexOf('@')) >= 0) {
                tempPid = Integer.parseInt(pids.substring(0, index));
            }
        }
        catch(Exception e) {
            tempPid = -1;
        }

        // final assignment
        pid = tempPid;
    }
}
