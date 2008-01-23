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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
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

    public static  Logger getDefaultLogger() {
        return Logger.getAnonymousLogger();
    }
    
    public static boolean isLoggable(Level level) {
        return false;
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
