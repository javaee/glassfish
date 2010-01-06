/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/JMXCmdCmdMgr.java,v 1.19 2005/05/20 00:45:16 llc Exp $
 * $Revision: 1.19 $
 * $Date: 2005/05/20 00:45:16 $
 */
package com.sun.cli.jmxcmd;

import java.util.Map;
import java.util.Properties;


import com.sun.cli.jcmd.framework.CmdFactoryIniter;
import com.sun.cli.jcmd.JCmdKeys;
import com.sun.cli.jcmd.framework.Cmd;
import com.sun.cli.jcmd.framework.CmdFactory;
import com.sun.cli.jcmd.framework.CmdMgrImpl;
import com.sun.cli.jcmd.framework.CmdSource;
import com.sun.cli.jcmd.framework.CmdEnvKeys;

// to get the Cmd classes we need to register
import com.sun.cli.jmxcmd.cmd.*;

import org.glassfish.admin.amx.util.stringifier.StringifierRegistryImpl;
import com.sun.cli.jmxcmd.support.StringifierRegistryIniter;

import java.util.List;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.TypeCast;

/**
 */
public final class JMXCmdCmdMgr extends CmdMgrImpl
{

    private void checkRequirements()
    {
        try
        {
            Class.forName("javax.management.j2ee.statistics.CountStatistic");
        }
        catch (Exception e)
        {
            mCmdOutput.println("WARNING: javax.management.j2ee.statistics package missing; " +
                    "add javax77.jar to classpath or jars dir");
        }


        try
        {
            Class.forName("javax.management.remote.generic.GenericConnector");
        }
        catch (Exception e)
        {
            mCmdOutput.println(
                    "WARNING: javax.management.remote.generic package missing; " +
                    "add jmxremote_optional.jar to classpath or jars dir");
        }

        try
        {
            Class.forName("org.glassfish.admin.amx.util.stringifier.Stringifier");
        }
        catch (Exception e)
        {
            mCmdOutput.println(
                    "ERROR: org.glassfish.admin.amx.util.stringifier package missing; " +
                    "add glassfish amx-api.jar to classpath or jars dir");
        }
    }


    /**
     */
    public JMXCmdCmdMgr(final Map<String,Object> metaOptions)
            throws Exception
    {
        super(metaOptions);

        checkRequirements();

        try
        {
            mCmdOutput.printDebug("aliases file = " + JMXCmd.getAliasesFile());

            new StringifierRegistryIniter(StringifierRegistryImpl.DEFAULT);

            mGreeting = GREETING;

            if (mCmdOutput.getDebug())
            {
                final Properties props = (Properties) mMetaOptions.get(JCmdKeys.PROPERTIES);

                final String value = props.getProperty(JMXCmdEnvKeys.DEBUG_CONNECTION);

                final boolean debugConnection = Boolean.TRUE.toString().equals(value);

                mCmdEnv.put(JMXCmdEnvKeys.DEBUG_CONNECTION, "" + debugConnection, false);
            }

            mCmdFactory.setUnknownCmdClassGetter(new MyCmdClassGetter(mCmdFactory.getUnknownCmdClassGetter()));

            getEnv().put(CmdEnvKeys.UNKNOWN_CMD_HELPER, new UnknownCmdHelperImpl(), false);
        }
        catch (Throwable t)
        {
            System.err.println(ExceptionUtil.getStackTrace(t));
            throw (Exception) t;
        }

    }

    private class MyCmdClassGetter implements CmdFactory.UnknownCmdClassGetter
    {

        final CmdFactory.UnknownCmdClassGetter mGetter;


        public MyCmdClassGetter(CmdFactory.UnknownCmdClassGetter next)
        {
            mGetter = next;
        }


        public Class<? extends Cmd> getCmdClass(String name)
        {
            Class<? extends Cmd> theClass = mGetter.getCmdClass(name);
            if (theClass == null)
            {
                theClass = InvokeCmd.class;
            }

            return (theClass);
        }
    }

    private class JMXCmdCmdSource implements CmdSource
    {

        JMXCmdCmdSource()
        {
        }


        @SuppressWarnings("unchecked")
        public List<Class<? extends Cmd>> getClasses()
        {
            final List<Class<? extends Cmd>> list = ListUtil.newList();
            for (final Class clazz : BUILT_IN_COMMANDS)
            {
                final Class<? extends Cmd> c = TypeCast.asClass(clazz);
                list.add(c);
            }
            return list;
        }
        private final Class[] BUILT_IN_COMMANDS =
        {
            GetCmd.class,
            SetCmd.class,
            FindCmd.class,
            InspectCmd.class,
            JavaCmd.class,
            MBeanCmd.class,
            ListenCmd.class,
            MonitorCmd.class,
            InvokeCmd.class,
            TargetAliasesCmd.class,
            ConnectCmd.class,
            CountCmd.class,
            DomainsCmd.class,
            ProvidersCmd.class,
            JMXCmdVersionCmd.class,
            MBeanServerCmd.class,
            ValidateMBeansCmd.class,
            GenerateMBeansCmd.class,
            ProxyCmd.class,
            SecurityCmd.class,
            NavCmd.class
        };
    }


    protected void initCmds()
            throws Exception
    {
        super.initCmds();

        final CmdFactoryIniter initer = new CmdFactoryIniter(mCmdFactory, new JMXCmdCmdSource());
    }
    private final static String GREETING =
            "Type 'help' for help, 'quit' to quit.\n";
}

