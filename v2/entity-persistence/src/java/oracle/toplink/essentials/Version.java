/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
 * This class stores variables for the version and build Numbers that are used in printouts and exceptions.
 *
 * @author    Eric Gwin
 * @since    1.0,
 *          1.16 Added get and set Methods. Made variables private. - EJG
 */
package oracle.toplink.essentials;

public class Version {
    // The current copyright info for TopLink. 
    private static final String CopyrightString = "Copyright (c) 1998, 2007, Oracle.  All rights reserved.";

    // The current version of TopLink. 
    // This will be used by all product components and included in exceptions.
    private static String product = "Oracle TopLink Essentials";
    private static final String version = "@VERSION@";
    private static final String buildNumber = "@BUILD_NUMBER@";

    /** Keep track of JDK version in order to make some decisions about datastructures. **/
    public static final int JDK_VERSION_NOT_SET = 0;
    public static final int JDK_1_3 = 1;
    public static final int JDK_1_4 = 2;
    public static final int JDK_1_5 = 3;
    public static int JDK_VERSION = JDK_VERSION_NOT_SET;

    public static String getProduct() {
        return product;
    }

    public static void setProduct(String ProductName) {
        product = ProductName;
    }

    public static String getVersion() {
        return version;
    }

    public static String getBuildNumber() {
        return buildNumber;
    }

    /**
    * INTERNAL:
    *  return the JDK version we are using.
    */
    public static int getJDKVersion() {
        if (JDK_VERSION == JDK_VERSION_NOT_SET) {
            String version = System.getProperty("java.version");
            if ((version != null) && version.startsWith("1.5")) {
                useJDK15();
            } else if ((version != null) && version.startsWith("1.4")) {
                useJDK14();
            } else {
                useJDK13();
            }
        }
        return JDK_VERSION;
    }

    public static void useJDK13() {
        JDK_VERSION = JDK_1_3;
    }

    public static void useJDK14() {
        JDK_VERSION = JDK_1_4;
    }

    public static void useJDK15() {
        JDK_VERSION = JDK_1_5;
    }

    public static boolean isJDK13() {
        return getJDKVersion() == JDK_1_3;
    }

    public static boolean isJDK14() {
        return getJDKVersion() == JDK_1_4;
    }

    public static boolean isJDK15() {
        return getJDKVersion() == JDK_1_5;
    }
}
