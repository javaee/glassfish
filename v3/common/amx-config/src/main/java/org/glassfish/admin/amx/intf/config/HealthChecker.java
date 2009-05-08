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
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.config.AMXConfigProxy;

/**
<b>EE only</b>
Each cluster would be configured for a ping based health check mechanism.
Base interface for such a Health Checker.
 */
public interface HealthChecker extends AMXConfigProxy
{
    /**
    Returns the relative URL to ping to determine the health state of a
    listener.
     */
    public String getURL();

    /**
    Sets the relative URL to ping to determine the health state of a
    listener.
     */
    public void setURL(String url);

    /**
    Returns interval, in seconds, between health checks. A value of "0"
    means that the health check is disabled.
     */
    
    public String getIntervalInSeconds();

    /**
    Set the interval, in seconds, between health checks. A value of "0"
    means that the health check will be disabled. Default is 30
    seconds. Must be 0 or greater.
     */
    public void setIntervalInSeconds(String intervalInSeconds);

    /**
    Return the maximum time, in seconds, that a server must respond to a
    health check request to be considered healthy.
     */
    
    public String getTimeoutInSeconds();

    /**
    Set the maximum time, in seconds, that a server must respond to a
    health check request to be considered healthy. Default is 10
    seconds. Must be greater than 0.
     */
    public void setTimeoutInSeconds(String timeoutInSeconds);
}
