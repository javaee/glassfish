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

package com.sun.enterprise.web;

import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;
//import com.sun.enterprise.web.WebContainerAdminEventProcessor;
//import com.sun.enterprise.web.PEWebContainerAdminEventProcessor;
import com.sun.enterprise.web.WebContainerStartStopOperation;
import com.sun.enterprise.web.PEWebContainerStartStopOperation;
import com.sun.enterprise.web.SSOFactory;
import com.sun.enterprise.web.PESSOFactory;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Implementation of WebContainerFeatureFactory which returns web container
 * feature implementations for PE.
 */
@Service(name="com.sun.enterprise.web.PEWebContainerFeatureFactoryImpl")
public class PEWebContainerFeatureFactoryImpl
        implements WebContainerFeatureFactory {
        
    public WebContainerStartStopOperation getWebContainerStartStopOperation() {
        return new PEWebContainerStartStopOperation();
    }
    
    public HealthChecker getHADBHealthChecker(WebContainer webContainer) {
        return new PEHADBHealthChecker(webContainer);
    }
    
    public ReplicationReceiver getReplicationReceiver(EmbeddedWebContainer embedded) {
        return new PEReplicationReceiver(embedded);
    }    
    
    public SSOFactory getSSOFactory() {
        return new PESSOFactory();
    }    

    public VirtualServer getVirtualServer() {
        return new VirtualServer();
    }
    
    public String getSSLImplementationName(){
        return null;
    }

    /**
     * Gets the default access log file prefix.
     *
     * @return The default access log file prefix
     */
    public String getDefaultAccessLogPrefix() {
        return "_access_log.";
    }

    /**
     * Gets the default access log file suffix.
     *
     * @return The default access log file suffix
     */
    public String getDefaultAccessLogSuffix() {
        return ".txt";
    }

    /**
     * Gets the default datestamp pattern to be applied to access log files.
     *
     * @return The default datestamp pattern to be applied to access log files
     */
    public String getDefaultAccessLogDateStampPattern() {
        return "yyyy-MM-dd";
    }

    /**
     * Returns true if the first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     *
     * @return true if first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation. 
     */
    public boolean getAddDateStampToFirstAccessLogFile() {
        return true;
    }

    /**
     * Gets the default rotation interval in minutes.
     *
     * @return The default rotation interval in minutes
     */
    public int getDefaultRotationIntervalInMinutes() {
        return 15;
    }
}
