/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package admin.monitoring;

import java.io.*;

/**
 * @author Byron Nevins
 */
class Constants {
    static final String DOMAIN_NAME = "mon-domain";
    static final String CLUSTER_NAME = "mon-cluster";
    static final String CLUSTERED_INSTANCE_NAME1 = "clustered-i1";
    static final String CLUSTERED_INSTANCE_NAME2 = "clustered-i2";
    static final String STAND_ALONE_INSTANCE_NAME = "standalone-i3";
    static final String NO_DATA = "No monitoring data to report.";
    final static String HIGH = "HIGH";
    final static String LOW = "LOW";
    final static String OFF = "OFF";
    final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    final static File installDir = new File(System.getenv("S1AS_HOME"));

    // this is annoying!!!
    final static String STAR = isWindows ? "\"*\"" : "*";
    final static String SERVERDOTSTAR = isWindows ? "\"server.*\"" : "server.*";

    final static String[] INSTANCES = new String[]{
        CLUSTERED_INSTANCE_NAME1,
        CLUSTERED_INSTANCE_NAME2,
        STAND_ALONE_INSTANCE_NAME
    };
    final static String[] CONFIG_NAMES = new String[]{
        CLUSTER_NAME,
        STAND_ALONE_INSTANCE_NAME,
        "server"
    };
    final static String[] LEVELS = new String[]{
        OFF, LOW, HIGH
    };

    final static String MON_CATEGORIES[] = new String[]{
        "http-service",
        "connector-connection-pool",
        "connector-service",
        "deployment",
        "ejb-container",
        "jdbc-connection-pool",
        "jersey",
        "jms-service",
        "jpa",
        "jvm",
        "orb",
        "security",
        "thread-pool",
        "transaction-service",
        "web-container",
        "web-services-container"
    };
    final static File RESOURCES_DIR = new File("resources").getAbsoluteFile();
    final static File BUILT_RESOURCES_DIR = new File("apps").getAbsoluteFile();
}
