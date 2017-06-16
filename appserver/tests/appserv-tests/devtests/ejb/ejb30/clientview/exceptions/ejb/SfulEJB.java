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

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import javax.ejb.*;
import javax.annotation.Resource;
import javax.annotation.PreDestroy;
import javax.annotation.security.DenyAll;

@Stateful
@AccessTimeout(0)
public class SfulEJB implements 
    SfulBusiness, SfulRemoteBusiness, SfulRemoteBusiness2
{
    int state = 0;

    private @Resource SessionContext ctx;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void forceTransactionRequiredException() {}

    @Remove
    public void remove() {}

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void forceTransactionRolledbackException() {
        System.out.println("In SfulEJB::forceTransactionRolledbackException");
    }

    public void throwRuntimeAppException() throws RuntimeAppException {
        throw new RuntimeAppException();
    }

    public void throwRollbackAppException() throws RollbackAppException {
        throw new RollbackAppException();
    }

    @PreDestroy
    public void beforeDestroy() {
        System.out.println("In @PreDestroy callback in SfulEJB");
    }

    public void sleepFor(int sec) {
	try {
	    for (int i=0 ; i<sec; i++) {
		Thread.currentThread().sleep(1000);
	    }
	} catch (Exception ex) {
	}
    }

    public void ping() {
    }

    public void pingRemote() {
    }

    public void foo() {
    }

    @DenyAll
    public void denied() {


    }
}
