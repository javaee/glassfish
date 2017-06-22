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

package com.sun.s1asdev.ejb32.ejblite.timer;

import javax.ejb.*;
import javax.annotation.*;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FooBMT extends TimerStuffImpl implements Foo {

    @Resource private SessionContext sc;

    @Timeout
    @Schedule(second="*/2", minute="*", hour="*", info="Automatic BMT", persistent=false)
    public void timeout(Timer t) {
        try {
            System.out.println("In FooBMT::Timeout --> " + t.getInfo());
            if (t.isPersistent())
                throw new RuntimeException("FooBMT::Timeout -> " 
                       + t.getInfo() + " is PERSISTENT!!!");
        } catch(RuntimeException e) {
            System.out.println("got exception while calling getInfo");
            throw e;
        }

        try {
            handleTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleTimeout threw exception");
            e.printStackTrace();
        }

    }

    @PostConstruct
    private void init() throws EJBException {
	System.out.println("In ejblite.timer.Foo::init !!");
        isBMT = true;
        setContext(sc);
        getTimerService("init", true);
        doTimerStuff("init", false);
    }

    @PreDestroy
    public void remove() throws EJBException {
        System.out.println("In FooBMT::remove");
        getTimerService("remove", true);
        doTimerStuff("remove", false);
    }

}
