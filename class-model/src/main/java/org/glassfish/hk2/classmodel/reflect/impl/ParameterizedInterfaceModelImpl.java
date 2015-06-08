/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.ExtensibleType;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;

import java.util.*;

/**
 * Implementation of the {@link ParameterizedInterfaceModel}
 *
 * @author Jerome Dochez
 */
class ParameterizedInterfaceModelImpl implements ParameterizedInterfaceModel {

    final TypeProxy<InterfaceModel> rawInterface;
    final List<ParameterizedInterfaceModel> parameterizedTypes = new ArrayList<ParameterizedInterfaceModel>();

    ParameterizedInterfaceModelImpl(TypeProxy<InterfaceModel> rawInterface) {
        this.rawInterface = rawInterface;
    }

    synchronized void addParameterizedType(ParameterizedInterfaceModel type) {
        parameterizedTypes.add(type);
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(rawInterface.getName());
        if (!parameterizedTypes.isEmpty()) {
            sb.append("<");
            Iterator<ParameterizedInterfaceModel> parameterizedTypeItr = parameterizedTypes.iterator();
            while(parameterizedTypeItr.hasNext()) {
                sb.append(parameterizedTypeItr.next().getName());
                if (parameterizedTypeItr.hasNext()) sb.append(",");
            }
            sb.append(">");
        }
        return sb.toString();
    }

    public TypeProxy<InterfaceModel> getRawInterfaceProxy() {
        return rawInterface;
    }

    @Override
    public InterfaceModel getRawInterface() {
        return rawInterface.get();
    }

    @Override
    public Collection<ParameterizedInterfaceModel> getParametizedTypes() {
        return Collections.unmodifiableCollection(parameterizedTypes);
    }
}
