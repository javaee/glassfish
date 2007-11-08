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

import java.io.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.queryframework.ObjectBuildingQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.sessions.*;


/**
 * <p><b>Purpose</b>: Allows customization of how an object is cloned.
 * An implementer of CopyPolicy can be set on a descriptor to provide
 * special cloning routine for how an object is cloned in a unit of work.
 * By default the InstantiationCopyPolicy is used which creates a new instance of
 * the class to be copied into.
 * The MethodBasedCopyPolicy can also be used that uses a clone method in the object
 * to clone the object.  When a clone method is used it avoid the requirement of having to
 * copy over each of the direct attributes.
 */
public interface CopyPolicy extends Cloneable, Serializable {

    /**
     * Return a shallow clone of the object for usage with object copying, or unit of work backup cloning.
     */
    Object buildClone(Object object, Session session) throws DescriptorException;

    /**
     * Return a shallow clone of the object for usage with the unit of work working copy.
     */
    Object buildWorkingCopyClone(Object object, Session session) throws DescriptorException;

    /**
     * Return an instance with the primary key set from the row, used for building a working copy during a unit of work transactional read.
     */
    Object buildWorkingCopyCloneFromRow(Record row, ObjectBuildingQuery query) throws DescriptorException;

    /**
     * Return an instance with the primary key set from the row, used for building a working copy during a unit of work transactional read.
     */
    Object buildWorkingCopyCloneFromRow(Record row, ObjectLevelReadQuery query) throws DescriptorException;

   /**
     * Clone the CopyPolicy.
     */
    Object clone();

    /**
     * Allow for any initialization or validation required.
     */
    void initialize(Session session) throws DescriptorException;

    /**
     * Set the descriptor.
     */
    void setDescriptor(ClassDescriptor descriptor);

    /**
     * Return if this copy policy creates a new instance, vs a clone.
     */
    boolean buildsNewInstance();
}
