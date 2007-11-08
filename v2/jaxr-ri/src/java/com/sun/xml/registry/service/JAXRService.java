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
 * JAXRService.java
 *
 * Created on November 9, 2001, 2:25 PM
 */
package com.sun.xml.registry.service;

import com.sun.xml.registry.common.*;
import com.sun.xml.registry.common.util.*;
import java.util.*;
import javax.naming.*;
import javax.xml.registry.*;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class simply instantiates a jaxr ConnectionFactoryImpl
 * and binds it in a namespace.
 *
 * @author  bbissett
 */
public class JAXRService {
    
    Logger logger = Logger.getLogger(com.sun.xml.registry.common.util.Utility.LOGGING_DOMAIN + ".service");
    private static JAXRService instance;
    
    private JAXRService() {
        // nothing needed
    }
    
    /**
     * This is a singleton class.
     *
     * @return The single instance of the JAXRService class
     */
    public static JAXRService getInstance() {
        if (instance == null) {
            instance = new JAXRService();
        }
        return instance;
    }

    /**
     * 
     */
    void startService() {
        try {
            ConnectionFactoryImpl factory = new ConnectionFactoryImpl();
            Context ctx = new InitialContext();
            ctx.rebind("javax.xml.registry.ConnectionFactory", factory);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    void stopService() {
        try {
            Context ctx = new InitialContext();
            ctx.unbind("JAXRConnectionFactory");
            ctx.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            showUsage();
        }

        JAXRService service = JAXRService.getInstance();
        String command = args[0];
        
        if (command.equals("-startService")) {
            service.startService();
        } else if (command.equals("-stopService")) {
            service.stopService();
        } else {
            showUsage();
        }
    }

    private static void showUsage() {
        System.err.println("Must specify -startService or -stopService");
        System.exit(-1);
    }
        
}
