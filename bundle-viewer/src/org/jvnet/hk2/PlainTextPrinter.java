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

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class PlainTextPrinter implements Printer {
    private final PrintStream out;

    public PlainTextPrinter(PrintStream out) {
        this.out = out;
    }

    public void print(OSGiManifest m) {
        out.println("Bundle-Name:         "+m.name);
        out.println("Bundle-Version:      "+m.version);
        out.println("Bundle-SymbolicName: "+m.symbolName);

        {
            out.println("Export-Packages:");
            int w = Named.getLongestName(m.exportPackages);
            for (ExportedPackage b : m.exportPackages) {
                out.print("  "+pad(b.name,w));
                if (b.include!=null) {
                    out.println();
                    out.print("    "+pad("include: " + b.include,w));
                }
                if(b.version!=null)
                    out.print(" "+b.version);
                out.println();
            }
        }

        {
            out.println("Import-Packages:");
            int w = Named.getLongestName(m.importedPackages);
            for (ImportedPackage b : m.importedPackages) {
                out.print("  "+pad(b.name,w));
                for (Map.Entry<String, Set<String>> resolution : b.resolutions.entrySet()) {
                    out.print("    resolution: " + resolution.getKey());
                    out.println();
                    for (String packageName : resolution.getValue()) {
                        out.print("      " + packageName);
                        out.println();
                    }
                }
                if(b.version!=null)
                    out.print(" "+b.version);
                out.println();
            }
        }

        out.println("Private-Packages:");
        printPackages(m.privatePackages);

        {
            out.println("Require-Bundles:");
            int w = Named.getLongestName(m.requiredBundles);
            for (RequiredBundle b : m.requiredBundles) {
                out.print("  "+pad(b.name,w));
                if(b.version!=null)
                    out.print(" "+b.version);
                out.println();
            }
        }

        out.println();
    }
    
    private void printPackages(Set<String> packages) {
        for (String pkg : packages) {
            out.println("  "+pkg);
        }
    }

    private String pad(String str, int width) {
        while(str.length()<width) {
            str += WHITESPACE.substring(0,Math.min(WHITESPACE.length(), width - str.length()));
        }
        return str;
    }

    private final String WHITESPACE = "                                                      ";
}
