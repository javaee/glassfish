/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal;

import java.io.Serializable;

/**
 * A node with information about the parent, which is therefor
 * specific to a place in a tree
 * 
 * @author jwells
 *
 */
public class ParentedNode implements Serializable {
    private static final long serialVersionUID = 7004413497291650707L;
    
    private String childName;
    private boolean multiChildList;  // If true there is a list of children, otherwise there is just a single child
    private boolean multiChildArray;  // If true there is a list of children, otherwise there is just a single child
    private UnparentedNode child;
    
    public ParentedNode() {
    }
    
    public ParentedNode(String childName, boolean multiChildList, boolean multiChildArray, UnparentedNode child) {
        this.childName = childName;
        this.multiChildList = multiChildList;
        this.multiChildArray = multiChildArray;
        this.child = child;
    }
    
    public String getChildName() {
        return childName;
    }
    
    public UnparentedNode getChild() {
        return child;
    }
    
    public boolean isMultiChildList() {
        return multiChildList;
    }
    
    public boolean isMultiChildArray() {
        return multiChildArray;
    }
    
    /**
     * This is used when there are bean cycles.  If
     * the original child had not yet been processed
     * a placeholder was used, this method is used
     * to replace the placeholder with the real thing
     * 
     * @param child A non-placeholder child
     */
    public void setChild(UnparentedNode child) {
        this.child = child;
    }
    
    @Override
    public String toString() {
        return "ParentedNode(" + childName + "," + child + "," + System.identityHashCode(this) + ")";
    }

}
