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
package org.glassfish.admin.amx.impl.j2ee.loader;

import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.internal.data.ApplicationRegistry;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;

/**
   Supplies the needed values for other classes such as MBeans that do not have access to
   the injection facilities.
 */
@Service
public final class J2EEInjectedValues extends InjectedValues
{
    @Inject
    private ApplicationRegistry mAppsRegistry;
    public ApplicationRegistry getApplicationRegistry() { return mAppsRegistry; }
    
    @Inject
    ArchivistFactory mArchivistFactory;
    public ArchivistFactory getArchivistFactory() { return mArchivistFactory; }

    public static synchronized J2EEInjectedValues getInstance()
    {
        return getDefaultHabitat().getByType(J2EEInjectedValues.class);
    }
    
    public J2EEInjectedValues()
    {
    }
}















