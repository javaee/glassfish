/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.descriptors.copying;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ObjectBuildingQuery;

/**
 * <p><b>Purpose</b>: Allows customization of how an object is cloned.
 * This class defines common behavoir that allows a subclass to be used
 * and set on a descriptor to provide a special cloning routine for how an object
 * is cloned in a unit of work.
 */
public abstract class AbstractCopyPolicy implements CopyPolicy {
    protected ClassDescriptor descriptor;

    public AbstractCopyPolicy() {
        super();
    }

    public abstract Object buildClone(Object domainObject, Session session) throws DescriptorException;

    /**
     * By default use the buildClone.
     */
    public Object buildWorkingCopyClone(Object domainObject, Session session) throws DescriptorException {
        return buildClone(domainObject, session);
    }

    /**
     * Create a new instance, unless a workingCopyClone method is specified, then build a new instance and clone it.
     */
    public Object buildWorkingCopyCloneFromRow(Record row, ObjectLevelReadQuery query) throws DescriptorException {
        return this.buildWorkingCopyCloneFromRow(row, (ObjectBuildingQuery)query);
    }

    /**
     * By default create a new instance.
     */
    public Object buildWorkingCopyCloneFromRow(Record row, ObjectBuildingQuery query) throws DescriptorException {
        return getDescriptor().getObjectBuilder().buildNewInstance();
    }

    /**
     * INTERNAL:
     * Clones the CopyPolicy
     */
    public Object clone() {
        try {
            // clones itself
            return super.clone();
        } catch (Exception exception) {
        }
        return null;
    }

    /**
     * Return the descriptor.
     */
    protected ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Do nothing by default.
     */
    public void initialize(Session session) throws DescriptorException {
        // Do nothing by default.
    }

    /**
     * Set the descriptor.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Return if a new instance is created or a clone.
     */
    public abstract boolean buildsNewInstance();
}
