/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdMgrImpl.java,v 1.25 2005/05/20 00:50:52 llc Exp $
 * $Revision: 1.25 $
 * $Date: 2005/05/20 00:50:52 $
 */
package com.sun.cli.jcmd.framework;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import com.sun.cli.jcmd.JCmdKeys;
import org.glassfish.admin.amx.util.ExceptionUtil;
import com.sun.cli.jcmd.util.misc.StringifiedList;
import java.util.List;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.DebugState;
import org.glassfish.admin.amx.util.LineReaderImpl;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.StringUtil;

/**
A complete implementation of CmdMgr.  Assumptions of this implementation:
- all invocations require the use of a subcommand
- starting up with no arguments causes an interactive mode to be entered
 */
public class CmdMgrImpl implements CmdMgr, CmdEventListener
{

    protected final CmdEnvImpl mCmdEnv;
    protected final CmdOutput mCmdOutput;
    protected final CmdFactory mCmdFactory;
    protected final CmdEventMgr mCmdEventMgr;
    protected final CmdRunner mCmdRunner;
    protected final CmdAliasMgr mCmdAliasMgr;
    protected final File mPropertiesFile;
    protected final CmdHistoryImpl mCmdHistory;
    protected int mLastCmdErrorCode;
    protected Map mMetaOptions;
    protected String mGreeting;


    /**
    Enter interactive mode.  Most implementations will want to call
    handleInteractive( String ) or they can use this as written.

    @return result code
     */
    protected int handleInteractive()
    {
        handleInteractive(mGreeting);

        return (mLastCmdErrorCode);
    }

    class MyDebugState implements DebugState
    {

        final CmdEnv mCmdEnv;


        public MyDebugState(CmdEnv env)
        {
            mCmdEnv = env;
        }


        public boolean getDebug()
        {
            boolean isDebug = false;

            final String value = (String) mCmdEnv.get(CmdEnvKeys.DEBUG);
            if (value != null)
            {
                isDebug = value.equalsIgnoreCase("true");
            }

            return (isDebug);
        }
    }

    final class MyCmdOutput extends CmdOutputImpl
    {

        public MyCmdOutput(CmdEnv env, PrintStream output, PrintStream errorOutput)
        {
            super(output, errorOutput, output, new MyDebugState(env));
        }
    }


    /**
    Construct a new implementation using the specified properties file.

    The properties file will be used.
     */
    public CmdMgrImpl(final Map<String,Object> metaOptions)
            throws Exception
    {
        mMetaOptions = metaOptions;

        mPropertiesFile = FileNames.getInstance().getEnvFile();

        mGreeting = "";

        mCmdEnv = new CmdEnvImpl();
        mCmdEventMgr = new CmdEventMgr();
        mCmdEnv.put(CmdEnvKeys.CMD_EVENT_MGR, mCmdEventMgr, false);
        mCmdEventMgr.addListener(this);
        mCmdAliasMgr = new CmdAliasMgr();
        mCmdEnv.put(CmdEnvKeys.CMD_ALIAS_MGR, mCmdAliasMgr, false);

        mCmdOutput = new MyCmdOutput(mCmdEnv, System.out, System.err);
        mCmdEnv.put(CmdEnvKeys.CMD_OUTPUT, mCmdOutput, false);
        mCmdEnv.put(JCmdKeys.CLI_NAME_OPTION.getLongName(), getCLIName(), false);

        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        if (loader instanceof FrameworkClassLoader)
        {
            ((FrameworkClassLoader) loader).setCmdOutput(mCmdOutput);
        }
        else
        {
            //mCmdOutput.printError( "WARNING: classloader is not JMXAdminClassLoader" );
        }


        mCmdFactory = new CmdFactory();
        mCmdEnv.put(CmdEnvKeys.CMD_FACTORY, mCmdFactory, false);

        mCmdRunner = new CmdRunnerImpl(mCmdFactory, mCmdEnv, new MyCmdRunnerHook());
        mCmdEnv.put(CmdEnvKeys.CMD_RUNNER, mCmdRunner, false);

        mCmdHistory = new CmdHistoryImpl();
        mCmdEnv.put(CmdEnvKeys.CMD_HISTORY, mCmdHistory, false);

        final String debug = (String) mMetaOptions.get(JCmdKeys.DEBUG_META_OPTION.getLongName());
        if (debug != null && new Boolean(debug).booleanValue())
        {
            mCmdEnv.put(CmdEnvKeys.DEBUG, "" + debug, false);
        }


        mCmdFactory.setUnknownCmdClassGetter(new CmdMgrImplUnknownCmdClassGetter());

        mCmdOutput.printDebug("env file = " + FileNames.getInstance().getEnvFile());
        mCmdOutput.printDebug("history file = " + FileNames.getInstance().getHistoryFile());
        mCmdOutput.printDebug("cmd-aliases file = " + FileNames.getInstance().getCmdAliasesFile());


        // load command history

        final File historyFile = FileNames.getInstance().getHistoryFile();
        if (historyFile.exists())
        {
            try
            {
                mCmdHistory.restore(historyFile);
            }
            catch (Exception e)
            {
                mCmdOutput.printDebug("Error reading history file " + historyFile + ":\n" +
                        ExceptionUtil.getStackTrace(e));
            }
        }
        mCmdHistory.autoSave(historyFile);


        // load command aliases
        final File cmdAliasesFile = FileNames.getInstance().getCmdAliasesFile();
        if (cmdAliasesFile.exists())
        {
            try
            {
                mCmdAliasMgr.restore(cmdAliasesFile);
            }
            catch (Exception e)
            {
                mCmdOutput.printDebug("Error reading cmd-aliases file " + cmdAliasesFile + ":\n" +
                        ExceptionUtil.getStackTrace(e));
            }
        }
    }
    protected static final String REPEAT_CMD_PREFIX = "!";

