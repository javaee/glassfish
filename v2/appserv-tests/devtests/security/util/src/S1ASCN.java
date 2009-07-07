/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package devtests.security.util;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;

//import com.sun.enterprise.util.net.NetUtils;

public class S1ASCN extends Java {
    public S1ASCN() {
    }

    public void execute() throws BuildException {
        String hostName = null;
        try {
//            hostName = NetUtils.getCanonicalHostName();                       
	      hostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception ex) {
            hostName = "localhost";
        }
        getProject().setNewProperty("s1asCN", hostName);
    }
}
