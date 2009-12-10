/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.launcher;

import java.util.*;

/**
 *
 * @author bnevins
 */
class RespawnInfo {
    RespawnInfo(String cn, String cp, String[] a) {
        classname = cn;
        classpath = cp;

        if(a == null)
            a = new String[0];

        args = a;
    }

    void put(Map<String, String> map) throws GFLauncherException {
        validate();
        map.put(PREFIX + "classname", classname);
        map.put(PREFIX + "classpath", classpath);
        putArgs(map);
    }

    private void validate() throws GFLauncherException {
        if(!ok(classname))
            throw new GFLauncherException("respawninfo.empty", "classname");
        if(!ok(classpath))
            throw new GFLauncherException("respawninfo.empty", "classpath");
        // args are idiot-proof
    }

    private void putArgs(Map<String, String> map) throws GFLauncherException {
        int numArgs = args.length;
        StringBuilder argLine = new StringBuilder();

        for(int i = 0; i < numArgs; i++) {
            String arg = args[i];

            if(i != 0)
                argLine.append(SEPARATOR);

            if(arg.indexOf(SEPARATOR) >= 0) {
                // this should not happen.  Only the ultra-paranoid programmer would
                // bother checking for it.  I guess that's me!
                throw new GFLauncherException("respawninfo.illegalToken", arg, SEPARATOR);
            }
            argLine.append(args[i]);
        }

        map.put(PREFIX + "args", argLine.toString());
    }
    
    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private String      classname;
    private String      classpath;
    private String[]    args;

    private static final String PREFIX = "-asadmin-";
    private static final String SEPARATOR = ",,,";
}
