/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/SampleCmdSource.java,v 1.3 2004/07/12 19:42:54 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/12 19:42:54 $
 */
package com.sun.cli.jcmd.framework;

import java.util.List;
import org.glassfish.admin.amx.util.ListUtil;

/**
An example of how to load new commands into jmxcmd.
 */
public class SampleCmdSource implements CmdSource
{

    public SampleCmdSource()
    {
    }
    private final static Class[] CLASSES =
    {
        SampleCmd.class,
    // list as many classes as desired
    };


    public List<Class<? extends Cmd>> getClasses()
    {
        final List<Class<? extends Cmd>> list = ListUtil.newList();
        list.add( SampleCmd.class );
        return list;
    }
}






