/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.component;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.glassfish.hk2.*;
import org.glassfish.hk2.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 5/31/11
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractResolvedBinder<T> implements ResolvedBinder<T> {
    final BinderImpl<T> metadata;
    Scope scope;

    protected AbstractResolvedBinder(BinderImpl<T> metadata) {
        this.metadata = metadata;
    }

    @Override
    public void in(Scope scope) {
        this.scope = scope;
    }

    protected void registerIn(Habitat habitat, Inhabitant<T> inhabitant) {
        List<String> contracts = new ArrayList<String>();
        contracts.addAll(metadata.contracts);
        if (contracts.isEmpty() && (metadata.name==null || metadata.name.isEmpty())) {
            // there is no name, or contract, register by type
            habitat.add(inhabitant);
        } else {
            if (contracts.isEmpty()) {
                // if no contract is present, but there is a name, the type itself is the contract.
                contracts.add(inhabitant.type().getName());
            }
            for (String contract : contracts) {
                habitat.addIndex(inhabitant, contract, metadata.name);
            }
            // finally, register by type
            habitat.add(inhabitant);
        }
    }

    abstract void registerIn(Habitat habitat);
}
