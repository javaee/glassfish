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

package com.sun.s1asdev.timer31.schedule_ann.client;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
//import javax.jms.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb31.timer.schedule_ann.Stles;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB private static Stles bean;

    public static void main(String args[]) { 
        boolean doJms = false; // TODO (args.length == 1) && (args[0].equalsIgnoreCase("jms"));

        stat.addDescription("ejb31-timer-schedule_ann");


        try {
            System.out.println("Waiting timers to expire for schedule_ann timer test");
            Thread.sleep(12000);
            System.out.println("Verifying timers for schedule_ann timer test");
            bean.verifyTimers(); 
            stat.addStatus("schedule_ann: ", stat.PASS );

        } catch(Exception e) {
            stat.addStatus("schedule_ann: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-schedule_ann");
    }

    // when running this class through the appclient infrastructure
    public Client() {
        try {
            context = new InitialContext();
        } catch(Exception e) {
            System.out.println("Client : new InitialContext() failed");
            e.printStackTrace();
            stat.addStatus("Client() ", stat.FAIL);
        }
    }

}
