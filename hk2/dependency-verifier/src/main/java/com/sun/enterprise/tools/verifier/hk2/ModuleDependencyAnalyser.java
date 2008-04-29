/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.tools.verifier.hk2;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoader;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompilerImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ModuleDependencyAnalyser {

    ModuleDefinition moduleDef;
    ClosureCompilerImpl closure;
    Repository moduleRepository;

    String[] excludedPatterns = {"java."
            , "javax."
            , "org.osgi."
            , "org.jvnet.hk2."
            , "com.sun.hk2."
            , "com.sun.enterprise.module."
    };

    public ModuleDependencyAnalyser() {
    }

    public synchronized boolean analyse(ModuleDefinition moduleDef,
                                                                Repository moduleRepository)
            throws IOException {
        this.moduleDef = moduleDef;
        this.moduleRepository = moduleRepository;
        // Make a classpath consisting of only module jar file.
        File moduleJar = new File(moduleDef.getLocations()[0]);
        String classpath = moduleJar.getAbsolutePath();
        ClassFileLoader cfl = ClassFileLoaderFactory.newInstance(new Object[]{classpath});
        closure = new ClosureCompilerImpl(cfl);
        for (String pattern : excludedPatterns) {
            closure.addExcludedPattern(pattern);
        }
        excludeExportedClasses();
        closure.buildClosure(new JarFile(moduleJar));
        if (System.getProperty("debugOutput") != null) {
            Logger.getLogger("apiscan.classfile").setLevel(Level.FINER);
            PrintStream out = new PrintStream(new FileOutputStream(System.getProperty("debugOutput")));
            out.println(closure);
        }
        return closure.getFailed().isEmpty();
    }

    public Map<String, Collection<String>> getResult() {
        return closure.getFailed();
    }

    /**
     * This method adds classes exported by all the bundles that this bundle imports
     * to the list of excluded class names.
     */
    private void excludeExportedClasses() throws IOException {
        ModuleDependency[] dependencies = moduleDef.getDependencies();
        for (ModuleDependency dependency : dependencies) {
            ModuleDefinition requiredModuleDef =
                    moduleRepository.find(dependency.getName(), dependency.getVersion());
            for (String cls : getExportedClasses(requiredModuleDef)) {
                closure.addExcludedClass(cls);
            }
        }
    }

    private Set<String> getExportedClasses(ModuleDefinition moduleDef) throws IOException {
        Attributes attributes = moduleDef.getManifest().getMainAttributes();
        String exportedPkgsAttr = attributes.getValue("Export-Package");
        StringTokenizer st = new StringTokenizer(exportedPkgsAttr, ",", false);
        Set<String> exportedPkgs = new HashSet<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            int idx = token.indexOf(';');
            String pkg = (idx == -1) ? token : token.substring(0, idx);
//            System.out.println("pkg = " + pkg);
            exportedPkgs.add(pkg);
        }
        Set<String> exportedClasses = new HashSet<String>();
        JarFile moduleJar = new JarFile(new File(moduleDef.getLocations()[0]));
        Enumeration<JarEntry> moduleEntries = moduleJar.entries();
        while (moduleEntries.hasMoreElements()) {
            String entry = moduleEntries.nextElement().getName();
            if (entry.endsWith(".class")) {
                String clsName = convertToExternalName(entry);
                String pkgName = getPackageName(clsName);
                if (exportedPkgs.contains(pkgName)) {
                    exportedClasses.add(clsName);
                }
            }
        }
        return exportedClasses;
    }

    private static String convertToExternalName(String internalName) {
        assert (internalName.endsWith(".class"));
        String s = internalName.substring(0, internalName.length() - ".class".length());
        return s.replaceAll("/", ".");
    }

    /**
     * @param className name of class in external format (i.e. java.util.Set).
     * @return package name in dotted format, e.g. java.lang for java.lang.void
     */
    private static String getPackageName(String className) {
        int idx = className.lastIndexOf('.');
        if (idx != -1) {
            return className.substring(0, idx);
        } else
            return "";
    }

    public String getResultAsString() {
        StringBuilder sb = new StringBuilder();
        Map<String, Collection<String>> failed = closure.getFailed();
        if (failed.isEmpty()) return "";
        for (String referencingPath : failed.keySet()) {
            String referencingClass;
            int idx = referencingPath.lastIndexOf('/');
            if (idx == -1) {
                referencingClass = referencingPath;
            } else {
                referencingClass = referencingPath.substring(idx + 1);
            }
            sb.append("\t").append(referencingClass);
            sb.append(" -> {");
            String[] notFoundClasses = failed.get(referencingPath).toArray(new String[0]);
            for (int i = 0; i < notFoundClasses.length; ++i) {
                sb.append(notFoundClasses[i]);
                if (i != (notFoundClasses.length - 1)) { // skip appending ',' at the end
                    sb.append(", ");
                }
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    public void printResult(PrintStream out) {
        out.println(getResultAsString());
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java " + ModuleDependencyAnalyser.class.getName() +
                    " <Repository Dir Path> [Path to module]...");

            System.out.println("Examples:\n" +
                    "1. Following command verifies all modules in the specified repository:\n" +
                    " java " + ModuleDependencyAnalyser.class.getName() +
                    " /tmp/glassfish/modules/\n\n" +
                    "2. Following command verifies only the specified modules in the specified repository:\n" +
                    " java " + ModuleDependencyAnalyser.class.getName() +
                    " /tmp/glassfish/modules/ kernel-10.0-SNAPSHOT.jar amx-impl-10.0SNAPSHOT.jar\n\n");
            return;
        }
        String repoPath = args[0];
        Repository moduleRepository = new DirectoryBasedRepository("repo", new File(repoPath));
        moduleRepository.initialize();
        List<ModuleDefinition> moduleDefs = new ArrayList<ModuleDefinition>();
        if (args.length > 1) {
            for (int i = 1; i < args.length; ++i) {
                File moduleFile = new File(args[i]);
                if (!moduleFile.isAbsolute()) {
                    moduleFile = new File(repoPath, args[i]);
                }
                ModuleDefinition moduleDef = new DefaultModuleDefinition(moduleFile);
                moduleDefs.add(moduleDef);
            }
        } else {
            moduleDefs = moduleRepository.findAll();
        }
        for (ModuleDefinition moduleDef : moduleDefs) {
            ModuleDependencyAnalyser analyser = new ModuleDependencyAnalyser();
            analyser.analyse(moduleDef, moduleRepository);
            System.out.println("<Module name = " + moduleDef.getLocations()[0] + ">");
            System.out.println(analyser.getResultAsString());
            System.out.println("</Module>");
        }
    }

}
