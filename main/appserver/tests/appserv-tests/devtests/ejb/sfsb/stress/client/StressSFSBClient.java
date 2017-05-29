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

package com.sun.s1asdev.ejb.sfsb.stress.client;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;
import java.io.FileInputStream;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSBHome;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSB;

import java.util.ArrayList;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressSFSBClient
    implements Runnable
{

    String	    name;
    StressSFSBHome  home;
    ArrayList	    list;
    int		    maxActiveCount;
    boolean	    success = true;
    int		    maxIter = 5;
    Thread	    thread;

    public StressSFSBClient(String name,
	    StressSFSBHome home, int maxActiveCount)
    {
	thread = new Thread(this, name);
	this.name = name;
	this.home = home;
	this.maxActiveCount = maxActiveCount;
	this.list = new ArrayList(maxActiveCount);
	thread.start();
    }

    public void run() {
	System.out.println("StressSFSBClient: " + name + " started....");
	try {
	    for (int i=0; i<maxActiveCount; i++) {
		list.add(home.create(name+"-"+i));
	    }
	    for (int count = 0; count < maxIter; count++) {
		for (int i=0; i<maxActiveCount; i++) {
		    StressSFSB sfsb = (StressSFSB) list.get(i);
		    sfsb.ping();
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    success = false;
	}
    }

}
