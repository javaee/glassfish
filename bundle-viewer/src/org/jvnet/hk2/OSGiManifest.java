/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.jvnet.hk2;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Kohsuke Kawaguchi
 */
public class OSGiManifest {
    public final Set<ExportedPackage> exportPackages;

    public final Set<RequiredBundle> requiredBundles;

    public final Set<ImportedPackage> importedPackages;

    public final Set<String> privatePackages;

    public final String version;

    public final String symbolName;

    /**
     * Human-readable name of the bundle.
     */
    public final String name;

    public OSGiManifest(Manifest m) {
        Attributes atts = m.getMainAttributes();

        name = atts.getValue("Bundle-Name");
        version = atts.getValue("Bundle-Version");
        symbolName = atts.getValue("Bundle-SymbolicName");

        {
            Set<ImportedPackage> ipkgs = new TreeSet<ImportedPackage>();
            String ip = atts.getValue("Import-Package");
            if(ip!=null) {
                Lexer sc = new Lexer(ip);
                while(!sc.isEmpty())
                    ipkgs.add(new ImportedPackage(sc));
            }
            importedPackages = Collections.unmodifiableSet(ipkgs);
        }

        {
            String pp = atts.getValue("Private-Package");
            if(pp!=null)
            privatePackages = Collections.unmodifiableSet(new TreeSet<String>(Arrays.asList(pp.split(","))));
            else
                privatePackages = Collections.emptySet();
        }

        {
            Set<ExportedPackage> pkgs = new TreeSet<ExportedPackage>();
            String ep = atts.getValue("Export-Package");
            if(ep!=null) {
                Lexer sc = new Lexer(ep);
                while(!sc.isEmpty())
                    pkgs.add(new ExportedPackage(sc));
            }
            exportPackages = Collections.unmodifiableSet(pkgs);
        }

        {
            Set<RequiredBundle> bundles = new TreeSet<RequiredBundle>();
            String rb = atts.getValue("Require-Bundle");
            if(rb!=null) {
                Lexer sc = new Lexer(rb);
                while(!sc.isEmpty())
                    bundles.add(new RequiredBundle(sc));
            }
            requiredBundles = Collections.unmodifiableSet(bundles);
        }
    }
}
