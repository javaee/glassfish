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
package org.glassfish.api.deployment;

import java.lang.instrument.ClassFileTransformer;

/**
 * Providers of class loaders for GlassFish applications can optionally implements this interface
 * to indicate their class loader is capable of byte code enhancement.
 *
 * @author Jerome Dochez
 */
public interface InstrumentableClassLoader {

    /**
     * Create and return a temporary loader with the same visibility
     * as this loader. The temporary loader may be used to load
     * resources or any other application classes for the purposes of
     * introspecting them for annotations. The persistence provider
     * should not maintain any references to the temporary loader,
     * or any objects loaded by it.
     *
     * @return A temporary classloader with the same classpath as this loader
     */
    public ClassLoader copy();    

    /**
     * Add a new ClassFileTransformer to this class loader. This transfomer should be called for
     * each class loading event.
     *
     * @param transformer new class file transformer to do byte code enhancement.
     */
    public void addTransformer(ClassFileTransformer transformer);

}
