/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.cmd;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.glassfish.admin.amx.base.Pathnames;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;

/**
Manages info for {@link NavCmd}.
<p>
Currently works only for Glassfish V3 AMX compliant MBeans, and relies
on the Pathnames MBean for support.  Functionality can be fully generalized
later via a plugin or other general approach.
 */
public final class NavInfo
{
    public static final String DEFAULT_DIR = "/";
    private volatile MBeanServerConnection mConn;
    private volatile Pathnames mPaths;
    String mCurrentDir;
    Deque<String> mDirStack = new ArrayDeque<String>();


    public NavInfo(final MBeanServerConnection conn)
    {
        this( conn, DEFAULT_DIR);
    }
    
    public NavInfo(final MBeanServerConnection conn, final String path)
    {
        mCurrentDir = DEFAULT_DIR;
        mConn = null;
        setMBeanServerConnection(conn);
        cd(path);
    }

    public MBeanServerConnection getMBeanServerConnection()
    {
        return mConn;
    }
    
    public void setMBeanServerConnection(final MBeanServerConnection conn)
    {
        if (conn == mConn || conn == null)
        {
            return;
        }

        mPaths = ProxyFactory.getInstance(conn).getDomainRootProxy().getPathnames();
        mConn = conn;
    }


    private Pathnames paths()
    {
        return mPaths;
    }


    public String getCurrentDir()
    {
        return mCurrentDir;
    }


    public void setCurrentDir(final String dir)
    {
        String normalized = dir;
        while ( normalized.endsWith("/") && normalized.length() > 1 )
        {
            normalized = normalized.substring(0, normalized.length() - 1 );
        }
        
        if (resolve(normalized) != null)
        {
            mCurrentDir = normalized;
        }
        else
        {
            throw new IllegalArgumentException("NavInfo.setCurrentDir(): Bad path: " + dir);
        }
    }
    
    private static final String PATH_SEP = "/";
    
    boolean isAbsolutePath(final String s)
    {
        return s.startsWith( PATH_SEP );
    }
    
    /** no parsing yet, just common case */
    boolean isUp(final String s)
    {
        return s.equals( ".." ) || s.equals("../");
    }
    
    /** no parsing yet, just common case */
    boolean goesUp(final String s)
    {
        return s.startsWith("../") || isUp(s);
    }
    
    boolean isRelative(final String s)
    {
        return s.startsWith( "./" ) || ! isAbsolutePath(s);
    }
    
    String parentPath()
    {
        return resolve().getKeyProperty("pp");
    }
    
    private void println( final Object o )
    {
        System.out.println( "" + o );
    }
        
    /** change to a new directory, absolute or relative */
    public void cd(final String path)
    {
        final String saveDir = getCurrentDir();
        
        String cur = path;
        boolean ok = false;
        try
        {
            if ( isAbsolutePath(path) )
            {
                setCurrentDir(path);
                //println( "Setting absolute path: " + path );
            }
            else
            {
                while ( true )
                {
                    if ( cur.startsWith("./") )
                    {
                        cur = cur.substring(2);
                        //println( "Stripped ./: " + cur );
                    }
                    else if ( cur.startsWith("..") )
                    {
                        cur = cur.substring(2);
                        if ( cur.length() == 0 )
                        {
                            setCurrentDir( parentPath() );
                        }
                        else if ( cur.startsWith(PATH_SEP) )
                        {
                            cur = cur.substring(1);
                            //println( "Going up: " + cur );
                            setCurrentDir( parentPath() );
                        }
                        else
                        {
                            throw new IllegalArgumentException("Bad path " + path + " at " + cur );
                        }
                    }
                    else
                    {
                        String newPath = getCurrentDir();
                        if ( ! newPath.endsWith(PATH_SEP) )
                        {
                            newPath = newPath + PATH_SEP;
                        }
                        newPath = newPath + cur;
                        setCurrentDir( newPath) ;
                        break;
                    }
                }
            }
            ok = true;
        }
        finally
        {
            if ( ! ok )
            {
                setCurrentDir( saveDir );
            }
        }
    }


    public String toString()
    {
        return mCurrentDir;
    }


    public void pushd(final String s)
    {
        mDirStack.push(s);
    }


    public String popd()
    {
        return mDirStack.pop();
    }


    public ObjectName resolve()
    {
        return resolve(mCurrentDir);
    }


    public ObjectName resolve(final String dir)
    {
        if (paths() == null)
        {
            throw new IllegalStateException("NavInfo.resolve(): null paths()");
        }

        return paths().resolvePath(dir);
    }
}






