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

/*
 * MethodStartTO.java
 *
 * Created on July 19, 2005, 6:46 PM
 */

package com.sun.enterprise.admin.monitor.callflow;

/**
 *
 * @author Harpreet Singh
 */
public class MethodStartTO implements TransferObject{
    
    /** Creates a new instance of MethodStartTO */
    public MethodStartTO() {
    }
    
    public String requestId;
    
    public long timeStamp;
    
    public ComponentType componentType;
    
    public String componentName;
    
    public String appName;
    
    public String methodName;
    
    public String moduleName;
    
    public String threadId;
    
    public String transactionId;
    
    public String securityId;

    String getRequestId() {
        return requestId;
    }

    void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    long getTimeStamp() {
        return timeStamp;
    }

    void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    ComponentType getComponentType() {
        return componentType;
    }

    void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    String getComponentName() {
        return componentName;
    }

    void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    String getAppName() {
        return appName;
    }

    void setAppName(String appName) {
        this.appName = appName;
    }

    String getMethodName() {
        return methodName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    String getModuleName() {
        return moduleName;
    }

    void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    String getThreadId() {
        return threadId;
    }

    void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    String getTransactionId() {
        return transactionId;
    }

    void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    String getSecurityId() {
        return securityId;
    }

    void setSecurityId(String securityId) {
        this.securityId = securityId;
    }
}
