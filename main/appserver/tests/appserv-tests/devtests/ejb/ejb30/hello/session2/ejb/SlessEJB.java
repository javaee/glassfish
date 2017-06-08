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

package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.annotation.Resource;

import javax.ejb.EJB;
import javax.ejb.RemoteHome;
import javax.ejb.CreateException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.transaction.UserTransaction;

@Stateless

// External remote ejb 3.0 reference.  Target ejb doesn't exist
// but this shouldn't cause any problems as long as reference is
// not looked up.  Remote jndi-names are not dereferenced until 
// lookup time, in *our* implementation.  This works for old-style 
// ejb-refs so this TYPE-level @EJB should have the same behavior.
@EJB(name="ejb/External", beanInterface=ExternalBusiness.class)
@Remote(Sless.class)
@RemoteHome(SlessRemoteHome.class)
public class SlessEJB 
{
    @Resource(name="null") SessionContext sc;

    public String hello() {
        System.out.println("In SlessEJB:hello()");
        try {
            Object lookup = sc.lookup(null);
        } catch(IllegalArgumentException iae) {
            System.out.println("Successfully got IllegalArgException for " +
                               "SessionContext.lookup(null)");
        }
        SessionContext sc2 = (SessionContext) sc.lookup("null");
        System.out.println("SessionContext.lookup(\"null\") succeeded");

        return "hello from SlessEJB";
    }

    public String hello2() throws javax.ejb.CreateException {
        throw new javax.ejb.CreateException();
    }

    public String getId() {
        return "SlessEJB";
    }

    public Sless roundTrip(Sless s) {
        System.out.println("In SlessEJB::roundTrip " + s);
        System.out.println("input Sless.getId() = " + s.getId());
        return s;
    }

    public Collection roundTrip2(Collection collectionOfSless) {
        System.out.println("In SlessEJB::roundTrip2 " + 
                           collectionOfSless);
        if( collectionOfSless.size() > 0 ) {
            Sless sless = (Sless) collectionOfSless.iterator().next();
            System.out.println("input Sless.getId() = " + sless.getId());  
        }
        return collectionOfSless;
    }
}
