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

package com.sun.s1asdev.jdbc.stress.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.stress.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.stress.ejb.SimpleBMP;

public class SimpleBMPClient {
    
    // Invoke with args: #clients, #minutes
    public static void main(String[] args) throws Exception {

        int numClients = 10; //10 clients 
	int numMinutes = 10; // 10 minutes
	if (args.length == 2) {
            numClients = Integer.parseInt( args[0] );
	    numMinutes = Integer.parseInt( args[1] );
	}
        
        SimpleBMPClient client = new SimpleBMPClient();
	System.out.println("-=-=-=-=-=-=- Running for "+ numMinutes
	    +" minutes -=-=-=-=-=-=-=-");
	client.runTest( numClients, numMinutes );
    }	
    
    public void runTest(int numClients, int numMinutes ) throws Exception {

        SimpleBMPClientThread[] threads = new SimpleBMPClientThread[ numClients ];
	for (int i = 0 ; i < numClients; i++ ) {
	    try {
	        threads[i] = new SimpleBMPClientThread(i);
	    } catch( Exception e) {
	        System.out.println("Could not create thread : " + i);
		e.printStackTrace();
	    }
	    threads[i].start();
	}
        
	//Let it all run for few hours and then kill all threads
	System.out.println("Waiting for threads to do work now...");
	try {
	    //numMinutes min * 60 sec * 1000 = millis
	    Thread.sleep( numMinutes * 60 * 1000 );
	} catch(InterruptedException ie) {
	    ie.printStackTrace();
	}

	System.out.println("Interrupting threads now...");
	for (int i = 0 ; i < numClients; i++ ) {
	    threads[i].runFlag = false;
	}
    }	
}
