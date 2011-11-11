/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier.hk2;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.module.impl.HK2Factory;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoader;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompilerImpl;
import com.sun.enterprise.tools.verifier.apiscan.classfile.Util;

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
import java.net.URI;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ModuleDependencyAnalyser {

    ModuleDefinition moduleDef;
    ClosureCompilerImpl closure;
    Repository moduleRepository;
    final Logger logger = Logger.getLogger("apiscan.classfile");

    String[] excludedPatterns = {"java."
            // add all HK2 package patterns, because we don't handle reexport yet.
            , "org.jvnet.hk2."
            , "com.sun.hk2."
            , "com.sun.enterprise.module."
    };
    private File moduleJar;

    /**
     * Create a new analyser.
     * @param moduleDef module whose dependency needs to be analysed
     * @param moduleRepository repository used to satisfy dependencies
     * @throws IOException
     */
    public ModuleDependencyAnalyser(ModuleDefinition moduleDef,
                                    Repository moduleRepository) throws IOException {
        this.moduleDef = moduleDef;
        this.moduleRepository = moduleRepository;
        moduleJar = new File(moduleDef.getLocations()[0]);
        // Make a classpath consisting of only module jar file.
        String classpath = moduleJar.getAbsolutePath();
        ClassFileLoader cfl = ClassFileLoaderFactory.newInstance(new Object[]{classpath});
        closure = new ClosureCompilerImpl(cfl);
        for (String pattern : excludedPatterns) {
            closure.addExcludedPattern(pattern);
        }
        excludeImportedPackages();
        excludeExportedClasses();
    }

    /**
     * Analyse dependency of a module. It uses the repository to look up
     * modules that this module depends on.
     * @return true if all the dependencies are OK, false if something is missing
     * @throws IOException if there is any failure in reading module information
     */
    public synchronized boolean analyse()
            throws IOException {
        closure.buildClosure(new JarFile(moduleJar));
        if (System.getProperty("debugOutput") != null) {
            logger.setLevel(Level.FINER);
            PrintStream out = new PrintStream(new FileOutputStream(System.getProperty("debugOutput")));
            out.println(closure);
        }
        return closure.getFailed().isEmpty();
    }

    public void excludePatterns(Collection<String> patterns) {
        for (String p : patterns) {
            closure.addExcludedPattern(p.trim());
        }
    }

    public void excludePackages(Collection<String> packages) {
        for (String p : packages) {
            closure.addExcludedPackage(p);
        }
    }

    public void excludeClasses(Collection<String> classes) {
        for (String c : classes) {
            closure.addExcludedClass(c);
        }
    }

    /**
     * @see com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompiler#getFailed() for
     * description of the return value.
     * @return a map of referencing class name to collection of unresolved classes
     */
    public Map<String, Collection<String>> getResult() {
        return closure.getFailed();
    }

    /**
     * This method adds packages imported by this bundle to
     * to the list of excluded package names.
     */
    private void excludeImportedPackages() {
        Attributes attributes = moduleDef.getManifest().getMainAttributes();
        String exportedPkgsAttr = attributes.getValue("Import-Package");
        if (exportedPkgsAttr==null) return;
        StringTokenizer st = new StringTokenizer(exportedPkgsAttr, ",", false);
        Set<String> importedPkgs = new HashSet<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            int idx = token.indexOf(';');
            String pkg = (idx == -1) ? token : token.substring(0, idx);
//            System.out.println("pkg = " + pkg);
            importedPkgs.add(pkg);
        }
        for (String pkg : importedPkgs) {
            closure.addExcludedPackage(pkg);
        }
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
        Set<String> exportedPkgs = new HashSet<String>();
        java.util.jar.Manifest m = moduleDef.getManifest();
        if (m==null) return exportedPkgs;
        Attributes attributes = m.getMainAttributes();
        String exportedPkgsAttr = attributes.getValue("Export-Package");
        StringTokenizer st = new StringTokenizer(exportedPkgsAttr, ",", false);
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
                String clsName = Util.convertToExternalClassName(
                        entry.substring(0, entry.length() - ".class".length()));
                String pkgName = Util.getPackageName(clsName);
                if (exportedPkgs.contains(pkgName)) {
                    exportedClasses.add(clsName);
                }
            }
        }
        return exportedClasses;
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
        File f = new File(repoPath) {
            @Override public File[] listFiles() {
                List<File> files = new ArrayList<File>();
                for (File f : super.listFiles()) {
                    if (f.isDirectory()) {
                        for (File f2 : f.listFiles()) {
                            if (f2.isFile() && f2.getName().endsWith(".jar")) {
                                files.add(f2);
                            }
                        }
                    } else if (f.isFile() && f.getName().endsWith(".jar")) {
                        files.add(f);
                    }
                }
                return files.toArray(new File[files.size()]);
            }
        };
        HK2Factory.initialize();
        Repository moduleRepository = new DirectoryBasedRepository("repo", f);
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
        List<URI> badModules = new ArrayList<URI>();
        for (ModuleDefinition moduleDef : moduleDefs) {
            ModuleDependencyAnalyser analyser =
                    new ModuleDependencyAnalyser(moduleDef, moduleRepository);
            if (System.getProperty("ExcludedPatterns")!=null) {
                StringTokenizer st = new StringTokenizer(
                        System.getProperty("ExcludedPatterns"), ",", false);
                Set<String> patterns = new HashSet<String>();
                while (st.hasMoreTokens()) {
                    patterns.add(st.nextToken());
                }
                analyser.excludePatterns(patterns);
            }
            if (!analyser.analyse()) {
                URI badModule = moduleRepository.getLocation().relativize(moduleDef.getLocations()[0]);
                badModules.add(badModule);
                System.out.println("<Module name = " + badModule + ">");
                System.out.println(analyser.getResultAsString());
                System.out.println("</Module>");
            }
        }
        if (badModules.isEmpty()) {
            System.out.println("All modules are OK");
        } else {
            System.out.println("Dependencies are not correctly set up for following modules:");
            for (URI badModule : badModules) {
                System.out.print(badModule + " ");
            }
            System.out.println("");
        }
    }

}
