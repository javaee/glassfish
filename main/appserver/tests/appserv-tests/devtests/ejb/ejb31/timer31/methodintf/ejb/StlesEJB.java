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

package ejb31.timer.methodintf;

import javax.ejb.*;
import javax.annotation.Resource;

@Stateless
public class StlesEJB implements Stles {

    @Resource TimerService ts;

    private static boolean ct = false;
    private static boolean t1 = false;
    private static boolean t2 = false;
    private static boolean t3 = false;

    @Timeout
    private void timeout1(Timer t) {
        t1 = validate(false);
        t.cancel();
    }

    @Schedule(second="*", minute="*", hour="*", info="timer-every-sec")
    private void timeout2(Timer t) {
        t2 = validate(true);
        t.cancel();
    }

    @Schedule(second="4/2", minute="*", hour="*", dayOfWeek="0-7", info="another-timer")
    private void timeout3(Timer t) {
        t3 = validate(false);
        t.cancel();
    }

    public void createTimer() {
        ct = validate(true);
        ScheduleExpression se = new ScheduleExpression().second("*/5").minute("*").hour("*");
        TimerConfig tc = new TimerConfig("timer-5-sec", true);
        ts.createCalendarTimer(se, tc);
    }

    private void log(Timer t) {
        System.out.println("in StlesEJB:timeout "  + t.getInfo() + " - persistent: " + t.isPersistent());
    }

    private boolean validate(boolean op) {
        boolean valid = true;
        try {
            javax.transaction.TransactionSynchronizationRegistry r = (javax.transaction.TransactionSynchronizationRegistry)
                   new javax.naming.InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            System.out.println("========> TX Status for " + op + " : " + r.getTransactionStatus());
            if (op && r.getTransactionStatus() != javax.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: NON-Active transaction");
                valid = false;
            } else if (!op && r.getTransactionStatus() == javax.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: Active transaction");
                valid = false;
            }
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
            valid = false;
        }

        return valid;
    }

    public boolean verifyTimers() {
        return ct && t1 && t2 && t3;
    }

}
