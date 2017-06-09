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

package com.sun.s1asdev.ejb31.timer.reschedule_after_failures;

import javax.ejb.*;
import javax.interceptor.InvocationContext;
import javax.annotation.Resource;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Stateless
public class StlesEJB implements Stles {

    @Resource
    private TimerService timerSvc;

    private static volatile int i = 0;
    private static volatile boolean b = false;

    public void createTimers() throws Exception {

        Calendar now = new GregorianCalendar();
        int month = (now.get(Calendar.MONTH) + 1); // Calendar starts with 0
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        System.out.println("createTimers(): creating timer with 8 sec ");
        ScheduleExpression se = new ScheduleExpression().second("*/8").minute("*").hour("*");
        TimerConfig tc = new TimerConfig("timer-8-sec", true);
        timerSvc.createCalendarTimer(se, tc);
    }

    public void verifyTimers() {
        if (!b) {
            throw new EJBException("Timer was not rescheduled!");
        }
    }

    @Timeout
    public void timeout(Timer t) {

        System.out.println("in StlesEJB:timeout "  + t.getInfo() + " - persistent: " + t.isPersistent());
        if (i < 2) {
            i++;
            throw new RuntimeException("Failing number " + i);
        }
        b = true;
    }

}
