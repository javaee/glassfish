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


package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.util.Collection;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface Method {
    /**
     * @return the {@link ClassFile} this method belongs to.
     */
    ClassFile getOwningClass();

    /**
     * @return the name of the method.
     */
    String getName();

    /**
     * @return return the descriptor, e.g. ([Ljava.lang.String;)V
     */
    String getDescriptor();

    /**
     * @return return the access flags.
     */
    int getAccess();

    /**
     * Used only when method's parameters or return type use generics.
     * @return
     */
    String getSignature();

    /**
     * @return the internal names of the method's exception classes. May be null.
     */
    String[] getExceptions();

    /**
     *
     * @return an unmodifiable collection of method references representing
     * the methods that are invoked directly (i.e. not recurssively) from this
     * method.
     */
    Collection<MethodRef> getReferencedMethods();

    /**
     *
     * @return an unmodifiable collection of class names in external format
     * representing the classes that are directly (i.e. not recurssively)
     * referenced by this method.
     */
    Collection<String> getReferencedClasses();

    /**
     * @return a reference that represents this method.
     */
    MethodRef getSelfReference();

    /**
     *
     * @return true if this is a native method else false
     */
    boolean isNative();

}
