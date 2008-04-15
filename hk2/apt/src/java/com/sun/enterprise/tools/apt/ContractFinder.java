/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.tools.apt;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.ClassType;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;

import java.util.HashSet;
import java.util.Set;

/**
 * Given a {@link TypeDeclaration},
 * find all super-types that have {@link Contract},
 * including ones pointed by {@link ContractProvided}.
 *
 * @author Kohsuke Kawaguchi
 */
public class ContractFinder {
    /**
     * The entry point.
     */
    public static Set<TypeDeclaration> find(TypeDeclaration d) {
        return new ContractFinder().check(d).result;
    }


    /**
     * {@link InterfaceType}s whose contracts are already checked.
     */
    private final Set<InterfaceDeclaration> checkedInterfaces = new HashSet<InterfaceDeclaration>();

    private final Set<TypeDeclaration> result = new HashSet<TypeDeclaration>();

    private ContractFinder() {
    }

    private ContractFinder check(TypeDeclaration d) {
        // look for @ContractProvided interface
        checkContractProvided(d);

        // traverse up the inheritance tree and find all supertypes that have @Contract
        while(true) {
            checkSuperInterfaces(d);
            if (d instanceof ClassDeclaration) {
                ClassDeclaration cd = (ClassDeclaration) d;
                checkContract(cd);
                ClassType sc = cd.getSuperclass();
                if(sc==null)    break;
                d = sc.getDeclaration();
            } else
                break;
        }

        return this;
    }

    private void checkContractProvided(TypeDeclaration impl) {
        ContractProvided provided = impl.getAnnotation(ContractProvided.class);
        if (provided != null) {
            try {
                provided.value();
            } catch (MirroredTypeException e) {
                result.add(((DeclaredType)e.getTypeMirror()).getDeclaration());
            }
        }
    }

    private void checkContract(TypeDeclaration type) {
        if (type.getAnnotation(Contract.class) != null)
            result.add(type);
    }

    private void checkSuperInterfaces(TypeDeclaration d) {
        for (InterfaceType intf : d.getSuperinterfaces()) {
            InterfaceDeclaration i = intf.getDeclaration();
            if(checkedInterfaces.add(i)) {
                checkContract(i);
                checkSuperInterfaces(i);
            }
        }
    }
}
