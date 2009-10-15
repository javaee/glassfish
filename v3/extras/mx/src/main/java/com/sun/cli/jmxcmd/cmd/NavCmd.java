/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.cmd;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;

import java.util.Map;
import java.util.HashMap;


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
import java.util.Set;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import static com.sun.cli.jmxcmd.cmd.NavInfo.*;

import org.glassfish.admin.amx.core.PathnameParser;



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
        private final static String SYNOPSIS = "navigate through the MBean hierarchy";
        private final static String TARGET_TEXT = "Target MBeans, and navigate like a file system";


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
    private final static String TARGET_NAME = "target";
    private final static String CLEAR_TARGET_NAME = "clear-target";
    private final static CmdInfo TARGET_INFO =
        new CmdInfoImpl(TARGET_NAME, TARGETS_OPERAND_INFO);
    private final static CmdInfo CLEAR_TARGET_INFO =
        new CmdInfoImpl(CLEAR_TARGET_NAME);
    private final static String CD_NAME = "cd";
    private final static String PWD_NAME = "pwd";
    private final static String CAT_NAME = "cat";
    private final static String PUSHD_NAME = "pushd";
    private final static String POPD_NAME = "popd";
    private final static String LS_NAME = "ls";
    private final static String PGET_NAME = "pget";
    
    protected static final OperandsInfo CD_OPERAND_INFO = new OperandsInfoImpl("<path>", 0);
    private final static CmdInfo CD_INFO = new CmdInfoImpl(CD_NAME, CD_OPERAND_INFO);
    private final static CmdInfo CAT_INFO = new CmdInfoImpl(CAT_NAME);
    private final static CmdInfo LS_INFO = new CmdInfoImpl(LS_NAME);
    private final static CmdInfo PWD_INFO = new CmdInfoImpl(PWD_NAME);
    private final static CmdInfo PUSHD_INFO = new CmdInfoImpl(PUSHD_NAME);
    private final static CmdInfo POPD_INFO = new CmdInfoImpl(POPD_NAME);
    
    protected static final OperandsInfo PGET_OPERAND_INFO = new OperandsInfoImpl("<path@attr>", 0);
    private final static CmdInfo PGET_INFO = new CmdInfoImpl(PGET_NAME, PGET_OPERAND_INFO);
    


    public static CmdInfos getCmdInfos()
    {
        return new CmdInfos(new CmdInfo[]
            {
                CD_INFO, LS_INFO, PWD_INFO, PUSHD_INFO, POPD_INFO, CAT_INFO, TARGET_INFO, CLEAR_TARGET_INFO, PGET_INFO
            });
    }
    public static final String NAV_INFO_KEY = "NavCmd.NavInfo";


    private ObjectName target()
    {
        return getNavInfo().resolve();
    }


    private AMXProxy targetProxy()
    {
        return ProxyFactory.getInstance(getConnection()).getProxy(target());
    }

    private void pget(final String[] argsIn)
    {
        final String ATTR_DELIM = "@";
        final String ALL = "*";
        
        final MBeanServerConnection conn = getConnection();
        final ProxyFactory proxyFactory = ProxyFactory.getInstance(conn);
        
        final String currentDir = getNavInfo().getCurrentDir();
        
        final String[] args = argsIn.length == 0 ?  new String[] { getNavInfo().getCurrentDir() } : argsIn;
        
        for( final String arg : args )
        {
            String expr = arg;
            println( "Processing arg: " + arg );
            
            // full path?  If not, prepend the current dir
            if ( ! expr.startsWith( "/" ) )
            {
                // not a full path, prepend current dir
                if ( expr.startsWith(ATTR_DELIM) )
                {
                    expr = currentDir + expr;
                }
                else
                {
                    if ( currentDir.endsWith("/") )
                    {
                        expr = currentDir + expr;
                    }
                    else
                    {
                        expr = currentDir + "/" + expr;
                    }
                }
            }
            println( "arg = " + arg + ", expr = " + expr);
            
            try
            {
                final int idx = expr.indexOf(ATTR_DELIM);
                final String path = idx < 0 ? expr : expr.substring(0,idx);
                String attr = idx < 0 ? ALL : expr.substring(idx+1);
                if ( attr.length() == 0 )
                {
                    attr = ALL;
                }
                
                final PathnameParser parser = new PathnameParser(path);
                println( "Parsed: " + parser.parts() );
                
                //println( "path = " + path + ", attrs = " + attr );
                final NavInfo navInfo = new NavInfo(conn, path);
                
                final ObjectName objectName = navInfo.resolve();
                final AMXProxy amx = proxyFactory.getProxy(objectName);
                
                final Set<String> attrNames = attr.equals(ALL) ? amx.attributeNames() : SetUtil.newUnmodifiableStringSet( attr.split(",") );
                final Map<String,Object> results = amx.attributesMap(attrNames);
                
                for( final String attrName : SetUtil.toSortedStringArray( results.keySet() ) )
                {
                    final Object value = results.get(attrName);
                    final String valueStr = SmartStringifier.toString(value);
                    println( path + ATTR_DELIM + attrName + "=" + valueStr);
                }
                
            }
            catch( final Exception e )
            {
                println( "" + e );
            }
            println( "" );
        }
    }
    

    private void cd(final String dest)
    {
        final NavInfo info = getNavInfo();
        info.cd(dest);
        setTargets( new String[] { info.resolve().toString() } );
        println(info.getCurrentDir() + " = " + info.resolve());
        
        envPut(NAV_INFO_KEY, info.toString(), true);
    }


    private void ls()
    {
        final AMXProxy amx = targetProxy();

        final String delim = ", ";

        final Set<AMXProxy> childrenSet = amx.childrenSet();
        //final Map<String,Object> attrs = amx.attributesMap();

        final String attrNamesStr = CollectionUtil.toString(amx.attributeNames(), delim);

        final List<String> childItems = new ArrayList<String>();
        for (final AMXProxy child : childrenSet)
        {
            final String nameProp = child.nameProp();
            childItems.add(child.type() + (nameProp == null ? "" : "[" + nameProp + "]"));
        }
        final String childrenStr = CollectionUtil.toString(childItems, delim);

        println(attrNamesStr);
        println("");
        if (childrenSet.size() != 0)
        {
            println(childrenStr);
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
        NavInfo info = null;
        final Object o = envGet(NAV_INFO_KEY);
        if (o instanceof String)
        {
            info = new NavInfo( getConnection() );
            info.setCurrentDir((String) o);
        }
        else if (o != null)
        {
            info = (NavInfo) o;
            info.setMBeanServerConnection( getConnection() );
            info.resolve();
        }
        else
        {
            info = new NavInfo( getConnection() );
            println( "getNavInfo: made a new  NavInfo" );
        }

        return info;
    }


    private void connect()
    {
        try
        {
            establishProxy();
            getNavInfo().setMBeanServerConnection(getConnection());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }


    void displayExistingTarget()
    {
        println("Targets:\n" + envGet(JMXCmdEnvKeys.TARGETS));
    }


    void warnEmptyTargets(final String[] targets)
        throws Exception
    {
        // issue warning if some targets could not be resolved to anything
        for (int i = 0; i < targets.length; ++i)
        {
            final String target = targets[i];

            final ObjectName[] objects =
                resolveTargets(getProxy(), new String[]
                {
                    target
                });

            if (objects.length == 0)
            {
                println("WARNING: target " + target + " does not resolve to any objects");
            }
        }
    }


    void setTargets(final String[] targets)
    {
        try
        {
            putEnvTargets(targets);
            warnEmptyTargets(targets);

            getAliasMgr().deleteAlias(JMXCmdEnvKeys.TARGETS_ALIAS);
            getAliasMgr().createAlias(JMXCmdEnvKeys.TARGETS_ALIAS, ArrayStringifier.stringify(targets, " "));
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

        // validate the current dir
        NavInfo navInfo;
        try
        {
            navInfo = getNavInfo();
        }
        catch( final Exception e )
        {
            navInfo = new NavInfo( getConnection() );
            println( "Exception getting NavInfo: " + e.getMessage() );
            println( "Working MBean set to " + navInfo.getCurrentDir() );
            envPut(NAV_INFO_KEY, navInfo.toString(), true);
        }

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
            println(navInfo.getCurrentDir() + " = " + target());
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
        else if (cmd.equals(CLEAR_TARGET_NAME))
        {
            if (operands.length != 0)
            {
                throw new IllegalUsageException(CLEAR_TARGET_NAME + " takes no operands");
            }

            envRemove(JMXCmdEnvKeys.TARGETS);
            getAliasMgr().deleteAlias(JMXCmdEnvKeys.TARGETS_ALIAS);
        }
        else if (cmd.equals(TARGET_NAME))
        {
            if (operands.length == 0)
            {
                displayExistingTarget();
            }
            else
            {
                connect();
                setTargets(operands);
            }
        }
        else if ( cmd.equals( PGET_NAME ) )
        {
            pget(operands);
        }
        else
        {
            println("Unknown command: " + cmd);
        }
    }
}



