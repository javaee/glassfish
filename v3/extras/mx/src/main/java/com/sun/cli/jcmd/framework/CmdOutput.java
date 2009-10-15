/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdOutput.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

import org.glassfish.admin.amx.util.DebugState;
import org.glassfish.admin.amx.util.Output;

/**
	The API that should be used to output from a Cmd running within the framework.
 */
public interface CmdOutput extends Output, DebugState
{
};


