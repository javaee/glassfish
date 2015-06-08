/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module;

/**
 * A ModuleDependency instance holds all information necessary to identify 
 * a dependency between modules. Modules can declare their dependency on a 
 * separate module using the name, the version and whether they accept the 
 * sub module implementation to be shared. They can also specify whether or
 * not they want to re-export the sub module public interfaces. Re-exporting
 * means that the sub-module's public interfaces will also be published as 
 * a public interface of the enclosing module.
 *
 * @author Jerome Dochez
 */
public class ModuleDependency {
    
    final private String name;
    final private String version;
    final private boolean shared;
    final private boolean reexport;
    
    /**
     * Create a new instance of ModuleDependency, where the sub module is 
     * idenfied by its name and version. The sub module implementation should 
     * be shared among users of that module
     * @param name the module name
     * @param version the module version
     */
    public ModuleDependency(String name, String version) {
        this.name = name;
        this.version = version;
        this.shared = true;
        this.reexport = false;
    }

    /**
     * Create a new instance of ModuleDependency, where the sub module is 
     * idenfied by its name and version and wheter the containing module 
     * requires a private copy or not
     * @param name the module name
     * @param version the module version
     * @param shared true if the containing module accept a shared copy 
     */
    public ModuleDependency(String name, String version, 
            boolean shared, boolean reexport) {
        this.name = name;
        this.version = version;
        this.shared = shared;
        this.reexport = reexport;
    }
    
    
    /**
     * Returns the module name
     * @return the module name 
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the module version
     * @return the module version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Returns true if the containing module accept a shared implementation
     * of the sub module
     * @return true if shared implementation is acceptable
     */
    public boolean isShared() {
        return shared;
    }
    
    /**
     * Returns true if the containing module is reexporting the public 
     * interfaces of the sub module
     * @return true if reexporting the sub module public interface
     */
    public boolean isReexporting() {
        return reexport;
    }
    
    /**
     * Returns a string representation
     * @return a printable string about myself
     */
    public String toString() {
       return "Module Dependency : " + getName() + ":" + getVersion();
    }
}
