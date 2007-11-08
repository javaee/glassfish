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
 * JBIComponentDocument.java
 * 
 * @author ylee
 * @author Graj
 */

package com.sun.jbi.jsf.framework.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jbi.jsf.framework.model.JBIComponentStatus;


public class JBIComponentStatusDocument implements Serializable {

    public static final String COMP_INFO_LIST_NODE_NAME = "component-info-list";
    public static final String COMP_INFO_NODE_NAME = "component-info";
//    public static final String ID_NODE_NAME = "id";
    public static final String NAME_NODE_NAME = "name";
    public static final String TYPE_NODE_NAME = "type";
    public static final String STATUS_NODE_NAME = "state";
    public static final String DESCRIPTION_NODE_NAME = "description";
    public static final String VERSION_NODE_NAME = "version";
    public static final String NAMESPACE_NODE_NAME = "xmlns";

    protected List<JBIComponentStatus> jbiComponentStatusList = new ArrayList<JBIComponentStatus>();


    /**
     *
     */
    public JBIComponentStatusDocument() {
    }


    /**
     * @return Returns the jbiComponentList.
     */
    public List<JBIComponentStatus> getJbiComponentStatusList() {
        return this.jbiComponentStatusList;
    }
    
    /**
     * @param jbiComponentStatusList The jbiComponentList to set.
     */
    public void setJbiComponentStatusList(List<JBIComponentStatus> jbiComponentStatusList) {
        this.jbiComponentStatusList = jbiComponentStatusList;
    }

    public void addJbiComponentStatus(JBIComponentStatus componentStatus) {
        jbiComponentStatusList.add(componentStatus);
    } 
    
    public void addJbiComponentStatusList(List<JBIComponentStatus> list) {
        jbiComponentStatusList.addAll(list);
    }
    
    public void dump() {
        Iterator iterator = this.jbiComponentStatusList.iterator();
        JBIComponentStatus component = null;
        while((iterator != null) && (iterator.hasNext() == true)) {
            component = (JBIComponentStatus) iterator.next();
            component.dump();
        }
    }

}
