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

import java.util.Map;
import java.util.HashMap;

/**
 * Extended module types which are specific to SJSAS
 *
 * @author Sreenivas Munnangi
 */

public class XModuleType {

    private final static Map<Integer, XModuleType> ModuleTypes=new HashMap<Integer, XModuleType>();
    
	/**
	 * The module is an EAR archive.
	 */
	public static final XModuleType EAR = create(0);
	/**
	 * The module is an Enterprise Java Bean archive.
	 */
	public static final XModuleType EJB = create(1);
	/**
	 * The module is an Client Application archive.
	 */
	public static final XModuleType CAR = create(2);
	/**
	 * The module is an Connector archive.
	 */
	public static final XModuleType RAR = create(3);
	/**
	 * The module is an Web Application archive.
	 */
	public static final XModuleType WAR = create(4);

    /**
     * The module is EJB archive packaged in a War file.
     */
    public static final XModuleType EjbInWar = create(5);

    public static final XModuleType WebServices = create(6);

    public static final XModuleType Persistence = create(7);

	private static final String[] stringTable = {
	"ear",
	"ejb",
	"car",
	"rar",
	"war",
	};    

    final Integer index;
    protected XModuleType(int value) {
        this.index = value;
    }

    public static XModuleType create(int index) {
        synchronized(ModuleTypes) {
            if (!ModuleTypes.containsKey(index)) {
                ModuleTypes.put(index, new XModuleType(index));
            }
        }
        return ModuleTypes.get(index);
    }     

    /**
     * {@inheritDoc}
     * <p/>
     * Considers only {@link #index} for equality.
     */
    @Override
    public boolean equals(final Object o) {
        return this==o || (o!=null && getClass()==o.getClass() && index.equals((((XModuleType) o).index)));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns {@link #index} as the hash code.
     */
    @Override
    public int hashCode() {
        return index.hashCode();
    }

    @Override
    public String toString() {
    	int index = this.index;
		if (stringTable != null && index >= 0 && index < stringTable.length)
        	return stringTable[index];
		else
        	return Integer.toString (this.index);
    }
}
