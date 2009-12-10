/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/MonitorCmd.java,v 1.15 2005/11/15 20:59:53 llc Exp $
 * $Revision: 1.15 $
 * $Date: 2005/11/15 20:59:53 $
 */
package com.sun.cli.jmxcmd.cmd;

import java.io.File;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.RuntimeOperationsException;

import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.MBeanAttributeInfo;

import org.glassfish.admin.amx.util.stringifier.*;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;


import org.glassfish.admin.amx.util.jmx.JMXUtil;
import com.sun.cli.jcmd.util.misc.StringifiedList;

import com.sun.cli.jmxcmd.support.ArgParserImpl;
import com.sun.cli.jmxcmd.support.ArgParserException;


import com.sun.cli.jcmd.framework.IllegalUsageException;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdEvent;
import com.sun.cli.jcmd.framework.QuittingCmdEvent;
import com.sun.cli.jcmd.framework.CmdEventListener;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.CmdOutput;
import com.sun.cli.jcmd.framework.CmdOutputNull;
import com.sun.cli.jcmd.framework.CmdOutputN;
import com.sun.cli.jcmd.framework.CmdOutputToFile;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;
import org.glassfish.admin.amx.util.ArrayConversion;
import org.glassfish.admin.amx.util.CompareUtil;
import org.glassfish.admin.amx.util.RegexUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.jmx.ReadWriteAttributeFilter;

class MonitorInfo
{
    public final String mName;
    public final String[] mAttributes;
    public long mIntervalSeconds;
    public final String mFilename;
    public final boolean mStdout;
    public final String[] mTargets;


    MonitorInfo(String name, String[] targets,
                String[] attrs, int intervalSeconds, String filename, boolean stdout)
    {
        mName = name;
        mTargets = targets;
        mAttributes = attrs;
        mIntervalSeconds = intervalSeconds;
        mFilename = filename;
        mStdout = stdout;
    }


    MonitorInfo(String data)
    {
        final String[] pairs = data.split("" + FIELD_DELIM);

        if (pairs.length != 6)
        {
            throw new IllegalArgumentException("MonitorInfo requires 6 values");
        }
        mName = pairs[ 0];
        mAttributes = pairs[ 1].split("" + LIST_DELIM);
        mIntervalSeconds = new Integer(pairs[ 2]).intValue();
        mFilename = pairs[ 3].equals("null") ? null : pairs[ 3];
        mTargets = new StringifiedList(pairs[ 4], LIST_DELIM).toArray();
        mStdout = new Boolean(pairs[ 5]).booleanValue();
    }
    final static char NVDELIM = '=';
    final static char FIELD_DELIM = '\t';
    final static char LIST_DELIM = ':';


    /**
    Convert to a form suitable for reinstantiating.
     */
    public String toString()
    {
        final String f =
            mName + FIELD_DELIM +
            ArrayStringifier.stringify(mAttributes, "" + LIST_DELIM) + FIELD_DELIM +
            mIntervalSeconds + FIELD_DELIM +
            mFilename + FIELD_DELIM +
            new StringifiedList(mTargets, LIST_DELIM).toString() + FIELD_DELIM +
            mStdout;

        return (f);
    }
}

/**
Command for monitoring MBean attributes.  Not all MBeans support notifications, so this command
makes it possible to check for attribute changes in any case.
 */
public class MonitorCmd extends JMXCmd
{
    private static final Set<MonitorThread> mThreads;


    public MonitorCmd(final CmdEnv env)
    {
        super(env);
    }
    private final static OptionInfo ATTRIBUTES_OPTION = createAttributesOption();
    private final static OptionInfo INTERVAL_OPTION = new OptionInfoImpl("interval", "i", "seconds");
    private final static OptionInfo NAME_OPTION = new OptionInfoImpl("name", "n", "monitor-name", true);
    private final static OptionInfo FILE_OPTION = new OptionInfoImpl("file", "f", PATH_ARG);
    private final static OptionInfo STDOUT_OPTION = new OptionInfoImpl("stdout", "t");


    static
    {
        mThreads = new HashSet<MonitorThread>();
    }
    public static final int DEFAULT_POLL_SECONDS = 5;

