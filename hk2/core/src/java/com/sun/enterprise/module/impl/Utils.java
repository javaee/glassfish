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

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dochez
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }

    public static void identifyCyclicDependency(ModuleImpl m, Logger logger) {

        StringBuffer tree = new StringBuffer();
        tree.append(m.getName());
        Vector<Module> traversed = new Vector<Module>();
        boolean success = traverseAndFind(m, m, traversed);
        if (success) {
            traversed.remove(0);
            for (Module mod : traversed) {
                tree.append("-->" + mod.getName());
            }
            tree.append("-->" + m.getName());
        logger.log(Level.SEVERE, "Cyclic dependency : " + tree.toString());
        }
    }

    static private boolean traverseAndFind(Module toTraverse, ModuleImpl toFind, Vector<Module> traversed) {

        traversed.add(toTraverse);
        for (ModuleDependency md : toTraverse.getModuleDefinition().getDependencies())  {
            ModulesRegistry registry = toTraverse.getRegistry();
            for (Module mod : registry.getModules()) {
                if (mod.getName().equals(md.getName())) {
                    if (mod!=null) {
                        if (mod.getName().equals(toFind.getName())) {
                            return true;
                        }
                        if (traverseAndFind(mod, toFind, traversed)) {
                            return true;
                        }
                    }

                }
            }
        }
        traversed.remove(toTraverse);
        return false;
    }
}
