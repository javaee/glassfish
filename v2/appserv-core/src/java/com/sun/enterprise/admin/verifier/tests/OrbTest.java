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

package com.sun.enterprise.admin.verifier.tests;

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

import java.net.*;

/**
 * Test Case to check the validity of Orb fields
 */

// 8.0 XML Verifier
//import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import  com.sun.enterprise.config.serverbeans.*;

import com.sun.enterprise.admin.verifier.*;
// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;


public class OrbTest extends ServerXmlTest implements ServerCheck {
    // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public static final String ERROR_MSG = "Message Fragment Size can be only 1024, 2048, 4096, 8192, 16284 or 32568";
    
    public OrbTest() {
    }

    // check method called by command line verifier
    public Result check(ConfigContext context)
    {
        Result result;
        String msgFragmentSize = null;
        
        result = super.getInitializedResult();
        // 8.0 XML Verifier
        /*try {
            Server server = (Server)context.getRootConfigBean();
            IiopService service = server.getIiopService();
            Orb orb = service.getOrb();
            msgFragmentSize = orb.getMessageFragmentSize();
            
            int size = Integer.parseInt(msgFragmentSize);
            
            int kSize = size/1024;
            int remainder = size%1024;
            
            if (remainder != 0) {
                result.failed(ERROR_MSG);
            }
            else if (!((kSize == 1) || (kSize == 2) || (kSize == 4) || (kSize == 8) || (kSize == 16) || (kSize == 32))) {
                result.failed(ERROR_MSG);
            } else result.passed("Valid Message Fragment Size");
            //Bug 4713369 <addition>
            String steadyPool = orb.getSteadyThreadPoolSize();
            try {
                if(Integer.parseInt(steadyPool) < 0)
                    result.failed(smh.getLocalString(getClass().getName()+".steadyThreadNegative","Steady Thread Pool Size cannot be negative number"));
            } catch(NumberFormatException e) {
                    result.failed(smh.getLocalString(getClass().getName()+".steadyThreadInvalid","Steady Thread Pool Size : invalid number"));
            }
            String maxPool = orb.getMaxThreadPoolSize();
            try {
                if(Integer.parseInt(maxPool) < 0)
                    result.failed(smh.getLocalString(getClass().getName()+".maxPoolNegative","Max Thread Pool Size cannot be negative number"));
            } catch(NumberFormatException e) {
                result.failed(smh.getLocalString(getClass().getName()+".maxPoolInvalid","Max Thread Pool Size : invalid number"));
            }
            String idleTimeout = orb.getIdleThreadTimeoutInSeconds();
            try {
                if(Integer.parseInt(idleTimeout) < 0)
                    result.failed(smh.getLocalString(getClass().getName()+".idleTimeoutNegative","Idle Thread Timeout cannot be negative number"));
            } catch(NumberFormatException e) {
                result.failed(smh.getLocalString(getClass().getName()+".idleTimeoutInvalid","Idle Thread Timeout : invalid number"));
            }
            String conn = orb.getMaxConnections();
            try {
                if(Integer.parseInt(conn) < 0)
                    result.failed(smh.getLocalString(getClass().getName()+".maxConnNegative","Max Connections cannot be negative number"));
            } catch(NumberFormatException e) {
                result.failed(smh.getLocalString(getClass().getName()+".maxConnInvalid","Max Connections : invalid number"));
            }
            //Bug 4713369 </addition>
            
        }
        catch (NumberFormatException nfe) {
            result.failed("Message Fragment Size - " + msgFragmentSize + " : Invalid");
        }
        catch(Exception ex) {
            //<addition author="irfan@sun.com" [bug/rfe]-id="logging" >
            /*ex.printStackTrace();
            result.failed("Exception : " + ex.getMessage());*/
            /*_logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            result.failed("Exception : " + ex.getMessage());
            //</addition>
        }*/
        return result;
    }
    
    // check method called by adminGUI and iasadmin
    public Result check(ConfigContextEvent ccce) {
        Result result = new Result();
        result.passed("Passed **");
        
        Object value  = ccce.getObject();
        String choice = ccce.getChoice();
        ConfigContext context = ccce.getConfigContext();
        String beanName = ccce.getBeanName();
        String msgFragmentSize = null;
        
        if(beanName!=null)
                return validateAttribute(ccce.getName(), ccce.getObject());
        
        return result;
    }
    
    public Result validateAttribute(String name, Object value) {
        Result result = new Result();
        result.passed("Passed **");
        String msgFragmentSize = null;
        
        if(name.equals(ServerTags.MESSAGE_FRAGMENT_SIZE)) {
            try {
                msgFragmentSize = (String) value;
                int size = Integer.parseInt(msgFragmentSize);

                int kSize = size/1024;
                int remainder = size%1024;

                if (remainder != 0) {
                    result.failed(ERROR_MSG);
                }
                else if (!((kSize == 1) || (kSize == 2) || (kSize == 4) || (kSize == 8) || (kSize == 16) || (kSize == 32))) {
                    result.failed(ERROR_MSG);
                } else result.passed("Vaild Message Fragment Size");
            } catch (NumberFormatException nfe) {
                result.failed("Message Fragment Size - " + msgFragmentSize + " : Invalid");
            }
        }
        if(name.equals(ServerTags.MAX_CONNECTIONS)) {
            try {
                if(value != null) {
                    String conn = (String) value;
                    if(Integer.parseInt(conn) < 0) {
                        result.failed(smh.getLocalString(getClass().getName()+".maxConnNegative","Max Connections cannot be negative number"));
                    }
                    else
                        result.passed("Passed ***");

                }
            } catch(NumberFormatException e) {
                result.failed(smh.getLocalString(getClass().getName()+".maxConnInvalid","Max Connections : invalid number"));
            }
        }
        // Bug 4713369 <addition>
        return result;
    }
}
