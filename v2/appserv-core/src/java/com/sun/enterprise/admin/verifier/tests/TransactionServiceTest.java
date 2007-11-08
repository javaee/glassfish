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

/* Test Case which validates the Transaction Service Fields
 * Author : srini@sun.com
 **/

import java.io.File;

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

public class TransactionServiceTest extends ServerXmlTest implements ServerCheck {
    // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public TransactionServiceTest() {
    }
 
    // check method called by command line verifier
    public Result check(ConfigContext context) {
        Result result;
        result = super.getInitializedResult();
        // 8.0 XML Verifier
        /*try {
            Server server = (Server)context.getRootConfigBean();
            TransactionService txn = server.getTransactionService();
            String file = txn.getTxLogDir();
            // <addition> srini@sun.com Bug : 4698904
            if(file==null || file.equals("")) 
                result.failed("File Name cannot be Null");
            else {
                File f = new File(file);
                if(f.exists())
                    result.passed("Transaction Log Dir valid");
                else
                    result.failed("Invalid Transaction Log Directory - " + file);
            }
            String heuDecision = txn.getHeuristicDecision();
            if(heuDecision.equals("rollback") || heuDecision.equals("commit"))
                result.passed("Heurisitic Decision Valid");
            else
                result.failed("Invalid Heuristic Decision - " + heuDecision );
            // Bug : 4713369 <addition>
            try {
                String resTime = txn.getTimeoutInSeconds();
                if(Integer.parseInt(resTime) < 0) {
                    result.failed(smh.getLocalString(getClass().getName()+".resTimeoutNegative","Response Timeout cannot be negative number"));
                }
                else
                    result.passed("Passed ***");
            }catch(NumberFormatException e) {
                result.failed(smh.getLocalString(getClass().getName()+".resTimeoutInvalid","Response Timeout : invalid number"));
            }
            try {
                String keyPointInterval = txn.getKeypointInterval();
                if(Integer.parseInt(keyPointInterval) < 0) {
                    result.failed(smh.getLocalString(getClass().getName()+".keyPointNegative","Key Point Interval cannot be negative number"));
                }
                else
                    result.passed("Passed ***");
            }catch(NumberFormatException e) {
                result.failed(smh.getLocalString(getClass().getName()+".keyPointInvalid","Key Point Interval : invalid number"));
            }
            // Bug : 4713369 </addition>
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
    
    // check method called by iasadmin and adminGUI
    public Result check(ConfigContextEvent ccce) {
        Result result;
        result = new Result();
        Object value = ccce.getObject();
        String beanName = ccce.getBeanName();
        if(beanName!=null) {
            String name = ccce.getName();
            return testSave(name,(String)value);
        }
        
        TransactionService txn = (TransactionService)value;
        // <addition> srini@sun.com Bug : 4698904
        String file = txn.getTxLogDir();
        if(file==null || file.equals("")) {
                result.failed("File Name cannot be Null");
                return result;
        }
        // </addition>
        File f = new File(txn.getTxLogDir());
        if(f.exists())
            result.passed("Transaction Log Dir valid");
        else
            result.failed("Invalid Transaction Log Directory");
        String heuDecision = txn.getHeuristicDecision();
        if(heuDecision.equals("rollback") || heuDecision.equals("commit"))
            result.passed("Heurisitic Decision Valid");
        else
            result.failed("Invalid Heuristic Decision");
        return result;
    }
    
    public Result testSave(String name, String value) {
	    Result result = new Result();
            result.passed("Passed **");
	    if(name.equals(ServerTags.TX_LOG_DIR)){
                // <addition> srini@sun.com Bug : 4698904
                if(value==null || value.equals("")) {
                    result.failed("File Name cannot be Null");
                    return result;
                }
                // </addition>
                File f = new File(value);
                if(f.exists())
                    result.passed("Transaction Log Dir valid");
                else
                    result.failed("Invalid Transaction Log Directory");
            }
            else if(name.equals(ServerTags.HEURISTIC_DECISION)){
                if(value.equals("rollback") || value.equals("commit"))
                    result.passed("Heurisitic Decision Valid");
                else
                    result.failed("Invalid Heuristic Decision");
            }
            // Bug : 4713369 <addition>
            else if(name.equals(ServerTags.TIMEOUT_IN_SECONDS)) {
                try {
                    if(Integer.parseInt(value) < 0)
                        result.failed(smh.getLocalString(getClass().getName()+".resTimeoutNegative","Response Timeout cannot be negative number"));
                    else
                        result.passed("Passed ***");
                } catch(NumberFormatException e) {
                    result.failed(smh.getLocalString(getClass().getName()+".resTimeoutInvalid","Response Timeout : invalid number"));
                }
            }
            else if(name.equals(ServerTags.KEYPOINT_INTERVAL)) {
                try {
                    if(Integer.parseInt(value) < 0)
                        result.failed(smh.getLocalString(getClass().getName()+".keyPointNegative","Key Point Interval cannot be negative number"));
                    else
                        result.passed("Passed ***");
                } catch(NumberFormatException e) {
                    result.failed(smh.getLocalString(getClass().getName()+".keyPointInvalid","Key Point Interval : invalid number"));
                }
            }
           // Bug : 4713369 </addition>
            
            return result;
    }
}
