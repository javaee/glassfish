/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.ha.store.apt.generators;

import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Mahesh Kannan
 */
public class AbstractGenerator {

    private int indent;

    private String space = "";

    protected Map<String, TypeMirror> types =
            new HashMap<String, TypeMirror>();

    protected Set<String> attrNames = new HashSet<String>();

    protected void increaseIndent() {
        indent++;
        space += "\t";
    }

    protected void decreaseIndent() {
        indent--;
        space = space.substring(1);
    }

    protected void println(String msg) {
        System.out.println(space + msg);
    }

    protected void print(String msg) {
        System.out.print(space + msg);
    }

    protected void println() {
        System.out.println(space);
    }

    protected void addAttribute(String attrName, TypeMirror decl) {
        attrNames.add(attrName);
        types.put(attrName, decl);
    }

    protected static String getWrapperType(TypeMirror type) {
        String result = type.toString();
        if (type instanceof PrimitiveType) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        /*
        int index = result.lastIndexOf(' ');
        result = result.substring(0, index);

        if (type instanceof PrimitiveType) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }

        int ltIndex = result.indexOf('<');

        return (ltIndex == -1) ? result : result.substring(0, ltIndex);
        */
        return result;
    }

}
