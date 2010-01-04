/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/ClassSourceFromStrings.java,v 1.4 2005/11/08 22:39:16 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:16 $
 */
package com.sun.cli.jcmd.framework;

import java.util.List;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.TypeCast;

/**
An implementation of ClassSource which uses classnames. 
 */
public final class ClassSourceFromStrings<T> implements ClassSource<T>
{

    private final String[] mClassnames;
    private boolean mErrorIfNotFound;


    public ClassSourceFromStrings(
            final String[] classnames,
            final boolean errorIfNotFound)
            throws ClassNotFoundException
    {
        mClassnames = classnames;
        mErrorIfNotFound = errorIfNotFound;

        if (mErrorIfNotFound)
        {
            // produce an error right now if there's going to be one
            _getClasses();
        }
    }


    //@SuppressWarnings("unchecked")
    private List<Class<? extends T>> _getClasses()
    {
        List<Class<? extends T>> list = ListUtil.newList();

        if (mClassnames != null)
        {
            for (final String classname : mClassnames)
            {
                try
                {
                    final Class theClass = ClassUtil.getClassFromName(classname);

                    final Class<? extends T> c = TypeCast.asClass(theClass);
                    list.add(c);
                }
                catch (final ClassNotFoundException e)
                {
                    if (mErrorIfNotFound)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return (list);
    }


    public List<Class<? extends T>> getClasses()
    {
        return _getClasses();
    }
};



