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
package com.sun.enterprise.deployment.annotation.introspection;

import com.sun.enterprise.deployment.annotation.factory.SJSASFactory;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import java.util.Set;


/**
 * This class contains the list of all annotations types name 
 * which can be present at the class level (Type.TYPE).  
 *
 * @author Jerome Dochez
 */
@Service(name="default")
@Scoped(Singleton.class)
public class DefaultAnnotationScanner implements AnnotationScanner, 
    PostConstruct {

    @Inject 
    SJSASFactory factory;
    
    private Set<String> annotations=null;
    
    /**
     * Test if the passed constant pool string is a reference to 
     * a Type.TYPE annotation of a J2EE component
     *
     * @String the constant pool info string 
     * @return true if it is a J2EE annotation reference
     */
    public boolean isAnnotation(String value) {
        return annotations.contains(value);
    }
    
    public void postConstruct() {
        annotations = factory.getAnnotations();
    }
}
