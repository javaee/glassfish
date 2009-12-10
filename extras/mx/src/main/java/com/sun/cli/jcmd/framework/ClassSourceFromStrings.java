/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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



