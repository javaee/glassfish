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

/*
 * PropertyElements.java
 *
 * Created on March 13, 2002, 2:00 PM
 */

package com.sun.enterprise.tools.common.properties;

import com.sun.enterprise.tools.common.dd.connector.ResourceAdapter;
import com.sun.enterprise.tools.common.dd.connector.SunConnector;
import java.io.FileInputStream;
/**
 *
 * @author  vkraemer
 * @version 
 */
public class PropertyElements {

    private ResourceAdapter ra;

    /** Creates new PropertyElements */
    public PropertyElements(ResourceAdapter ra) {
        this.ra = ra;
    }
    
    public PropertyElements(PropertyElements source) {
        this.ra = (ResourceAdapter) source.ra.clone();
    }
    
    public ResourceAdapter getResourceAdapter() {
        return ra;
    }
    
    public Object getAttributeDetail(int row, int col) { 
        return ra.getAttributeValue(ResourceAdapter.PROPERTY, row, intToAttribute(col)); //NOI18N
    }
    
    public void setAttributeDetail(Object v, int r, int c) { 
        String input = (String) v;
        if (null == input || 0 == input.trim().length()) {
            // test for need to delete
            if (r >= getLength())
                return;
            int otherDex = c - 1;
            if (otherDex < 0)
                otherDex = -otherDex;
            String otherVal = ra.getAttributeValue(ResourceAdapter.PROPERTY, r, 
                intToAttribute(otherDex));
            if (null == otherVal || 0 == otherVal.trim().length()) {
                ra.removePropertyElement(r);
                return;
            }
            input = " "; // NOI18N
        }
        while (r >= getLength())
            ra.addPropertyElement(true);
        ra.setAttributeValue(ResourceAdapter.PROPERTY, r, intToAttribute(c), input); //NOI18N
    }

    public int getLength() {
        return ra.sizePropertyElement();
    }
    
    public int getWidth() {
        return 2;
    }
    
    private String intToAttribute(int col) {
        if (col == 0) 
            return "name"; // NOI18N
        if (col == 1)
            return "value"; // NOI18N
        return "error"; // NOI18N
    }

    // this is used for debugging the PropertyElementsTableModel DO NOT EXPOSE
    //
    PropertyElements(String args[]) {
        int rowCount = 0;
        if (null != args && args.length > 0) {
            try {
                rowCount = Integer.parseInt(args[0]);
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
        ra = new ResourceAdapter();
        for (int i = 0; i < rowCount; i++) {
            ra.addPropertyElement(true);
            ra.setAttributeValue(ResourceAdapter.PROPERTY,i,"name",""+i); //NOI18N
            ra.setAttributeValue(ResourceAdapter.PROPERTY,i,"value",""+(rowCount - i)); //NOI18N
        }
        //
        // Note: the ResourceAdapter MUST be part of a SunConnector or the
        // PropertyElementsTableModel is BROKEN.
        SunConnector connectorDD = SunConnector.createGraph();
        connectorDD.setResourceAdapter(ra);
    }
    
    //ResourceAdapter getRA() {
        //return ra;
    //}
    
    String dumpIt() {
        return ra.dumpBeanNode();
    }

    public String toString() {
        return ra.dumpBeanNode();
    }
}
