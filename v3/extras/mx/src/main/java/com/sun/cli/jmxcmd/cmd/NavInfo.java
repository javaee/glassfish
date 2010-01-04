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






