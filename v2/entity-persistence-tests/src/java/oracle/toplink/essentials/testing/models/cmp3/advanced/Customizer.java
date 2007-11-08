/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  


// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.util.HashMap;
import java.util.Map;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.tools.sessionconfiguration.DescriptorCustomizer;
import oracle.toplink.essentials.tools.sessionconfiguration.SessionCustomizer;

/**
 * Session and descriptor customizer.
 */
public class Customizer implements SessionCustomizer, DescriptorCustomizer {
    static HashMap sessionCalls = new HashMap();
    static HashMap descriptorCalls = new HashMap();

    public void customize(Session session) {
        String sessionName = session.getName();
        Integer numberOfCalls = (Integer)sessionCalls.get(sessionName);
        int num = 0;
        if(numberOfCalls != null) {
            num = numberOfCalls.intValue();
        }
        sessionCalls.put(sessionName, new Integer(num + 1));
    }
    
    public void customize(ClassDescriptor descriptor) {
        String javaClassName = descriptor.getJavaClass().getName();
        Integer numberOfCalls = (Integer)descriptorCalls.get(javaClassName);
        int num = 0;
        if(numberOfCalls != null) {
            num = numberOfCalls.intValue();
        }
        descriptorCalls.put(javaClassName, new Integer(num + 1));
    }
    
    public static Map getSessionCalls() {
        return sessionCalls;
    }

    public static Map getDescriptorCalls() {
        return descriptorCalls;
    }
    
    public static int getNumberOfCallsForSession(String sessionName) {
        Integer numberOfCalls = (Integer)sessionCalls.get(sessionName);
        if(numberOfCalls == null) {
            return 0;
        } else {
            return numberOfCalls.intValue();
        }
    }

    public static int getNumberOfCallsForClass(String javaClassName) {
        Integer numberOfCalls = (Integer)descriptorCalls.get(javaClassName);
        if(numberOfCalls == null) {
            return 0;
        } else {
            return numberOfCalls.intValue();
        }
    }
}
