/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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

/**
 * AdminBaseDevTest is a base class for administration CLI dev tests.
 *
 * @author tmueller
 */
package admin;

import com.sun.appserv.test.BaseDevTest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class AdminBaseDevTest extends BaseDevTest {

    @Override
    public void report(String name, boolean success) {
        // bnevins june 6 2010

        // crazy base class uses a Map to store these reports.  If you use
        // the same name > 1 time they are ignored and thrown away!!!
        // I went with this outrageous kludge because (1) it is just tests
        // and (2) there are tens of thousands of other files in this harness!!!

        // another issue is hacking off strings after a space.  Makes no sense to me!!

        String name2 = name.replace(' ', '_');
        if (!name2.equals(name)) {
            System.out.println("Found spaces in the name.  Replaced with underscore. "
                    + "before: " + name + ", after: " + name2);
            name = name2;   // don't foul logic below!
        }

        int i = 0;

        while (reportNames.add(name2) == false) {
            name2 = name + i++;
        }

        if (!name2.equals(name)) {
            System.out.println("Duplicate name found (" + name
                    + ") and replaced with: " + name2);
        }

        super.report(name2, success);
    }

    @Override
    public void report(String step, AsadminReturn ret) {
        report(step, ret.returnValue);
    }

    /*
     * Returns true if String b contains String a.
     */
    static boolean matchString(String a, String b) {
        return b.indexOf(a) != -1;
    }

    static String getURL(String urlstr) {
        try {
            URL u = new URL(urlstr);
            URLConnection urlc = u.openConnection();
            BufferedReader ir = new BufferedReader(new InputStreamReader(urlc.getInputStream(),
                    "ISO-8859-1"));
            StringWriter ow = new StringWriter();
            String line;
            while ((line = ir.readLine()) != null) {
                ow.write(line);
                ow.write("\n");
            }
            ir.close();
            ow.close();
            return ow.getBuffer().toString();
        }
        catch (IOException ex) {
            printf("unable to fetch URL:" + urlstr);
            return "";
        }
    }

    static void printf(String fmt, Object... args) {
        if (DEBUG) {
            System.out.printf("**** DEBUG MESSAGE ****  " + fmt + "\n", args);
        }
    }

    private final SortedSet<String> reportNames = new TreeSet<String>();
    protected final static boolean DEBUG;
    protected final static boolean isHudson = Boolean.parseBoolean(System.getenv("HUDSON"));
    static {
        String name = System.getProperty("user.name");

        if (name != null && name.equals("bnevins"))
            DEBUG = true;
        else if (isHudson)
            DEBUG = true;
        else if (Boolean.parseBoolean(System.getenv("AS_DEBUG")))
            DEBUG = true;
        else
            DEBUG = false;
    }

}