    static final class MonitorCmdHelp extends CmdHelpImpl
    {
        private static final String MONITOR_NAME_OP = "<monitor-name>";


        public MonitorCmdHelp()
        {
            super(getCmdInfos());
        }
        private final static String SYNOPSIS = "monitor MBean attributes for changes";
        private final static String MONITOR_TEXT =
            "A monitor periodically polls the targets for changes to attributes. " +
            "Before you can use a monitor, you must define it.  This definition is saved for later use, even " +
            "if you quit and restart.\n\n" +
            "Starting a monitor will stop it if it is already running and use the current definition. " +
            "Output is emitted to the console unless a file is specified.  If a file is specified, and output to " +
            "the console is also desired, use the " + STDOUT_OPTION.getLongName() + " option.'" +
            "";


        public String getName()
        {
            return (MONITOR_CMD);
        }


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getText()
        {
            return (MONITOR_TEXT);
        }
    }


    public CmdHelp getHelp()
    {
        return (new MonitorCmdHelp());
    }
    public static final String MONITOR_CMD = "monitor";
    public static final String DEFINE_CMD = "define-monitor";
    public static final String DELETE_CMD = "delete-monitor";
    public static final String START_CMD = "start-monitor";
    public static final String STOP_CMD = "stop-monitor";
    public static final String LIST_CMD = "list-monitors";
    private static final OptionsInfo DEFINE_OPTIONS =
        new OptionsInfoImpl(new OptionInfo[]
        {
            ATTRIBUTES_OPTION, INTERVAL_OPTION,
            NAME_OPTION, STDOUT_OPTION, FILE_OPTION
        });
    private final static OperandsInfoImpl LISTENER_NAME_OPERAND =
        new OperandsInfoImpl("<listener-name>", 1, 1);
    private final static CmdInfo DEFINE_INFO =
        new CmdInfoImpl(DEFINE_CMD, DEFINE_OPTIONS, TARGETS_OPERAND_INFO);
    private final static CmdInfo DELETE_INFO =
        new CmdInfoImpl(DELETE_CMD, LISTENER_NAME_OPERAND);
    private final static CmdInfo START_INFO =
        new CmdInfoImpl(START_CMD, LISTENER_NAME_OPERAND);
    private final static CmdInfo STOP_INFO =
        new CmdInfoImpl(STOP_CMD, LISTENER_NAME_OPERAND);
    private final static CmdInfo LIST_INFO = new CmdInfoImpl(LIST_CMD);


    public static CmdInfos getCmdInfos()
    {
        return (new CmdInfos(DEFINE_INFO, DELETE_INFO, START_INFO, STOP_INFO, LIST_INFO));
    }


    void startMonitor(
        final MonitorInfo info)
        throws Exception
    {
        CmdOutput output = null;

        if (info.mFilename != null)
        {
            final File theFile = new File(info.mFilename);

            final CmdOutput fileOutput = new CmdOutputToFile(theFile);
            CmdOutput stdout = null;
            if (info.mStdout)
            {
                stdout = this;
            }
            else
            {
                stdout = new CmdOutputNull();
            }

            output = new CmdOutputN(new CmdOutput[]
                {
                    fileOutput, stdout
                });
        }
        else
        {
            output = this;
        }

        final ObjectName[] resolvedTargets = resolveTargets(getProxy(), info.mTargets);
        if (resolvedTargets.length != 0)
        {
            final MonitorThread m = new MonitorThread(info,
                this,
                getConnection(),
                resolvedTargets,
                new ChangeListenerImpl(this));
            mThreads.add(m);
            m.start();

            getCmdEventMgr().addListener(m);
        }
        else
        {
            printError("No targets found");
        }
    }


    MonitorInfo getMonitor(final String name)
    {
        MonitorInfo info = null;

        final String data = (String) envGet(MONITOR_PREFIX + name);
        if (data != null)
        {
            info = new MonitorInfo(data);
        }

        return (info);
    }


