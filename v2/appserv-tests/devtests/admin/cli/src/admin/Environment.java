/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
/**
 * Environment -- static methods for figuring out directories, files, etc.
 *
 *
 * @author Byron Nevins
 */
package admin;

import java.io.File;

public final class Environment {
    private static final boolean isHadas;
    private static final File gf_home;

    public static boolean isHadas() {
        return isHadas;
    }

    public static File getGlassFishHome() {
        return gf_home;
    }

    private Environment() {
        // no instances allowed!
    }

    static {
        isHadas = Boolean.getBoolean("HADAS")
                || Boolean.parseBoolean(System.getenv("hadas"))
                || Boolean.parseBoolean(System.getenv("HADAS"));

        File gf_homeNotFinal = null;

        try {
            String home = System.getenv("S1AS_HOME");

            if (home == null) {
                gf_homeNotFinal = null;
                throw new IllegalStateException("No S1AS_HOME set!");
            }

            gf_homeNotFinal = new File(home);

            try {
                gf_homeNotFinal = gf_homeNotFinal.getCanonicalFile();
            }
            catch (Exception e) {
                gf_homeNotFinal = gf_homeNotFinal.getAbsoluteFile();
            }

            if (!gf_homeNotFinal.isDirectory()) {
                gf_homeNotFinal = null;
                throw new IllegalStateException("S1AS_HOME is not pointing at a real directory!");
            }
        }
        catch(IllegalStateException e) {
            // what's the point of struggling on?
            System.out.println("#####  CATASTROPHIC ERROR -- You must set S1AS_HOME to point to the GlassFish installation directory");
            System.exit(2);
        }
        finally {
            gf_home = gf_homeNotFinal;
        }
    }
}
