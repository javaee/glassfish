/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/cmd/RunTestsCmdSource.java,v 1.1 2003/12/09 01:53:11 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/12/09 01:53:11 $
 */
package com.sun.cli.jmxcmd.test.cmd;

import com.sun.cli.jcmd.framework.Cmd;
import com.sun.cli.jcmd.framework.CmdSource;
import java.util.List;
import org.glassfish.admin.amx.util.ListUtil;

/**
Run the JUnit tests.
 */
public class RunTestsCmdSource implements CmdSource
{
    public List<Class<? extends Cmd>> getClasses()
    {
        final List<Class<? extends Cmd>> list = ListUtil.newList();
        list.add( RunTestsCmd.class );
        return list;
    }
}






