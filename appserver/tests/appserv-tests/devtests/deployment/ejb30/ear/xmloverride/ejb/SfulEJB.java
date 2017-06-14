/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.deployment.ejb30.ear.xmloverride;

import javax.annotation.Resource;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.Status;

@RunAs(value="nobody")
@Stateful
@PermitAll
@Resource(name="myDS", type=DataSource.class, mappedName="jdbc/noSuchRes")
public class SfulEJB implements Sful, SessionSynchronization
{
    private boolean inTx = false;
    @EJB private Sless sless;

    @Resource(name="myDS5", lookup="jdbc/__default")
    private DataSource myDS5;

    private DataSource myDS6;

    @Resource(name="myDS7", lookup="jdbc/noexist")
    private DataSource myDS7;

    @Resource(name="myDS8", mappedName="jdbc/noexist2")
    private DataSource myDS8;

    @Resource(name="envEntry1", lookup="java:app/env/value1")
    private Integer envEntry1;

    @Resource(name="envEntry2", lookup="nonexist")
    private Integer envEntry2;

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return sless.hello();
    }

    //@TransactionAttribute(TransactionAttributeType.REQUIRED)
    @DenyAll
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String goodNight(String message) {
        System.out.println("In SfulEJB:goodNight(): " + inTx);
        if (!inTx) {
            throw new IllegalStateException("It should be in transaction.");
        }

        try {
            InitialContext ic = new InitialContext();
            DataSource myDS = (DataSource)ic.lookup("java:comp/env/myDS");
            int loginTimeout = myDS.getLoginTimeout();
            System.out.println("myDS login timeout = " + loginTimeout);
            DataSource myDS2 = (DataSource)ic.lookup("java:comp/env/jdbc/__default");
            int loginTimeout2 = myDS2.getLoginTimeout();
            System.out.println("myDS2 login timeout = " + loginTimeout2);

            DataSource myDS3 = (DataSource)ic.lookup("java:comp/env/myDS3");
            int loginTimeout3 = myDS3.getLoginTimeout();
            System.out.println("myDS3 login timeout = " + loginTimeout3);

            DataSource myDS4 = (DataSource)ic.lookup("java:comp/env/myDS4");
            int loginTimeout4 = myDS4.getLoginTimeout();
            System.out.println("myDS4 login timeout = " + loginTimeout4);

            int loginTimeout5 = myDS5.getLoginTimeout();
            System.out.println("myDS5 login timeout = " + loginTimeout5);

            int loginTimeout6 = myDS6.getLoginTimeout();
            System.out.println("myDS6 login timeout = " + loginTimeout6);

            int loginTimeout7 = myDS7.getLoginTimeout();
            System.out.println("myDS7 login timeout = " + loginTimeout7);

            int loginTimeout8 = myDS8.getLoginTimeout();
            System.out.println("myDS8 login timeout = " + loginTimeout8);

            System.out.println("enEntry1 = " + envEntry1);
            System.out.println("envEntry2 = " + envEntry2);

            if( (envEntry1 == null) || envEntry1.intValue() != 8 ) {
                throw new RuntimeException("invalid enventry1 value");
            }

            if( (envEntry2 == null) || envEntry2.intValue() != 88 ) {
                throw new RuntimeException("invalid enventry2 value");
            }

        } catch(Exception ex) {
            throw new IllegalStateException("Cannot get expected value: " + ex);
        } 
        return "goodNight";
    }

    @RolesAllowed(value={"nobody"})
    public String goodNight(String message1, String message2) {
        System.out.println("In SfulEJB:goodNight(" + message1 + ", " + message2 + ")");
        return "goodNight " + message1 + ", " + message2;
    }

    @PermitAll
    public String bye() {
        System.out.println("In SfulEJB:bye()");
        return "bye";
    }

    // -- implements SessionSynchronization --
    public void afterBegin() {
        inTx = true;
    }

    public void beforeCompletion() {
        inTx = true;
    }

    public void afterCompletion(boolean committed) {
        inTx = false;
    }
}
