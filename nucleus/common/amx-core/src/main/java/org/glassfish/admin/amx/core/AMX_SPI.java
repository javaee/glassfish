/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.admin.amx.core;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

import javax.management.ObjectName;

/**
    @deprecated MBean implementations can 'implements AMX_SPI', though it is only behavior
    via MBeanInfo and attributes that is actually required.
 */
@Taxonomy(stability = Stability.COMMITTED)
@Deprecated
public interface AMX_SPI {
    /** the unencoded name, which could differ from the value of the 'name' property in the ObjectName */
    @ManagedAttribute
    @Description("Name of this MBean, can differ from name in ObjectName")
    public String getName();
    
    /** Return the ObjectName of the parent.  Must not be null (except for DomainRoot) */
    @ManagedAttribute
    @Description("Parent of this MBean, non-null except for DomainRoot")
    public ObjectName getParent();
    
    /**
        If no children are possible (a leaf node), an AttributeNotFoundException should be thrown.
     */
    @ManagedAttribute
    @Description("Children of this MBean, in no particular order")
    public ObjectName[] getChildren();
}

