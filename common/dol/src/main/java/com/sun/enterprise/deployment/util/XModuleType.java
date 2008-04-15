/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import javax.enterprise.deploy.shared.ModuleType;

/**
 * Extended module types which are specific to SJSAS
 *
 * @author Sreenivas Munnangi
 */

public class XModuleType extends ModuleType {

    private static final int offset = 100;
    public static final XModuleType LCM = new XModuleType(100);
    public static final XModuleType CMB = new XModuleType(101);

    private static final String[] stringTable = {
        "lcm",
        "cmb"
    };

    private static final XModuleType[] enumValueTable = {
        LCM,
        CMB
    };

    private static final String[] moduleExtension = {
        ".jar",
        ".jar"
    };

    protected XModuleType(int value) {
        super(value);
    }

    protected String[] getStringTable() {
        return this.stringTable;
    }

    protected ModuleType[] getEnumValueTable() {
        return this.enumValueTable;
    }

    public String getModuleExtension() {
        if (super.getValue() >= this.getOffset()) {
            return (this.moduleExtension[super.getValue() - this.getOffset()]);
        } else {
            return super.getModuleExtension();
        }
    }

    public static ModuleType getModuleType(int value) {
        if (value >= offset) {
            return enumValueTable[value-offset];
        } else {
            return ModuleType.getModuleType(value);
        }
    }

    public String toString() {
        String[] strTable = this.getStringTable();
        int index = super.getValue() - this.getOffset();
        if (strTable != null && index >= 0 && index < strTable.length)
            return strTable[index];
        else
            super.toString();
        return null;
    }

    protected int getOffset() {
        return offset; 
    }

}
