/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License.  You can
 *  obtain a copy of the License at
 *  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 *  or packager/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at packager/legal/LICENSE.txt.
 *
 *  GPL Classpath Exception:
 *  Oracle designates this particular file as subject to the "Classpath"
 *  exception as provided by Oracle in the GPL Version 2 section of the License
 *  file that accompanied this code.
 *
 *  Modifications:
 *  If applicable, add the following below the License Header, with the fields
 *  enclosed by brackets [] replaced by your own identifying information:
 *   "Portions Copyright [year] [name of copyright owner]"
 *
 *  Contributor(s):
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package org.glassfish.dcom;

import java.util.Random;
import org.jinterop.dcom.test.MSShell;
import org.jinterop.dcom.test.TestWinReg;

/**
 *
 * @author wnevins
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("HELLO DCOM!!");
           TestWinReg.main(getWinRegArgs());
        //MSShell.main(authInfo);
    }

    private static String[] getWinRegArgs() {
        // address domain username password keyname
        String regitem = "rrrr" + new Random(System.nanoTime()).nextLong();
        String[] ret = new String[5];
        ret[0] = authInfo[0];
        ret[1] = authInfo[1];
        ret[2] = authInfo[2];
        ret[3] = authInfo[3];
        ret[4] = regitem;
        System.out.println("REGITEM: " + regitem);
        return ret;
    }
    // address domain username password
    static final String[] authInfo = new String[]{
        //"wnevins-lnr",
        //"wnevins-lnr",
        //"wnevins-lap",
        //"wnevins-lap",
        //"bnevins.com",
        //"bnevins.com",
        //"wnevins",
        //"Benny235",};

        "wnevins-lnr",
        "wnevins-lnr",
        "wnevins",
        "Swteim235",
    };



}
/*
 * %JAVA_HOME%\bin\java -classpath ../../lib;../../lib/j-interopdeps.jar;../../lib/jcifs-1.2.19.jar;../../lib/j-interop.jar
org.jinterop.dcom.test.TestWinReg %1 %2 %3 %4 %5
execute wnevins-lnr wnevins-lnr wnevins password zzzz0000001

d:\gf_other\j-Interop\examples\WinReg>
 */