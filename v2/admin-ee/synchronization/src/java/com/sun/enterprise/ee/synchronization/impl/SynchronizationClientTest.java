/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.ee.synchronization.impl;

import java.io.File;


/**
 * Synchronization client test obj.
 *
 * @since  JDK1.4
 */
public class SynchronizationClientTest {

    public static void main(String[] args) {

        if (( args.length < 1 ) || (args.length > 8)) {
            System.out.println("Usage: SyncClientTest <hostname:string> <port:int> <username:string> <password:string> <get|put> <source:string> <destination dir/file:string> <instName:string>");
            return;
        }
        
        try {
            //Workaround the removal of the corresponding connstructor, since the constructor does not
            //allow passing in the http(s) protocol information
            //SynchronizationClientImpl sc = new SynchronizationClientImpl(args[0],Integer.parseInt(args[1]), args[2], args[3], args[7] );
            SynchronizationClientImpl sc = new SynchronizationClientImpl(args[7] );

            System.out.println("Sync client initialized, connecting to remote host \n");

            sc.connect();

            System.out.println("connection established \n");

            if ( args[4].equals("get") ) {
                sc.get(args[5], new File(args[6]));
                return;
            }

           if ( args[4].equals("put") ){
                sc.put(new File(args[5]), args[6]);
                return;
           }

            System.out.println(" commands can be either get or put , please retry");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
