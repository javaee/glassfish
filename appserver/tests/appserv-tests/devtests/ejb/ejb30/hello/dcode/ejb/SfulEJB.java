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

package com.sun.s1asdev.ejb.ejb30.hello.dcode;

import javax.ejb.Stateful;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Init;
import javax.ejb.RemoteHome;

@RemoteHome(SfulHome.class)
@Stateful
public class SfulEJB 
{

    // reference to a Remote Business object from another application
    @EJB(mappedName="ejb_ejb30_hello_session2_Sless") com.sun.s1asdev.ejb.ejb30.hello.session2.Sless sless;

    // reference to a RemoteHome from another application
    @EJB(mappedName="ejb_ejb30_hello_session2_Sless") com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemoteHome slessHome;

    com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemote slessRemote;
    
    @Init
    public void create() {
        System.out.println("In SfulEJB::create");
        try {
            slessRemote = slessHome.create();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String hello() {
        System.out.println("In SfulEJB:hello()");

        System.out.println("Calling sless");

        sless.hello();

        try {        

            slessRemote.hello();

            com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemote 
                anotherRemote = slessHome.create();
        
            anotherRemote.hello();

        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
            
        System.out.println("Called sless.hello()");

        return "hello";
    }

}
