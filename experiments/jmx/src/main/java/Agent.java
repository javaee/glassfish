/*
 * @(#)file      Agent.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.7
 * @(#)lastedit  04/04/21
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.sun.jmx.defaults.ServiceName;
import com.sun.jmx.interceptor.DefaultMBeanServerInterceptor;
import com.sun.jmx.interceptor.MBeanServerInterceptor;
import com.sun.jmx.mbeanserver.SunJmxMBeanServer;

import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * This class implements a simple Java DMK agent which instantiates a
 * {@link MasterMBeanServerInterceptor} and plugs in a
 * {@link FileMBeanServerInterceptor}.
 * <p/>
 * The <code>FileMBeanServerInterceptor</code> simulates virtual MBeans
 * which represent files and directories.
 *
 * @see DefaultMBeanServerInterceptor
 * @see FileMBeanServerInterceptor
 * @see MasterMBeanServerInterceptor
 */
public class Agent {

    private MBeanServer mbs;

    private static final String SEP_LINE =
        "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" +
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

    /**
     * Constructor:
     * - instantiates the MBean server of this agent,
     * - retrieves the MBean server ID from the MBean server delegate.
     */
    public Agent() {
        // Instantiates the MBean server
        //
        echo("\n\tInstantiating the MBean server of this agent...");
        mbs = ManagementFactory.getPlatformMBeanServer();
        echo("\tdone");

        // enable interceptor
        try {
            Field f = mbs.getClass().getDeclaredField("interceptorsEnabled");
            f.setAccessible(true);
            f.set(mbs,true);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    /**
     * This method creates a new {@link FileMBeanServerInterceptor} and
     * inserts it in the MBeanServer. This method first creates a
     * {@link FileMBeanServerInterceptor}, and then plugs it into a
     * {@link MasterMBeanServerInterceptor} that will route requests to either
     * the {@link DefaultMBeanServerInterceptor} or the
     * {@link FileMBeanServerInterceptor}, depending on the domain part of the
     * MBean's ObjectName. It then inserts the
     * {@link MasterMBeanServerInterceptor} into the given MBeanServer.
     *
     * @param server The MBeanServer where the new FileMBeanServerInterceptor
     *               is to be inserted.
     * @param domain The domain name reserved for the MBeans managed
     *               by the FileMBeanServerInterceptor.
     * @return The new FileMBeanServerInterceptor.
     * @throws IllegalArgumentException if the given MBeanServer is
     *                                  null, or does not support {@link MBeanServerInterceptor}s.
     */
    public static FileMBeanServerInterceptor insertFileInterceptor(
        MBeanServer server,
        String domain) {
        // The server must not be null
        //
        if (server == null)
            throw new IllegalArgumentException("MBeanServer can't be null");

        // We get the server's DefaultMBeanServerInterceptor. We will pass
        // this interceptor to our MasterMBeanServerInterceptor so that no
        // MBeans are lost.
        //
        final MBeanServerInterceptor defaultInterceptor =
            ((SunJmxMBeanServer) server).getMBeanServerInterceptor();

        // We get the MBeanServerDelegate. We will pass the MBeanServerDelegate
        // to our FileMBeanServerInterceptor so that we can fake the creation
        // and destruction of MBeans by sending MBeanServerNotifications through
        // the delegate.
        //
        final MBeanServerDelegate delegate =
            ((SunJmxMBeanServer) server).getMBeanServerDelegate();

        // We create the FileMBeanServerInterceptor. The
        // FileMBeanServerInterceptor handles virtual MBeans which represent
        // files and directories. All these MBeans will share the same domain.
        // No other MBean should use this domain. This is not imposed by Java
        // DMK MBeanServerInterceptors, but it is a design choice in this
        // example: both the FileMBeanServerInterceptor and the
        // MasterMBeanServerInterceptor implementations we provide here are
        // designed to work that way. The FileMBeanServerInterceptor will use
        // the MBeanServerDelegate in order to fake the creation and destruction
        // of MBeans by sending MBeanServerNotifications at startup, and when
        // the operation "cd" is invoked on a virtual Directory MBean. The
        // FileMBeanServerInterceptor will use the MBeanServer in order to
        // evaluate QueryExp objects.
        //
        final FileMBeanServerInterceptor fileInterceptor =
            new FileMBeanServerInterceptor(domain, delegate, server);

        // We create a new MasterMBeanServerInterceptor that will route requests
        // to either the DefaultMBeanServerInterceptor, or the
        // FileMBeanServerInterceptor.
        //
        // The MasterMBeanServerInterceptor will use the domain part of the
        // ObjectName in order to decide to which interceptor the requests
        // should be forwarded.
        //
        // The MasterMBeanServerInterceptor is needed so that the MBeanServer
        // can behave as a regular MBeanServer. If we had simply set the
        // FileMBeanServerInterceptor as default interceptor of the MBeanServer
        // then:
        // * all MBeans already registered in the DefaultMBeanServerInterceptor
        //   would be lost (this includes the HTML adaptor and also the
        //   MBeanServerDelegate),
        // * No user MBean creation or unregistration would be possible,
        //   since the FileMBeanServerInterceptor only handles its own virtual
        //   MBean.
        //
        // The MasterMBeanServerInterceptor makes it possible to keep the MBeans
        // from the DefaultMBeanServerInterceptor accessible, and reroute only
        // those requests that are targeted to the virtual file MBeans to
        // the FileMBeanServerInterceptor.
        //
        final MBeanServerInterceptor master =
            new MasterMBeanServerInterceptor(defaultInterceptor,
                fileInterceptor,
                domain);

        // We set the MasterMBeanServerInterceptor as the MBeanServer's default
        // MBeanServerInterceptor.
        //
        ((SunJmxMBeanServer) server).setMBeanServerInterceptor(master);

        return fileInterceptor;
    }

    /**
     * This method activates the FileSystem simulation.
     * <p/>
     * It first calls <tt>insertFileInterceptor()</tt> in order to
     * insert a {@link FileMBeanServerInterceptor} into the MBeanServer,
     * and then starts the inserted {@link FileMBeanServerInterceptor} on
     * the current directory.
     */
    public void showFiles() {
        echo("\n\tInserting the FileMBeanServerInterceptor in this agent...");
        FileMBeanServerInterceptor fileInterceptor =
            insertFileInterceptor(mbs, "file");
        echo("\tdone");
        echo("\n\tStarting the FileMBeanServerInterceptor...");
        fileInterceptor.start(".");
        echo("\tdone");
    }

    public static void main(String[] args) {
        // START
        //
        echo(SEP_LINE);
        echo("Creating an instance of this Agent...");
        Agent myAgent = new Agent();
        echo("\ndone");

        // ACTIVATE FILESYSTEM SIMULATION
        //
        echo("\nPress <Enter> to insert the FileMBeanServerInterceptor and ");
        echo("view the files from the local directory as virtual MBeans.");
        waitForEnterPressed();
        myAgent.showFiles();

        // END
        //
        echo("\nPress <Enter> to stop the agent...");
        waitForEnterPressed();

        // Remove the MBeans that we have added, i.e. the HTML adaptor.
        // Removes all MBeans except the delegate and the MBeans from
        // the "file" domain.
        //
        myAgent.removeMBeans("file");

        echo(SEP_LINE);
        System.exit(0);
    }

    public void removeMBeans(String excludedDomain) {
        echo(SEP_LINE);
        try {
            echo("Unregistering all MBeans except " +
                "the MBean server delegate");
            if (excludedDomain != null)
                echo("and the MBeans from the \"" + excludedDomain +
                    ":\" domain.\n");
            else
                echo("");
            echo("    Current MBean count = " +
                mbs.getMBeanCount() + "\n");

            Set<ObjectName> allMBeans = mbs.queryNames(null, null);
            for (ObjectName name : allMBeans) {
                if (name == null) continue;
                if (name.toString().equals(ServiceName.DELEGATE)) continue;
                if (excludedDomain != null &&
                    name.getDomain().equals(excludedDomain)) continue;
                echo("\tUnregistering " + name.toString());
                mbs.unregisterMBean(name);
            }
            echo("\n    Current MBean count = " +
                mbs.getMBeanCount() + "\n");
            echo("done\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void echo(String msg) {
        System.out.println(msg);
    }

    private static void waitForEnterPressed() {
        try {
            boolean done = false;
            while (!done) {
                char ch = (char) System.in.read();
                if (ch < 0 || ch == '\n') {
                    done = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
