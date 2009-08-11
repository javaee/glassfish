/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.cmd;

import javax.management.ObjectName;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionDependency;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.IllegalUsageException;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import static com.sun.cli.jmxcmd.cmd.NavInfo.*;

/**
	Manages the default targets.
 */
public class NavCmd extends JMXCmd
{
		public
	NavCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class TargetCmdHelp extends CmdHelpImpl
	{
		public	TargetCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS		= "file system analogy for MBeans";
		private final static String	TARGET_TEXT		= "file system analogy for MBeans.";

		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TARGET_TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new TargetCmdHelp() );
	}
	
	private final static String	CD_NAME		= "cd";
	private final static String	PWD_NAME	= "pwd";
	private final static String PUSHD_NAME	= "pushd";
	private final static String POPD_NAME	= "popd";
	private final static String LS_NAME	    = "ls";
		
	protected static final OperandsInfo	CD_OPERAND_INFO	= new OperandsInfoImpl( "<path>", 0 );
		
	private final static CmdInfo	CD_INFO	    = new CmdInfoImpl( CD_NAME, CD_OPERAND_INFO );
	private final static CmdInfo	LS_INFO     = new CmdInfoImpl( LS_NAME );
	private final static CmdInfo	PWD_INFO    = new CmdInfoImpl( PWD_NAME );
	private final static CmdInfo	PUSHD_INFO  = new CmdInfoImpl( PUSHD_NAME );
	private final static CmdInfo	POPD_INFO   = new CmdInfoImpl( POPD_NAME );
		
	
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( CD_INFO, LS_INFO, PWD_INFO, PUSHD_INFO, POPD_INFO) );
	}
	    
    public static final String NAV_INFO_KEY = "NavCmd.NavInfo";
	
    private void cd(final String dest )
    {
        final NavInfo info = getNavInfo();
        info.setCurrentDir(dest);
    }
    
    private NavInfo getNavInfo()
    {
        final Object o = envGet(NAV_INFO_KEY);
        NavInfo info = null;
        if ( o instanceof String )
        {
            info = new NavInfo();
            info.setCurrentDir((String)o);
        }
        else if ( o != null )
        {
            info = (NavInfo)o;
        }
        else
        {
            info = new NavInfo();
        } 
        
        envPut(NAV_INFO_KEY, info, false);
        return info;
    }
    
    /**
        Commands are stubbed out, 
     */
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		final String	cmd	= getSubCmdNameAsInvoked();
		
		assert( operands != null );
        
        final NavInfo navInfo = getNavInfo();
		
		if ( cmd.equals( CD_NAME ) )
		{
            if ( operands.length >= 2 )
            {
				throw new IllegalUsageException( cmd + " requires 0 or 1 arguments" );
            }
            final String dir = operands.length == 0 ? DEFAULT_DIR : operands[0];
            cd(dir);
            println(dir);
		}
        else if ( cmd.equals( LS_NAME ) )
        {
            println( navInfo.getCurrentDir() );
            establishProxy();
            // list all attribute names and children
            println( "Not yet implemented: " + cmd );
        }
        else if ( cmd.equals( PWD_NAME ) )
        {
            println( navInfo.getCurrentDir() );
        }
        else if ( cmd.equals( PUSHD_NAME ) )
        {
            navInfo.pushd( navInfo.getCurrentDir() );
            println( navInfo.getCurrentDir() );
        }
        else if ( cmd.equals( POPD_NAME ) )
        {
            try
            {
                final String dir = navInfo.popd();
                navInfo.setCurrentDir(dir);
            }
            catch( final Exception e)
            {
                println( "Directory stack empty" );
            }
            println( navInfo.getCurrentDir() );
        }
		else
		{
            println( "Unknown command: " + cmd );
		}
	}
}