    void startMonitors(final String[] names)
        throws Exception
    {
        stopMonitors(names);

        for (int i = 0; i < names.length; ++i)
        {
            final String monitorName = names[i];

            final MonitorInfo info = getMonitor(monitorName);
            if (info == null)
            {
                printError("Monitor " + quote(monitorName) + " has not been defined.");
            }
            else
            {
                startMonitor(info);
            }
        }
    }


    void deleteMonitors(final String[] names)
        throws Exception
    {
        stopMonitors(names);

        for (int i = 0; i < names.length; ++i)
        {
            final String monitorName = names[i];

            if (envGet(MONITOR_PREFIX + monitorName) != null)
            {
                envRemove(MONITOR_PREFIX + monitorName);
                println("Deleted monitor: " + monitorName);
            }
            else
            {
                printError("Monitor " + quote(monitorName) + " has not been defined.");
            }
        }
    }


    Set<String> getAllMonitorNames()
    {
        Set<String> names = new HashSet<String>();

        final Set<String> monitors = getEnvKeys(MONITOR_PREFIX + ".*");
        final int prefixLength = MONITOR_PREFIX.length();

        for (final String m : monitors)
        {
            names.add(m.substring(prefixLength, m.length()));
        }
        return (names);
    }


    Set<String> getMonitorNames(final String[] monitorNames)
    {
        Set<String> names = new HashSet<String>();

        if (monitorNames == null || monitorNames.length == 0)
        {
            names = getAllMonitorNames();
        }
        else
        {
            names = ArrayConversion.arrayToSet(monitorNames);
        }

        return (names);
    }


    void stopMonitors(final String[] monitors)
        throws Exception
    {
        final Set<String> monitorsSet = getMonitorNames(monitors);

        for (final MonitorThread m : mThreads)
        {
            if (monitorsSet.contains(m.getMonitorName()))
            {
                println("Stopping: " + m.getMonitorName());
                m.quit();
                m.interrupt();
                mThreads.remove(m);
            }
        }
    }


    String getMonitorStatus(final String name)
    {
        String status = "";

        final MonitorThread thread = getMonitorThread(name);

        if (thread == null)
        {
            status = "stopped";
        }
        else
        {
            status = "running";
        }

        return (status);
    }


    void listMonitor(final MonitorInfo m)
    {
        println("Monitor: " + m.mName);
        println("\tstatus: " + getMonitorStatus(m.mName));
        println("\tpoll interval seconds: " + m.mIntervalSeconds);
        println("\tattributes: " + ArrayStringifier.stringify(m.mAttributes, ", "));
        println("\tfilename: " + m.mFilename);
        println("\tstdout: " + m.mStdout);
        println("\ttargets: " + ArrayStringifier.stringify(m.mTargets, ", "));

    }


    MonitorThread getMonitorThread(String name)
    {
        MonitorThread thread = null;

        final Iterator iter = mThreads.iterator();
        while (iter.hasNext())
        {
            final MonitorThread m = (MonitorThread) iter.next();

            if (m.getMonitorName().equals(name))
            {
                thread = m;
                break;
            }
        }

        return (thread);
    }


    void listMonitors()
    {
        final Set<String> monitors = getAllMonitorNames();

        if (monitors.size() == 0)
        {
            println("No monitors defined.");
        }
        else
        {
            final Iterator iter = monitors.iterator();
            while (iter.hasNext())
            {
                final String name = (String) iter.next();

                final MonitorInfo info = getMonitor(name);
                listMonitor(info);
            }
        }
    }


    private String[] parseAttrs(String attrs)
        throws ArgParserException
    {
        final ArgParserImpl parser = new ArgParserImpl();

        return (parser.ParseNames(attrs));
    }
    static final String MONITOR_PREFIX = "MONITOR_";


