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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.admin.verifier.tests;

/* Test case for checking the validity of HTTP Service 
 * Author : srini@sun.com
 */

import java.io.File;

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

public class HttpServiceTest extends ServerXmlTest implements ServerCheck {
    
    // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public HttpServiceTest() {
    }
 
    // check method called from command line verifier
    public Result check(ConfigContext context)
    {
        Result result;
        result = super.getInitializedResult();
        /*try {
            Server server = (Server)context.getRootConfigBean();
            HttpService http  = server.getHttpService();
            String qos = http.getQosMetricsIntervalInSeconds();
            String qosRecompute = http.getQosRecomputeTimeIntervalInMillis();
            try {
                // <addition> srini@sun.com Bug : 4698904
                if(qos!=null)
                // </addition>
                    Long.parseLong(qos);
                result.passed("Http Service QOS Metric Interval in Seconds Valid " + qos);
            } catch(NumberFormatException e) {
                result.failed("Http Service QOS Metric Interval in Seconds invalid number - " + qos );
            }
            try {
               // <addition> srini@sun.com Bug : 4698904
               if(qosRecompute!=null)
               // </addition>
                    Long.parseLong(qosRecompute);   
               result.passed("Http Service QOS Recompute time interval in millis Valid ");
            } catch(NumberFormatException e) {
                result.failed("Http Service QOS Recompute time Interval in millis Invalid - " + qosRecompute );
            }
            Acl[] aclList = http.getAcl();
            for(int i=0;i<aclList.length;i++){
                String file = aclList[i].getFile();
                File f = new File(file);
                try {
                    if(f.exists())
                        result.passed("Acl File Valid - " + file);
                    else
                        result.failed("Acl File Invalid - " + file);
                }catch(SecurityException e){
                    result.failed("Acl File Not able to access"+ file);
                }
            }
        }
        catch(Exception ex) {
            // Logging
            _logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            result.failed("Exception : " + ex.getMessage());
        }*/
        return result;
    }
    
    // check method called from iasadmin and admin GUI
    public Result check(ConfigContextEvent ccce)
    {
        Result result;
        Object value = ccce.getObject();
        result = new Result();
        String beanName = ccce.getBeanName();
        result.passed("Passed***");
        if(beanName!=null) {
               String name = ccce.getName();
               result = testSave(name,(String)value);
               return result;
        }
/*
        HttpService http = (HttpService)value;
        String qos = http.getQosMetricsIntervalInSeconds();
        String qosRecompute = http.getQosRecomputeTimeIntervalInMillis();
        try {
            // <addition> srini@sun.com Bug : 4698904
            if(qos!=null)
            // </addition>
                Long.parseLong(qos);
            result.passed("QOS Metric Interval Valid Number : Passed");
        } catch(NumberFormatException e) {
            result.failed("QOS Metric Interval Invalid Number " + qos);
        }
        try {
           // <addition> srini@sun.com Bug : 4698904
           if(qosRecompute!=null)
           // <addition> 
                Long.parseLong(qosRecompute);
           result.passed("QOS Recompute Interval Valid : Passed");
        } catch(NumberFormatException e) {
            result.failed(" QOS Recompute time Interval Invalid Number " + qosRecompute);
        }
        Acl[] aclList = http.getAcl();
        for(int i=0;i<aclList.length;i++){
            String file = aclList[i].getFile();
            File f = new File(file);
            try {
                if(f.exists())
                    result.passed("Acl File Valid - " + file + ": Passed");
                else
                    result.failed("Acl File Invalid - " + file + ": Failed");
            }catch(SecurityException e){
                result.failed("Acl File Not able to access "+ file + ": Failed");
            }
        }
*/
        return result;
    }
    
    public Result testSave(String name, String value) {
        Result result = new Result();
        result.passed("passed ** ");
/*
        if(name.equals(ServerTags.QOS_METRICS_INTERVAL_IN_SECONDS)) {
            try {
                // <addition> srini@sun.com Bug : 4698904
                if(value!=null)
                // </addition>
                    Long.parseLong(value);
                result.passed("QOS Metrics Interval Valid Number ");
            } catch(NumberFormatException e) {
                result.failed("QOS Metrics Interval Invalid Number " + value);
            }
        }
        if(name.equals(ServerTags.QOS_RECOMPUTE_TIME_INTERVAL_IN_MILLIS)){
            try {
                // <addition> srini@sun.com Bug : 4698904
                if(value!=null)
                // <addition> 
                     Long.parseLong(value);
                result.passed("QOS Recompute Time Interval Valid Number");
            } catch(NumberFormatException e){
                result.failed("QOS Recompute Time Interval Invalid Number " + value);
            }
        }
*/
        return result;
    }
    
}
