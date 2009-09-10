package com.sun.enterprise.universal.process;

import com.sun.enterprise.util.StringUtils;
import java.lang.management.ManagementFactory;

/**
 * Includes a somewhat kludgy way to get the pid for "me".
 * Another casualty of the JDK catering to the LEAST common denominator.
 * Some obscure OS might not have a pid!
 * The name returned from the JMX method is like so:
 * 12345@mycomputername where 12345 is the PID
 * @author bnevins
 */
public final class ProcessUtils {
    private ProcessUtils() {
    }

    /**
     * Try and find the Process ID of "our" process.
     * @return the process id or -1 if not known
     */
    public static final int getPid() {
        return pid;
    }

    private static final int pid;

    static {
        int tempPid = -1;

        try {
            String pids = ManagementFactory.getRuntimeMXBean().getName();
            int index = -1;

            if(StringUtils.ok(pids) && (index = pids.indexOf('@')) >= 0) {
                tempPid = Integer.parseInt(pids.substring(0, index));
            }
        }
        catch(Exception e) {
            tempPid = -1;
        }

        // final assignment
        pid = tempPid;
    }
}