    protected void executeInternal()
        throws Exception
    {
        final String cmd = getSubCmdNameAsInvoked();

        if (cmd.equals(LIST_CMD))
        {
            listMonitors();
        }
        else if (cmd.equals(STOP_CMD))
        {
            stopMonitors(getOperands());
        }
        else if (cmd.equals(DEFINE_CMD))
        {
            final String[] targets = getTargets();

            printDebug("TARGETS: " + ArrayStringifier.stringify(targets, ", "));

            final String attrsOption = getString(ATTRIBUTES_OPTION.getShortName(), "*");
            final Integer intervalSecondsOption = getInteger(INTERVAL_OPTION.getShortName(), null);
            final String fileOption = getString(FILE_OPTION.getShortName(), null);
            final String nameOption = getString(NAME_OPTION.getShortName(), null);
            final boolean stdoutOption = getBoolean(STDOUT_OPTION.getShortName(), Boolean.FALSE).booleanValue();

            assert (nameOption != null);

            // parse the attributes option into a String[]
            final String[] attrNames = parseAttrs(attrsOption);

            final int interval = intervalSecondsOption == null ? DEFAULT_POLL_SECONDS : intervalSecondsOption.intValue();

            final MonitorInfo info = new MonitorInfo(nameOption,
                targets,
                attrNames,
                interval,
                fileOption,
                stdoutOption);

            envPut(MONITOR_PREFIX + nameOption, info.toString(), true);
            println("Created monitor " + nameOption);
        }
        else if (cmd.equals(DELETE_CMD))
        {
            deleteMonitors(getOperands());
        }
        else if (cmd.equals(START_CMD))
        {
            establishProxy();
            startMonitors(getOperands());
        }
        else
        {
            throw new IllegalUsageException(cmd);
        }
    }

    class ChangeListenerImpl implements MonitorThread.ChangeListener
    {
        final CmdOutput mOutput;


        public ChangeListenerImpl(CmdOutput output)
        {
            mOutput = output;
        }


        public void attributesChanged(
            final MBeanServerConnection conn,
            final ObjectName objectName,
            final MonitorThread.AttributeChangedInfo[] changes)
        {
            final SmartStringifier stringifier = new SmartStringifier(",", true);

            mOutput.println("");
            mOutput.println("Attribute changes for: " + objectName);

            for (int i = 0; i < changes.length; ++i)
            {
                final MonitorThread.AttributeChangedInfo change = changes[i];

                final String oldString = stringifier.stringify(change.mOldValue);
                final String newString = stringifier.stringify(change.mNewValue);

                mOutput.println(change.mName + ": " + oldString + " => " + newString);
            }
        }


        public void exceptionOccured(
            MBeanServerConnection conn,
            ObjectName objectName,
            Exception e)
        {
            mOutput.printError("Exception monitoring: " + objectName + " = " + e.getMessage());
        }
    };
}

final class MonitoringTarget
{
    private final MBeanServerConnection mConn;
    private final ObjectName mObjectName;
    private final String[] mAttributeNames;
    private Map<String, Object> mLastAttributes;


    ObjectName getObjectName()
    {
        return (mObjectName);
    }


    Map<String, Object> getLastAttributes()
    {
        return (mLastAttributes);
    }


    String[] getAttributeNames()
    {
        return (mAttributeNames);
    }


    void setLastAttributes(Map<String, Object> attrs)
    {
        mLastAttributes = attrs;
    }


    public MonitoringTarget(
        MBeanServerConnection conn,
        ObjectName objectName,
        String[] attributeNames)
        throws java.io.IOException, ReflectionException, InstanceNotFoundException, IntrospectionException
    {
        mConn = conn;
        mObjectName = objectName;
        mLastAttributes = new HashMap<String, Object>();
        mAttributeNames = resolveAttrNames(conn, objectName, attributeNames);
    }


    public boolean haveAttributesToMonitor()
    {
        return (mAttributeNames.length != 0);
    }


    private static String[] matchCandidateNames(final String[] candidates, final Pattern[] patterns)
    {
        final HashSet<String> s = new HashSet<String>();

        for (int c = 0; c < candidates.length; ++c)
        {
            final String candidate = candidates[c];

            // see if the candidate matches any of the patterns
            for (int p = 0; p < patterns.length; ++p)
            {
                if (patterns[p].matcher(candidate).matches())
                {
                    s.add(candidate);
                    break;
                }
            }
        }

        return ((String[]) ArrayConversion.setToArray(s, new String[s.size()]));
    }
    /**
    Winnow the names down to only those that actually exist.
     */
    public final static String ALL_ATTRS = "*";
    public final static String ALL_ATTRS_READ_ONLY = ALL_ATTRS + "r";
    public final static String ALL_ATTRS_WRITEABLE = ALL_ATTRS + "w";


