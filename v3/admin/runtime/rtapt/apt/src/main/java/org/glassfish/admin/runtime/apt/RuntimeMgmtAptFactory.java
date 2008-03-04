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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.admin.runtime.apt;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import java.util.*;
import org.glassfish.admin.runtime.annotations.MBean;
import org.glassfish.admin.runtime.annotations.ManagedAttribute;
import org.glassfish.admin.runtime.annotations.ManagedOperation;


public class RuntimeMgmtAptFactory implements AnnotationProcessorFactory {

    public RuntimeMgmtAptFactory() {}

    public AnnotationProcessor getProcessorFor(
            Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment env
            ) {
        return new RuntimeMgmtAptProcessor(env);
    }

    public Collection<String> supportedAnnotationTypes() {
        Collection<String> rslt = new ArrayList<String>();
        rslt.add(MBean.class.getName());
        rslt.add(ManagedAttribute.class.getName());
        rslt.add(ManagedOperation.class.getName());
        return rslt;
    }

    public Collection<String> supportedOptions() {
        return new ArrayList<String>();
    }
}
