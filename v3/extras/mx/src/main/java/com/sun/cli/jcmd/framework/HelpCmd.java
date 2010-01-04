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

/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/HelpCmd.java,v 1.11 2004/02/06 02:11:22 llc Exp $
 * $Revision: 1.11 $
 * $Date: 2004/02/06 02:11:22 $
 */
package com.sun.cli.jcmd.framework;

import java.util.Arrays;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import java.util.List;
import java.util.Set;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.SetUtil;

public class HelpCmd extends CmdBase
{

    public HelpCmd(final CmdEnv env)
    {
        super(env);
    }


    protected CmdFactory getCmdFactory()
    {
        return (super.getCmdFactory());
    }


    Cmd instantiateCmd(Class<? extends Cmd> cmdClass)
            throws Exception
    {
        Cmd cmd = null;

        final CmdEnvImpl env = new CmdEnvImpl();
        env.put(CmdEnvKeys.TOKENS, new String[0], false);

        final String[] names = getCmdNames(cmdClass);
        if (names != null)
        {
            cmd = getCmdFactory().createCmd(names[ 0], cmdClass, env);
        }

        return (cmd);
    }


    CmdHelp getHelpForCmd(Class<? extends Cmd> cmdClass)
            throws Exception
    {
        CmdHelp help = null;

        if (cmdClass == this.getClass())
        {
            help = new HelpCmdItselfHelp();
        }
        else if (cmdClass != null)
        {
            final Cmd cmd = instantiateCmd(cmdClass);
            if (cmd != null)
            {
                help = cmd.getHelp();
            }
        }

        return (help);
    }

    class HelpCmdItselfHelp extends CmdHelpImpl
    {

        public HelpCmdItselfHelp()
        {
            super(getCmdInfos());
        }
        private final static String HELP_NAME = "help";
        private final static String SYNOPSIS = "display help";
        private final static String HELP_TEXT =
                "To see all commands, type 'help'.  To see help for a particular command, " +
                "type 'help cmd'.";


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getText()
        {
            return (HELP_TEXT);
        }


        public String toString()
        {
            return (HELP_TEXT);
        }
    }

    final class HelpAllCommandsHelp extends CmdHelpImpl
    {

        public HelpAllCommandsHelp()
        {
            super(getCmdInfos());
        }
        private final static String SYNOPSIS = "display help";
        private final static String HELP_TEXT =
                "To see all commands, type 'help'.  To see help for a particular command, " +
                "type 'help cmd'.";


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getText()
        {
            return (HELP_TEXT);
        }