    private static String[] resolveAttrNames(
        MBeanServerConnection conn,
        ObjectName objectName,
        final String[] attrNames)
        throws java.io.IOException, ReflectionException,
        InstanceNotFoundException, IntrospectionException
    {
        String[] candidateNames = null;
        boolean wildcard = false;

        // special case of *, *r, *w
        if (attrNames.length == 1 &&
            (attrNames[ 0].equals(ALL_ATTRS) ||
            attrNames[ 0].equals(ALL_ATTRS_READ_ONLY) ||
            attrNames[ 0].equals(ALL_ATTRS_WRITEABLE)))
        {
            boolean includeReadOnly = true;
            boolean includeWriteable = true;

            if (!attrNames[ 0].equals(ALL_ATTRS))
            {
                includeReadOnly = attrNames[ 0].equals(ALL_ATTRS_READ_ONLY);
                includeWriteable = attrNames[ 0].equals(ALL_ATTRS_WRITEABLE);
            }

            final MBeanAttributeInfo[] allInfos = JMXUtil.getAttributeInfos(conn, objectName);
            final MBeanAttributeInfo[] readOnly = includeReadOnly ? JMXUtil.filterAttributeInfos(allInfos, ReadWriteAttributeFilter.WRITEABLE_FILTER) : null;
            final MBeanAttributeInfo[] writeable = includeWriteable ? JMXUtil.filterAttributeInfos(allInfos, ReadWriteAttributeFilter.READ_ONLY_FILTER) : null;

            final Set<MBeanAttributeInfo> candidateInfos = new HashSet<MBeanAttributeInfo>();
            if (includeReadOnly)
            {
                candidateInfos.addAll(SetUtil.newSet(readOnly));
            }
            if (includeWriteable)
            {
                candidateInfos.addAll(SetUtil.newSet(writeable));
            }

            final MBeanAttributeInfo[] finalInfos = (MBeanAttributeInfo[]) candidateInfos.toArray(new MBeanAttributeInfo[candidateInfos.size()]);

            candidateNames = JMXUtil.getAttributeNames(finalInfos);
        }
        else
        {
            candidateNames = JMXUtil.getAllAttributeNames(conn, objectName);

            // select matching names from candidateNames
            final Pattern[] patterns = RegexUtil.exprsToPatterns(attrNames);
            // filter candidate names
            candidateNames = matchCandidateNames(candidateNames, patterns);
        }

        /*
        Poll now with this set to figure out what names actually exist on this MBean.
         */
        final AttributeList attrList = JMXUtil.getAttributesRobust(conn, objectName, candidateNames, null);
        final Map<String, Object> attrMap = JMXUtil.attributeListToValueMap(attrList);

        return ((String[]) attrMap.keySet().toArray(new String[attrMap.size()]));
    }
}

class MonitorThread extends Thread implements CmdEventListener
{
    MonitorInfo mInfo;
    final MonitoringTarget[] mMonitoringTargets;
    final MBeanServerConnection mConn;
    boolean mQuit;
    final ChangeListener mListener;
    final CmdOutput mOutput;


    long getInterval()
    {
        return (mInfo.mIntervalSeconds);
    }


    MonitoringTarget[] getTargets()
    {
        return (mMonitoringTargets);
    }


