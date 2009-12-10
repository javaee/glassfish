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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin.commands;

/** An interface to get the information about the JVM which the appserver is running.
 * This interface is intended to replace the traditional techniques to get thread
 * dump from a JVM. This is the interface of the MBean that will implement the 
 * JMX based techniques in JDK 1.5+ platform to get interesting information about
 * the JVM itself.
 */
public interface JVMInformationMBean {
    
    public String getThreadDump(final String processName);
    
    public String getClassInformation(final String processName);
    
    public String getMemoryInformation(final String processName);
    
    public String getSummary(final String processName);
    
    public String getLogInformation(String processName);
}
