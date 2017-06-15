/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1as.devtests.ejb.generics;

import javax.ejb.*;
import java.util.*;

/**
 * All business methods in this class are overridden by subclass to verify
 * these business methods are correctly processed.
 */
public abstract class AbstractBaseEJB<T> {
    //abstract method, use parameterized param type with T
    public abstract void doSomething(List<T> t);

    //regular business method, no use of generics param
    public String hello() {
        System.out.println("In AbstractBaseEJB.hello.");
        return "Hello from AbstractBaseEJB.";
    }

    //use parameterized param type with T
    public void doSomething2(List<T> t) {
        System.out.println("In AbstractBaseEJB.doSomething2.");
    }

    //use parameterized return type with T
    public List<T> doSomething3() {
        System.out.println("In AbstractBaseEJB.doSomething3.");
        return null;
    }

    //use TypeVariable generics T as param
    abstract public void doSomething4(T t);

    //superclass has param List<T>, and subclass has param List
    abstract public void doSomething5(List<T> t);

    abstract public void doSomething6(List<List<T>> t);
} 
