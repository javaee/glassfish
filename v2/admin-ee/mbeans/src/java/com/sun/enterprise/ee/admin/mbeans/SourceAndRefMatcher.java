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
 * $Header: /cvs/glassfish/admin-ee/mbeans/src/java/com/sun/enterprise/ee/admin/mbeans/SourceAndRefMatcher.java,v 1.1.1.1 2006/08/08 19:48:40 dpatil Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2006/08/08 19:48:40 $
 */

package com.sun.enterprise.ee.admin.mbeans;

import java.util.ArrayList;
import javax.management.ObjectName;

public abstract class SourceAndRefMatcher
{
    private final String[] srcKeys;
    private final String[] refKeys;

    protected SourceAndRefMatcher(String[] srcKeys, String[] refKeys)
    {
        this.srcKeys = srcKeys;
        this.refKeys = refKeys;
    }

    public ObjectName[] matchingRefs(ObjectName[] srcs, ObjectName[] refs)
    {
        checkArg(srcs);
        checkArg(refs);

        final ArrayList al = new ArrayList();
        for (int i = 0; i < refs.length; i++)
        {
            final String ref = getKeyProperty(refs[i], refKeys);
            assert ref != null;
            for (int j = 0; j < srcs.length; j++)
            {
                final String name = getKeyProperty(srcs[j], srcKeys);
                assert name != null;
                if (name.equals(ref))
                {
                    al.add(refs[i]);
                }
            }
        }
        return (ObjectName[])al.toArray(new ObjectName[0]);
    }

    public ObjectName[] matchingSrcs(ObjectName[] srcs, ObjectName[] refs)
    {
        checkArg(srcs);
        checkArg(refs);

        final ArrayList al = new ArrayList();
        for (int i = 0; i < refs.length; i++)
        {
            final String ref = getKeyProperty(refs[i], refKeys);
            assert ref != null;
            for (int j = 0; j < srcs.length; j++)
            {
                final String name = getKeyProperty(srcs[j], srcKeys);
                assert name != null;
                if (name.equals(ref))
                {
                    al.add(srcs[j]);
                }
            }
        }
        return (ObjectName[])al.toArray(new ObjectName[0]);
    }

    private static String getKeyProperty(ObjectName on, String[] aliases)
    {
        for (int i = 0; i < aliases.length; i++)
        {
            final String name = on.getKeyProperty(aliases[i]);
            if (name != null)
            {
                return name;
            }
        }
        return null;
    }

    private static void checkArg(Object o)
    {
        if (o == null)
        {
            throw new IllegalArgumentException();
        }
    }
}