        public String toString()
        {
            final CmdFactory factory = getCmdFactory();

            final List<Class<? extends Cmd>> classes = factory.getClasses();
            final List<CmdInfos> cmdInfos = ListUtil.newList();
            final List<CmdHelp>  cmdHelps = ListUtil.newList();

            // make sure classes are unique
            final Set<Class<? extends Cmd>> classesSet = SetUtil.newSet();
            classesSet.addAll(classes);

            for (final Class<? extends Cmd> clazz : classesSet )
            {
                if ( clazz == null ) throw new IllegalArgumentException("null class");
                try
                {
                    final CmdInfos infos = getCmdInfos( clazz );
                    final CmdHelp help = getHelpForCmd( clazz );

                    if ( infos == null )
                    {
                        System.out.println( "Null CmdInfos from class " + clazz.getName() );
                        continue;
                    }
                    if ( help == null )
                    {
                        System.out.println( "Null CmdHelp from class " + clazz.getName() );
                        continue;
                    }
                    
                    cmdInfos.add( infos );
                    cmdHelps.add( help );
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            final String[] helps = new String[cmdHelps.size()];
            for (int classIdx = 0; classIdx < cmdHelps.size(); ++classIdx)
            {
                final CmdHelp   help  = cmdHelps.get(classIdx);
                final CmdInfos  infos = cmdInfos.get(classIdx);
                
                helps[classIdx] = help.getName() + " : " + help.getSynopsis();

                final int numSubCmds = infos.size();
                if (numSubCmds >= 2)
                {
                    for (int subCmdIdx = 0; subCmdIdx < numSubCmds; ++subCmdIdx)
                    {
                        helps[classIdx] += "\n    " + infos.get(subCmdIdx).getName();
                    }
                }
            }

            Arrays.sort(helps);
            return (ArrayStringifier.stringify(helps, "\n\n"));
        }
    }


    public CmdHelp getHelp()
    {
        return (new HelpAllCommandsHelp());
    }
    private final static String HELP_NAME1 = "help";
    private final static String HELP_NAME2 = "-?";
    private final static String HELP_NAME3 = "--help";


    private String stripColon(String cmd)
    {
        String cmdString = cmd;

        if (cmdString.endsWith(":"))
        {
            // indicates generic JMX method
            cmdString = cmdString.substring(0, cmdString.length() - 1);
        }
        return (cmdString);
    }


    protected String getHelpUnknown(String cmdString)
    {
        return ("no help available for command: " + cmdString);
    }
    static final OperandsInfo OPERANDS_INFO =
            new OperandsInfoImpl("command-name", 0);
    private static final CmdInfo HELP_INFO1 = new CmdInfoImpl(HELP_NAME1, null, OPERANDS_INFO);
    private static final CmdInfo HELP_INFO2 = new CmdInfoImpl(HELP_NAME2);
    private static final CmdInfo HELP_INFO3 = new CmdInfoImpl(HELP_NAME3);


    public static CmdInfos getCmdInfos()
    {
        // name can vary so new one up dynamically
        return (new CmdInfos(HELP_INFO1, HELP_INFO2, HELP_INFO3));
    }


    Class<? extends Cmd> searchForCmdClass(String cmdName)
            throws Exception
    {
        final CmdFactory cmdFactory = getCmdFactory();

        Class<? extends Cmd> result = cmdFactory.getClass(cmdName);
        if (result == null)
        {
            final List<Class<? extends Cmd>> classes = cmdFactory.getClasses();

            for (int i = 0; i < classes.size(); ++i)
            {
                final Cmd cmd = instantiateCmd(classes.get(i));
                final CmdHelp help = cmd.getHelp();

                // check if the overall name matches
                if (help.getName().equals(cmdName))
                {
                    result = classes.get(i);
                    break;
                }
            }
        }

        return (result);
    }


    protected String buildHelpMessage(UnknownCmdHelper helper)
            throws Exception
    {
        final String[] operands = getOperands();
        final CmdFactory cmdFactory = getCmdFactory();
        String msg = null;

        for (int i = 0; i < operands.length; ++i)
        {
            final String cmd = operands[i];

            Class<? extends Cmd> cmdClass = cmdFactory.getClass(cmd);
            if (cmdClass == null)
            {
                cmdClass = searchForCmdClass(cmd);
            }

            CmdHelp help = getHelpForCmd(cmdClass);
            /*
            if ( help == null )
            {
            final Class cmdClass	= cmdFactory.getClass( cmd );

            if ( cmdClass != null )
            {
            final String [] aka	= getCmdNames( cmdClass );
            if ( aka != null && aka.length != 0)
            {
            help	= getHelpForCmd( cmdFactory.getClass( aka[ 0 ] ) );
            }
            }
            }
             */


            if (help != null)
            {
                msg = help.toString();

                cmdClass = cmdFactory.getClass(cmd);
                if (cmdClass != null)
                {
                    msg = msg + "\n" + getAlsoKnownAs(cmdClass);
                }
            }
            else if (helper != null)
            {
                msg = helper.getHelpUnknown(cmd);
            }
        }
        return (msg);
    }

    public interface UnknownCmdHelper
    {

        public String getHelpUnknown(String cmd);
    }


    public static final UnknownCmdHelper getUnknownCmdHelper()
    {
        return (UnknownCmdHelperImpl.getInstance());
    }

    static final class UnknownCmdHelperImpl implements UnknownCmdHelper
    {

        public UnknownCmdHelperImpl()
        {
        }


        public String getHelpUnknown(String cmd)
        {
            return ("Unknown command: " + cmd);
        }
        private static final UnknownCmdHelperImpl INSTANCE = new UnknownCmdHelperImpl();


        public static UnknownCmdHelperImpl getInstance()
        {
            return (INSTANCE);
        }
    }


    /**
    Handle a help request. If help cannot be found, use helper to determine
    what to do.
     */
    public void handleHelp(UnknownCmdHelper helper)
            throws Exception
    {
        if (getArgHelper() == null)
        {
            preExecute();
        }

        final String[] operands = getOperands();

        if (operands.length == 0)
        {
            printUsage();
        }
        else
        {
            println(buildHelpMessage(helper));
        }
    }


    protected void executeInternal()
            throws Exception
    {
        printDebug("HelpCmd.executeInternal ");

        UnknownCmdHelper helper =
                (UnknownCmdHelper) envGet(CmdEnvKeys.UNKNOWN_CMD_HELPER);
        if (helper == null)
        {
            helper = UnknownCmdHelperImpl.getInstance();
        }

        handleHelp(helper);
    }
}
