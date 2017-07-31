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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.util.Collection;
import java.util.HashSet;
import javax.ejb.*;
import javax.annotation.Resource;

/**
 *
 * @author marina vatkina
 */

@Singleton
public class MyBean {

    @Resource TimerService ts;
    private volatile int counter = 0;
    private HashSet timers = new HashSet();
    private volatile boolean second_timer_running = false;

    @Schedule(second="*/5", minute="*", hour="*", info = "timer01")
    private void scheduledtimeout(Timer t) {
        test(t, "timer01");
    }

    @Timeout
    private void requestedtimeout(Timer t) {
        second_timer_running = true;
        test(t, "timer02");
    }

    private void test(Timer t, String name) {
        if (((String)t.getInfo()).startsWith(name)) {
            System.err.println("In ___MyBean:timeout___ "  + t.getInfo() + " - persistent: " + t.isPersistent());
            timers.add(t.getInfo());
            counter++;
        } else {
            throw new RuntimeException("Wrong " + t.getInfo() + " timer was called");
        }

    }

    public boolean timeoutReceived(String param) {
        System.err.println("In ___MyBean:timeoutReceived___ " + counter + " times");
        boolean result = (counter > 0);
        if (!second_timer_running)  {
            ts.createTimer(1000, 5000, "timer02 " + param);
        } else {
            result = result && (timers.size() == 3);
        }

        counter = 0;
        return result;
    }
}
