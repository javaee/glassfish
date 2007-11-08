/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * ObjectFactory.java        August 19, 2003, 6:29 AM
 */

package com.sun.enterprise.tools.common.validation.util;

import java.lang.reflect.Constructor;

/**
 * This class is a generic Factory that employes Java reflection to
 * create Objects.
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ObjectFactory {
    /* A class implementation comment can go here. */
    
    /** Create an instance of the class with the specified name by calling the
      * no-argument constructor.
     */
    public static Object newInstance(String className){
        
        Utils utils = new Utils();
        return utils.createObject(className);
    }


    /** Create an instance of the class with the specified name by calling the
      * a constructor that takes an String.
     */
    public static Object newInstance(String className, String argument){
        Class classObject = null;
        Utils utils = new Utils();

        Class[] argumentTypes = new Class[] {String.class};
        Constructor constructor =
            utils.getConstructor(className, argumentTypes);

        Object[] argumentValues = new Object[] {argument};

        return utils.createObject(constructor, argumentValues);
    }


    /** Create an instance of the class with the specified name by calling the
      * a constructor that takes an String.
     */
    public static Object newInstance(String className, Object argument){
        Class classObject = null;
        Utils utils = new Utils();

        Class[] argumentTypes = new Class[] {Object.class};
        Constructor constructor =
            utils.getConstructor(className, argumentTypes);

        Object[] argumentValues = new Object[] {argument};

        return utils.createObject(constructor, argumentValues);
    }    
}
