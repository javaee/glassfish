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
package com.sun.enterprise.diagnostics.report.html;

import com.sun.enterprise.diagnostics.Data;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;     
/**
 *
 * @author mu125243
 */
public class DataTraverser {
    
    private Data dataObj;
    
    /**
     *  Key of the map is one of the DataTypes. Assumption is there is one 
     * to one relationship between key and its value. 
     */
    private Map typeToData;
    
    /** Creates a new instance of DataTraverser */
    public DataTraverser(Data dataObj) {
        this.dataObj = dataObj;
    }
    
    /**
     * Returns the data object which this traverser is visiting
     * @return Data
     */
    public Data getSource() {
        return dataObj;
    }
    /**
     * Return child data object of type supplied as a parameter
     * @param type data object type
     * @return data object 
     */
    public Iterator<Data> getData(String type) {
        if(typeToData == null)
            traverse();
        
        Object obj = typeToData.get(type);
        if(obj != null) {
            ArrayList list = (ArrayList)obj;
            return list.iterator();
        }
        return new ArrayList().iterator();
    }
    
    private void traverse() {
        if(typeToData == null)
            typeToData = new HashMap(5);
        Iterator<Data> children = dataObj.getChildren();
        while(children.hasNext()) {
            Data child = children.next();
            ArrayList list = (ArrayList)typeToData.get(child.getType());
            if (list == null) {
                list = new ArrayList(5);
                typeToData.put(child.getType(), list);
            }
            list.add(child);
        }
    }
}
