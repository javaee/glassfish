/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.event.v3;

import com.sun.enterprise.admin.event.ElementChangeEvent;
import java.util.List;

/**
 * Needs to be instanciated and registered as the ElementChangeHelper when no admin
 * is present. implementation needs some work...
 *
 * @author Jerome Dochez
 */
public class ElementChangeHelperImpl implements ElementChangeHelper {
    
    /** Creates a new instance of ElementChangeHelperImpl */
    public ElementChangeHelperImpl() {
    }

    public int getActionCodeForChanges(List changeList) 
    {
        return ElementChangeEvent.ACTION_ELEMENT_UNDEFINED;
    }    

    public String getElementXPath(List changes) {
        return null;
    }

    public String getConfigElementPrimaryKey(String xpath) {
        return null;
    }

    public String getConfigElementTargetName(String xpath, com.sun.enterprise.config.ConfigContext ctx) {
        return null;
    }
    
}
