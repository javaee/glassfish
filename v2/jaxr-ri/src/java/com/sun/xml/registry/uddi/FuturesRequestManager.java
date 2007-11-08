/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


/*
 * FuturesRequestManager.java
 */
package com.sun.xml.registry.uddi;

/**
 * Current implementation simply spawns a thread for each
 * request that is made. Future work may pool threads or
 * use a worker thread to perform all requests serially.
 */
public class FuturesRequestManager implements Runnable {

    private JAXRCommand jaxrCommand;
    
    static void invokeCommand(JAXRCommand command) {
        FuturesRequestManager manager = new FuturesRequestManager(command);
        new Thread(manager).start();
    }
    
    private FuturesRequestManager(JAXRCommand command){
        jaxrCommand = command;
    }
    
    public void run() {
        try {
            jaxrCommand.execute();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }    
}
