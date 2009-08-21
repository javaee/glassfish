/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.cmd;

import javax.management.ObjectName;



import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.IllegalUsageException;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.CollectionUtil;
import static com.sun.cli.jmxcmd.cmd.NavInfo.*;

/**
Manages the default targets.
 */
public class NavCmd extends JMXCmd
{
    public NavCmd(final CmdEnv env)
    {
        super(env);
    }

    static final class NavCmdHelp extends CmdHelpImpl
    {
        public NavCmdHelp()
        {
            super(getCmdInfos());
        }
        private final static String SYNOPSIS = "file system analogy for MBeans";
        private final static String TARGET_TEXT = "file system analogy for MBeans.";


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getText()
        {
            return (TARGET_TEXT);
        }
    }


    public CmdHelp getHelp()
    {
        return (new NavCmdHelp());
    }
    private final static String CD_NAME = "cd";
    private final static String PWD_NAME = "pwd";
    private final static String CAT_NAME = "cat";
    private final static String PUSHD_NAME = "pushd";
    private final static String POPD_NAME = "popd";
    private final static String LS_NAME = "ls";
    protected static final OperandsInfo CD_OPERAND_INFO = new OperandsInfoImpl("<path>", 0);
    private final static CmdInfo CD_INFO = new CmdInfoImpl(CD_NAME, CD_OPERAND_INFO);
    private final static CmdInfo CAT_INFO = new CmdInfoImpl(CAT_NAME);
    private final static CmdInfo LS_INFO = new CmdInfoImpl(LS_NAME);
    private final static CmdInfo PWD_INFO = new CmdInfoImpl(PWD_NAME);
    private final static CmdInfo PUSHD_INFO = new CmdInfoImpl(PUSHD_NAME);
    private final static CmdInfo POPD_INFO = new CmdInfoImpl(POPD_NAME);


    public static CmdInfos getCmdInfos()
    {
        return new CmdInfos(new CmdInfo[]
            {
                CD_INFO, LS_INFO, PWD_INFO, PUSHD_INFO, POPD_INFO, CAT_INFO
            });
    }
    public static final String NAV_INFO_KEY = "NavCmd.NavInfo";


    private ObjectName target()
    {
        return getNavInfo().resolve();
    }
    
    
    private AMXProxy targetProxy()
    {
        return ProxyFactory.getInstance( getConnection() ).getProxy( target() );
    }


    private void cd(final String dest)
    {
        final NavInfo info = getNavInfo();
        info.cd(dest);
        println( info.getCurrentDir() + " = " + info.resolve());
    }


    private void ls()
    {
        final AMXProxy amx = targetProxy();
        
        final String delim = ", ";
        
        final Set<AMXProxy> childrenSet = amx.childrenSet();
        //final Map<String,Object> attrs = amx.attributesMap();
        
        final String attrNamesStr = CollectionUtil.toString( amx.attributeNames(), delim );
        
        final List<String> childItems = new ArrayList<String>();
        for( final AMXProxy child : childrenSet)
        {
            final String nameProp = child.nameProp();
            childItems.add( child.type() + (nameProp == null ? "" : "=" + nameProp) );
        }
        final String childrenStr = CollectionUtil.toString( childItems, delim );
        
        println( attrNamesStr );
        println( "" );
        if ( childrenSet.size() != 0 )
        {
            println( childrenStr );
        }
        
        /*
        try
        {
            final MBeanInfo info = getConnection().getMBeanInfo( target() );
            println( info );
        }
        catch( final Exception e )
        {
            e.printStackTrace();
        }
        */
    }


    private void cat()
    {
        final NavInfo info = getNavInfo();
        println("Not yet implemented");
    }


    private NavInfo getNavInfo()
    {
        final Object o = envGet(NAV_INFO_KEY);
        NavInfo info = null;
        if (o instanceof String)
        {
            info = new NavInfo(getConnection());
            info.setCurrentDir((String) o);
        }
        else if (o != null)
        {
            info = (NavInfo) o;
        }
        else
        {
            info = new NavInfo(getConnection());
        }

        envPut(NAV_INFO_KEY, info, false);
        return info;
    }


    private void connect()
    {
        try
        {
            establishProxy();
            getNavInfo().setMBeanServerConnection( getConnection() );
        }
        catch( final Exception e )
        {
            e.printStackTrace();
        }
    }
    

    /**
    Commands are stubbed out,
     */
    protected void executeInternal()
        throws Exception
    {
        final String[] operands = getOperands();
        final String cmd = getSubCmdNameAsInvoked();

        assert (operands != null);

        final NavInfo navInfo = getNavInfo();

        if (cmd.equals(CD_NAME))
        {
            if (operands.length >= 2)
            {
                throw new IllegalUsageException(cmd + " requires 0 or 1 arguments");
            }
            connect();
            final String dir = operands.length == 0 ? DEFAULT_DIR : operands[0];
            cd(dir);
        }
        else if (cmd.equals(LS_NAME))
        {
            connect();
            //println(navInfo.getCurrentDir());
            // list all attribute names and children
            ls();
        }
        else if (cmd.equals(PWD_NAME))
        {
            connect();
            println(navInfo.getCurrentDir() + " = " + target() );
        }
        else if (cmd.equals(PUSHD_NAME))
        {
            connect();
            navInfo.pushd(navInfo.getCurrentDir());
            println(navInfo.getCurrentDir());
        }
        else if (cmd.equals(CAT_NAME))
        {
            println("not implemented");
        }
        else if (cmd.equals(POPD_NAME))
        {
            connect();
            try
            {
                final String dir = navInfo.popd();
                navInfo.setCurrentDir(dir);
            }
            catch (final Exception e)
            {
                println("Directory stack empty");
            }
            println(navInfo.getCurrentDir());
        }
        else
        {
            println("Unknown command: " + cmd);
        }
    }
}



