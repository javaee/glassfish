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

package com.acme;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.*;
import javax.annotation.Resource;

@Singleton
public class SgltTimerBean {
    private static Set<String> expected_infos = new HashSet<String>();
    private static Set<String> errors = new HashSet<String>();

    static {
        expected_infos.add("StlesTimerBean.timer01.p");
        expected_infos.add("StlesTimerBean.timer01.t");
        expected_infos.add("StlesTimerBean.timer02.p");
        expected_infos.add("StlesTimerBean.timer02.t");
        expected_infos.add("SgltTimerBean.timer01.p");
        expected_infos.add("SgltTimerBean.timer01.t");
        expected_infos.add("SgltTimerBean.timer02.p");
        expected_infos.add("SgltTimerBean.timer02.t");
    }

    @Resource
    TimerService ts;

    @Schedules({
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "SgltTimerBean.timer01.p"),
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "SgltTimerBean.timer01.t", persistent = false)
    })
    private void scheduledTimeout(Timer t) {
        test(t, "SgltTimerBean.timer01");
    }

    @Timeout
    private void programmaticTimeout(Timer t) {
        test(t, "SgltTimerBean.timer02");
    }

    private void test(Timer t, String name) {
        if (((String) t.getInfo()).startsWith(name)) {
            System.err.println("In SgltTimerBean:timeout___ " + t.getInfo() + " - persistent: " + t.isPersistent());
        } else {
            throw new RuntimeException("Wrong " + t.getInfo() + " timer was called");
        }

    }

    public void createProgrammaticTimer() {
        System.err.println("In SgltTimerBean:createProgrammaticTimer__ ");
        for (Timer t : ts.getTimers()) {
            if (t.getInfo().toString().contains("SgltTimerBean.timer02")) {
                System.err.println("programmatic timers are already created for SgltTimerBean");
                return;
            }
        }
        ts.createTimer(1000, 5000, "SgltTimerBean.timer02.p");
        ScheduleExpression scheduleExpression = new ScheduleExpression().minute("*").hour("*").second("*/5");
        ts.createCalendarTimer(scheduleExpression, new TimerConfig("SgltTimerBean.timer02.t", false));
    }

    public int countAllTimers(String param) {
        Collection<Timer> timers = ts.getAllTimers();
        System.err.println("In SgltTimerBean:allTimersFound___ " + timers);
        printTimers(timers);
        return verifyTimers(timers, param);
    }

    private void printTimers(Collection<Timer> timers) {
        StringBuffer sb = new StringBuffer("<");
        for(Timer t:timers){
            sb.append(t);
            sb.append(":");
            sb.append(t.getInfo());
            sb.append(", ");
        }
        sb.append(">") ;
        System.err.println(sb.toString());
    }

    private int verifyTimers(Collection<Timer> ts, String param) {
        if("28081".equals(param)) {
            // called from c1in2, remove non-persistent programmatic timers from expectation
            expected_infos.remove("StlesTimerBean.timer02.t");
            expected_infos.remove("SgltTimerBean.timer02.t");
        }

        for(Timer t : ts) {
            String info = "" + t.getInfo();
            if (!expected_infos.contains(info)) {
                errors.add(info);
            }
        }

        if (ts.size() != expected_infos.size()) {
            throw new EJBException("timerSvc.getAllTimers().size() = "
                    + ts.size() + " but we expect " + expected_infos.size());
        }

        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String e : errors) {
                sb.append("" + e).append(", ");
            }
            throw new EJBException("Timers SHOULD NOT found for infos: " + sb.toString() );
        }
        return ts.size();
    }
}