    protected class CmdMgrImplUnknownCmdClassGetter implements CmdFactory.UnknownCmdClassGetter
    {

        public CmdMgrImplUnknownCmdClassGetter()
        {
        }


        public Class<? extends Cmd> getCmdClass(String name)
        {
            Class<? extends Cmd> theClass = null;

            if (name.startsWith(REPEAT_CMD_PREFIX))
            {
                theClass = HistoryCmd.class;
            }

            return (theClass);
        }
    }


    public void acceptCmdEvent(CmdEvent event)
    {
        if (event instanceof QuittingCmdEvent)
        {
            // save history
            final File historyFile = FileNames.getInstance().getHistoryFile();
            try
            {
                mCmdHistory.save(historyFile);
            }
            catch (Exception e)
            {
                mCmdOutput.printDebug("Error saving history file " + historyFile + ":\n" +
                        ExceptionUtil.getStackTrace(e));
            }


            // save command aliases
            final File cmdAliasesFile = FileNames.getInstance().getCmdAliasesFile();
            try
            {
                mCmdAliasMgr.save(cmdAliasesFile);
            }
            catch (Exception e)
            {
                mCmdOutput.printDebug("Error saving cmd aliases file " + cmdAliasesFile + ":\n" +
                        ExceptionUtil.getStackTrace(e));
            }
        }
    }


    public String getListenerName()
    {
        return (toString());
    }


    public CmdEnv getEnv()
    {
        return (mCmdEnv);
    }


    public CmdFactory getCmdFactory()
    {
        return (mCmdFactory);
    }


    public CmdRunner getCmdRunner()
    {
        return (mCmdRunner);
    }


    protected String getCLIName()
    {
        final String name = (String) mMetaOptions.get(JCmdKeys.CLI_NAME_OPTION.getLongName());
        return (name);
    }


    protected String getPrompt()
    {
        return (getCLIName() + "> ");
    }


    protected void handleInteractive(String greeting)
    {
        final LineReaderImpl lineReader = new LineReaderImpl(System.in);
        final CmdReader reader = new CmdReader(mCmdOutput,
                getPrompt(), new CmdAliasLineHook(mCmdAliasMgr));

        try
        {
            reader.goInteractive(greeting, lineReader, mCmdRunner);
        }
        catch (Throwable t)
        {
            mCmdOutput.printDebug("Exception from processing commands: " + t.getMessage());
            t.printStackTrace();
        }

        mCmdEventMgr.dispatchEvent(new QuittingCmdEvent(this));
    }