    public MonitorThread(
        final MonitorInfo info,
        final CmdOutput output,
        final MBeanServerConnection conn,
        final ObjectName[] objectNames,
        ChangeListener listener)
        throws java.io.IOException, ReflectionException, InstanceNotFoundException, IntrospectionException
    {
        super(info.mName);

        mInfo = info;
        mOutput = output;
        mConn = conn;
        mQuit = false;
        mListener = listener;

        final List<MonitoringTarget> targetList = new ArrayList<MonitoringTarget>();
        for (int i = 0; i < objectNames.length; ++i)
        {
            try
            {
                final MonitoringTarget target = new MonitoringTarget(mConn, objectNames[i], info.mAttributes);
                if (target.haveAttributesToMonitor())
                {
                    targetList.add(target);
                    mOutput.println("Monitoring: " + objectNames[i]);
                }
            }
            catch (RuntimeOperationsException e)
            {
                // flaky MBean--ignore
                mOutput.println("ignoring flaky MBean: " + objectNames[i]);
            }
            catch (java.io.IOException e)
            {
                // see if the connection is really dead by making another call
                mConn.isRegistered(objectNames[i]);

                // not really dead
                mOutput.println("ignoring MBean which threw IOException: " + objectNames[i]);
            }

        }

        mMonitoringTargets = (MonitoringTarget[]) targetList.toArray(new MonitoringTarget[targetList.size()]);
    }


    String getMonitorName()
    {
        return (mInfo.mName);
    }


    public void acceptCmdEvent(CmdEvent event)
    {
        if (event instanceof QuittingCmdEvent)
        {
            // process is quitting, shut our stuff down
            quit();
            interrupt();
        }
    }


    public String getListenerName()
    {
        return (toString());
    }

    public class AttributeChangedInfo
    {
        public String mName;
        public Object mOldValue;
        public Object mNewValue;


        AttributeChangedInfo(String name, Object oldValue, Object newValue)
        {
            mName = name;
            mOldValue = oldValue;
            mNewValue = newValue;
        }
    }

    public interface ChangeListener
    {
        public void attributesChanged(MBeanServerConnection conn,
                                      ObjectName objectName, final AttributeChangedInfo[] changes);


        public void exceptionOccured(MBeanServerConnection conn,
                                     ObjectName objectName, Exception e);
    };


    private AttributeChangedInfo[] compare(MonitoringTarget data, Map currentAttrs)
    {
        final List<AttributeChangedInfo> changeList = new ArrayList<AttributeChangedInfo>();

        final Iterator oldIter = data.getLastAttributes().keySet().iterator();
        while (oldIter.hasNext())
        {
            final String key = (String) oldIter.next();

            final Attribute oldAttr = (Attribute) data.getLastAttributes().get(key);
            final Attribute newAttr = (Attribute) currentAttrs.get(oldAttr.getName());

            if (newAttr == null ||
                !CompareUtil.objectsEqual(oldAttr.getValue(), newAttr.getValue()))
            {
                final AttributeChangedInfo info = new AttributeChangedInfo(oldAttr.getName(),
                    oldAttr.getValue(),
                    newAttr == null ? null : newAttr.getValue());

                changeList.add(info);
            }
        }

        return changeList.toArray(new AttributeChangedInfo[changeList.size()]);
    }


    private Map<String, Object> poll(final MonitoringTarget info)
        throws java.io.IOException, ReflectionException, InstanceNotFoundException
    {
        final AttributeList attrList = JMXUtil.getAttributesRobust(mConn,
            info.getObjectName(), info.getAttributeNames(), null);

        return (JMXUtil.attributeListToValueMap(attrList));
    }


    private void poll()
    {
        for (int i = 0; i < mMonitoringTargets.length; ++i)
        {
            final MonitoringTarget target = mMonitoringTargets[i];

            try
            {
                final Map<String, Object> currentAttrs = poll(target);

                final AttributeChangedInfo[] changes = compare(target, currentAttrs);

                target.setLastAttributes(currentAttrs);

                if (changes.length != 0)
                {
                    mListener.attributesChanged(mConn, target.getObjectName(), changes);
                }
            }
            catch (Exception e)
            {
                mListener.exceptionOccured(mConn, target.getObjectName(), e);
            }
        }
    }


    public void quit()
    {
        mQuit = true;
    }


    public void waitInterval()
    {
        try
        {
            sleep(mInfo.mIntervalSeconds * 1000L);
        }
        catch (InterruptedException e)
        {
        }
    }


    public void run()
    {
        while (!mQuit)
        {
            waitInterval();

            if (mQuit)
            {
                break;
            }

            poll();
        }
    }
}
