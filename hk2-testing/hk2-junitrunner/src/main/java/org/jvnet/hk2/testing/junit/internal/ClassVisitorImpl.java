/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.jvnet.hk2.testing.junit.internal;

import java.util.List;


import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.external.org.objectweb.asm.AnnotationVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.ClassVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.Opcodes;

/**
 * @author jwells
 *
 */
public class ClassVisitorImpl extends ClassVisitor {
    private final static String SERVICE_CLASS_FORM = "Lorg/jvnet/hk2/annotations/Service;";
    
    private final ServiceLocator locator;
    private final boolean verbose;
    
    private String implName;
    private boolean isAService = false;
    
    /**
     * Creates this with the config to add to if this is a service
     * @param config
     * @param verbose true if we should print out any service we are binding
     */
    public ClassVisitorImpl(ServiceLocator locator, boolean verbose) {
        super(Opcodes.ASM5);
        
        this.locator = locator;
        this.verbose = verbose;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public void visit(int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces) {
        implName = name.replace("/", ".");
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!visible) return null;
        
        if (SERVICE_CLASS_FORM.equals(desc)) {
            isAService = true;
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        if (!isAService) return;
        
        Class<?> implClass = null;
        try {
            implClass = Class.forName(implName);
        }
        catch (Throwable th) {
            System.out.println("HK2Runner could not classload service " + implName + ", skipping...");
            return;
        }
        
        List<ActiveDescriptor<?>> added = ServiceLocatorUtilities.addClasses(locator, implClass);
        
        if (verbose && !added.isEmpty()) {
            System.out.println("HK2Runner bound service " + added.get(0));
        }
    }
}