    @SuppressWarnings("unchecked")
    public class FrameworkCmdSource implements CmdSource
    {
        public List<Class<? extends Cmd>> getClasses()
        {
            final List<Class<? extends Cmd>> classes = ListUtil.newList();
            for( final Class clazz : BUILT_IN_COMMANDS )
            {
                classes.add( clazz );
            }
            return classes;
        }
        private final Class[] BUILT_IN_COMMANDS =
        {
            HelpCmd.class,
            SourceCmd.class,
            SetenvCmd.class,
            ConfigureCmd.class,
            EchoCmd.class,
            TimeCmd.class,
            HistoryCmd.class,
            CmdAliasesCmd.class,
        };
    }


    /**
    Register all available commands specified by the CmdSource objects in the environment.
     */
    protected void initEnvCmds()
            throws Exception
    {
        // initialize all non-built-in commands
        final String commandSources = (String) mCmdEnv.get(CmdEnvKeys.COMMAND_SOURCES);
        if (commandSources != null)
        {
            final CmdFactoryIniter initer = new CmdFactoryIniter(mCmdFactory);

            final StringifiedList list = new StringifiedList(commandSources);

            final java.util.Iterator iter = list.iterator();
            while (iter.hasNext())
            {
                final String classname = (String) iter.next();

                if (classname.length() != 0)
                {
                    //System.out.println( "Attempting to load: " + classname );
                    try
                    {
                        final Class theClass = ClassUtil.getClassFromName(classname);
                        final CmdSource source = (CmdSource) theClass.newInstance();

                        initer.addMappings(source);
                        //System.out.println( "Loaded: " + classname );
                    }
                    catch (Exception e)
                    {
                        mCmdOutput.printError("Can't initialize CmdSource given by class: " +
                                StringUtil.quote(classname) + ": " + e.getClass().getName() + ": " +
                                e.getMessage());
                    }
                    catch (Error e)
                    {
                        mCmdOutput.printError("Can't initialize CmdSource given by class: " +
                                StringUtil.quote(classname) + ": " + e.getClass().getName() + ": " +
                                e.getMessage());
                    }
                }
                else
                {
                    mCmdOutput.printDebug("CmdSource list " +
                            StringUtil.quote(commandSources) +
                            " contains an empty string");
                }
            }
        }
    }

    private class MyCmdRunnerHook implements CmdRunner.Hook
    {

        private MyCmdRunnerHook()
        {
        }


        public void preExecute(String cmdName, String[] tokens)
        {
            // ignore
        }


        public void postExecute(String cmdName, String[] tokens, int errorCode)
        {
            if (errorCode != 0)
            {
                mLastCmdErrorCode = errorCode;
            }
        }
    }


    protected void initCmds() throws Exception
    {
        // load the built-in framework commands
        new CmdFactoryIniter(mCmdFactory, new FrameworkCmdSource());

        // load any configured commands
        initEnvCmds();
    }


    protected int handleSingle(final String[] args)
            throws Exception
    {
        // the first argument is the sub-command name
        final String cmd = args[ 0];

        return (mCmdRunner.execute(cmd, args));
    }


    protected void addInClasspath()
    {
        ConfigureCmd.setupFrameworkClassLoader(mCmdEnv);
    }


    protected void loadEnv()
            throws java.io.IOException
    {
        if (mPropertiesFile != null)
        {
            mCmdEnv.load(mPropertiesFile);
        }
    }


    protected void storeEnv()
            throws java.io.IOException
    {
        if (mPropertiesFile != null)
        {
            mCmdEnv.store(mPropertiesFile);
        }
    }


    CmdHistory getCmdHistory()
    {
        return (mCmdHistory);
    }


    protected void executeStartupScript(String theFile)
            throws Exception
    {
        // execute the 'source' command on theFile
        handleSingle(new String[]
                {
                    "source", theFile
                });
    }


    /**
    Startup is complete.  Perform an initialization desired before we start processing/accepting
    input.
     */
    protected void startupComplete()
            throws Exception
    {
        String startupScript = null;

        startupScript = (String) mCmdEnv.get(CmdEnvKeys.STARTUP_SCRIPT);
        if (startupScript != null)
        {
            executeStartupScript(startupScript);
        }
    }


    public int run(final String[] args)
            throws Exception
    {
        loadEnv();
        addInClasspath();

        initCmds();

        int errorCode;

        startupComplete();

        // if there are no arguments, start up in interactive mode
        if (args.length == 0)
        {
            errorCode = handleInteractive();
        }
        else
        {
            errorCode = handleSingle(args);
        }

        return (errorCode);
    }
}

