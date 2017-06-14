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

package com.sun.s1asdev.deployment.ejb30.ear.security;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

@RunAs(value="sunuser")
@DeclareRoles({"j2ee", "sunuser"})
@Stateful
public class SfulEJB implements Sful
{
    @EJB private Sless sless;
    @EJB private SlessLocal slessLocal;
    @Resource private SessionContext sc;

    public String hello() {
        System.out.println("In SfulEJB:hello()");

        try {
            slessLocal.goodMorning();
            throw new RuntimeException("Unexpected success from slessLocal.goodMorning()");
        } catch(Exception ex) {
            System.out.println("Expected failure from slessLocal.goodMorning()");
        }

        try {
            slessLocal.goodBye();
            throw new RuntimeException("Unexpected success from slessLocal.goodBye()");
        } catch(EJBException ex) {
            System.out.println("Expected failure from slessLocal.goodBye()");
        }
        
        System.out.println(slessLocal.hello());
        return sless.hello();
    }

    @RolesAllowed({"j2ee"}) 
    public String goodAfternoon() {
        if (!sc.isCallerInRole("j2ee") || sc.isCallerInRole("sunuser")) {
            throw new RuntimeException("not of role j2ee or of role sunuser");
        }
        return "Sful: good afternoon";
    }

    @DenyAll
    public String goodNight() {
        System.out.println("In SfulEJB:goodNight()");
        return "goodNight";
    }
}
