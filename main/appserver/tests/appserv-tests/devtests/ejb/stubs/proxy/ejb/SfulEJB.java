/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.stubs.proxy;

import javax.ejb.*;

public class SfulEJB implements SessionBean
{

    private SessionContext sc_ = null;

    public SfulEJB(){}

    public void ejbCreate() {}

    public void notSupported() {}
    public void required() {}
    public void requiresNew() {}
    public void mandatory() {}
    public void never() {}
    public void supports() {}

    public void setSessionContext(SessionContext sc)
    {
        sc_ = sc;
    }

    public void ejbRemove() 
    { 
        System.out.println("In SfulEJB.ejbRemove(). about to throw exception");
        throw new RuntimeException("test cleanup for case where " +
                                   "ejbRemove throws an exception");
    }

    public void ejbActivate() 
    {}

    public void ejbPassivate()
    {}

 public void testException1() throws Exception {
        throw new Exception("testException1");
    }

    // will throw ejb exception
    public void testException2() {
        throw new EJBException("testException2");
    }

    // throws some checked exception which is a subclass of the declared
    // checked exception
    public void testException3() throws javax.ejb.FinderException {
        throw new ObjectNotFoundException("testException3");
    }

    // throws some checked exception
    public void testException4() throws javax.ejb.FinderException {
        throw new FinderException("testException4");
    }


    public void testPassByRef1(int a) {

    }

    public void testPassByRef2(Helper1 helper1) {
        helper1.a++;
        helper1.b = helper1.b + "SfulEJB::testPassByRef2";
    }

    public void testPassByRef3(Helper2 helper2) {
        helper2.a++;
        helper2.b = helper2.b + "SfulEJB::testPassByRef3";
    }

    public void testPassByRef4(CommonRemote cr) {

    }

    public Helper1 testPassByRef5() {
        Helper1 h1 = new Helper1();
        h1.a = 1;
        h1.b = "SfulEJB::testPassByRef5";
        return h1;
    }

    public Helper2 testPassByRef6() {
        Helper2 h2 = new Helper2();
        h2.a = 1;
        h2.b = "SfulEJB::testPassByRef6";
        return h2;    
    }

    public CommonRemote testPassByRef7() {
        return (CommonRemote) sc_.getEJBObject();
    }

    public int testPassByRef8() {
        return 8;
    }

}
