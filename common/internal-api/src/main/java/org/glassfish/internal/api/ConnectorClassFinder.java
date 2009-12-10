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
package org.glassfish.internal.api;

import com.sun.enterprise.loader.ASURLClassLoader;

/**
 * connector-class-finder to provide a class from its .rar
 *
 * @author Jagadish Ramu
 */
public class ConnectorClassFinder extends ASURLClassLoader implements DelegatingClassLoader.ClassFinder {

        private DelegatingClassLoader.ClassFinder librariesClassFinder;
        private String raName;

    public ConnectorClassFinder(ClassLoader parent, String raName,
                                              DelegatingClassLoader.ClassFinder librariesClassFinder){
            super(parent);
            this.raName = raName;
            // There should be better approach to skip libraries Classloader when none specified.
            // casting to DelegatingClassLoader is not a clean approach
            if(librariesClassFinder!= null && (librariesClassFinder instanceof DelegatingClassLoader)){
                if(((DelegatingClassLoader)librariesClassFinder).getDelegates().size() > 0){
                    this.librariesClassFinder = librariesClassFinder;
                }
            }
        }

    public Class<?> findClass(String name) throws ClassNotFoundException {
            Class c = null;

            if(librariesClassFinder != null){
                try{
                    c = librariesClassFinder.findClass(name);
                }catch(ClassNotFoundException cnfe){
                    //ignore
                }
                if(c != null){
                    return c;
                }
            }
            return super.findClass(name);
        }

        public Class<?> findExistingClass(String name) {
            if(librariesClassFinder != null){
                Class claz = librariesClassFinder.findExistingClass(name);
                if(claz != null){
                    return claz;
                }
            }
            return super.findLoadedClass(name);
        }

        public String getResourceAdapterName(){
            return raName;
        }

        public void setResourceAdapterName(String raName){
            this.raName = raName;
        }
    }
