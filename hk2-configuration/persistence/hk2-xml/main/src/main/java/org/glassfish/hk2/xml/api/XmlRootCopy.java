/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.api;

/**
 * This represents a copy of the parent
 * {@link XmlRootHandle}.  This tree can
 * be traversed and modified without those
 * modifications being reflected in the parent.
 * When the {@link #merge()} method is called
 * the parent tree will get all the changes
 * made to this tree in one commit.  The XmlRootCopy
 * allows for multiple changes to be made to the
 * root and its children in one atomic unit (either
 * all changes are made to the parent or none of them)
 * <p>
 * If the parent was changed after this copy
 * was created then the merge will fail.  The
 * method {@link #isMergeable()} can be used
 * to determine if this copy can still be merged
 * back into the parent
 * 
 * @author jwells
 *
 */
public interface XmlRootCopy<T> {
    /**
     * Gets the XmlRootHandle from which this copy was created
     * @return
     */
    public XmlRootHandle<T> getParent();
    
    /**
     * Gets the root of the JavaBean tree
     * 
     * @return The root of the JavaBean tree.  Will
     * only return null if the tree has not yet
     * been created
     */
    public T getChildRoot();
    
    /**
     * Returns true if this child copy can still
     * have merge called on it succesfully
     * 
     * @return true if it is still possible to
     * call merge (i.e., there has not been
     * a change to the parent tree since this
     * copy was made)
     */
    public boolean isMergeable();
    
    /**
     * Merges the changes made to this tree into
     * the parent tree
     */
    public void merge();

}
