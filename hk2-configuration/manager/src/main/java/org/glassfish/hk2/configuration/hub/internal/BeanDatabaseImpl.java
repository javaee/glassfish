/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.hub.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.utilities.reflection.BeanReflectionHelper;

/**
 * @author jwells
 *
 */
public class BeanDatabaseImpl implements BeanDatabase {
    private final long revision;
    private final HashMap<String, TypeImpl> types = new HashMap<String, TypeImpl>();
    
    /**
     * Creates a new, fresh database
     */
    /* package */ BeanDatabaseImpl(long revision) {
        this.revision = revision;
    }
    
    /* package */ BeanDatabaseImpl(long revision, BeanDatabase beanDatabase) {
        this(revision);
        
        for (Type type : beanDatabase.getAllTypes()) {
            types.put(type.getName(), new TypeImpl(type, ((WriteableTypeImpl) type).getHelper()));
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getAllTypes()
     */
    @Override
    public synchronized Set<Type> getAllTypes() {
        return Collections.unmodifiableSet(new HashSet<Type>(types.values()));
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getInstance(java.lang.String, java.lang.Object)
     */
    @Override
    public synchronized Instance getInstance(String type, String instanceKey) {
        Type t = getType(type);
        if (t == null) return null;
        
        return t.getInstance(instanceKey);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getType(java.lang.String)
     */
    @Override
    public synchronized Type getType(String type) {
        return types.get(type);
    }
    
    /* package */ long getRevision() {
        return revision;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#dumpDatabase()
     */
    @Override
    public void dumpDatabase() {
        dumpDatabase(System.err);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#dumpDatabase(java.io.PrintStream)
     */
    @Override
    public void dumpDatabase(PrintStream output) {
        Utilities.dumpDatabase(this, output);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#dumpDatabaseAsString()
     */
    @Override
    public String dumpDatabaseAsString() {
        return Utilities.dumpDatabaseAsString(this);
    }
    
    @Override
    public String toString() {
        return "BeanDatabaseImpl(" + revision + "," + System.identityHashCode(this) + ")";
    }
}
