/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.devtests.web.configupgrades;

import com.sun.appserv.test.BaseDevTest;
import com.sun.tools.javac.resources.version;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;

public class ConfigUpgradeTest extends BaseDevTest {
    public static final String RESOURCES = "src/main/resources/";
    private String gfHome = System.getenv("S1AS_HOME") + "/domains/domain1/config";
    private File backup = new File(gfHome, "domain-backup.xml");
    private File original = new File(gfHome, "domain.xml");

    public static void main(String[] args) {
        new ConfigUpgradeTest().run();
    }

    @Override
    protected String getTestName() {
        return "config-upgrades";
    }

    @Override
    protected String getTestDescription() {
        return "config-upgrades";
    }

    public void run() {
        try {
            report("initial server stop", asadmin("stop-domain"));
            final File[] list = new File(RESOURCES).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("domain-") && name.endsWith(".xml");
                }
            });
            for (File file : list) {
                testUpgrade(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            report("final server restart", asadmin("start-domain"));
            stat.printSummary();
        }
    }

    private void testUpgrade(File legacy) throws IOException {
        String version = legacy.getName().replaceAll("domain-v", "").replaceAll("\\.xml", "");
        try {
            copy(original, backup);
            copy(legacy, original);
            final AsadminReturn ret = asadminWithOutput("start-domain", "--upgrade"/*, "--debug"*/);
            report(String.format("upgrade from version %s", version), !ret.outAndErr.contains("|SEVERE|"));
        } finally {
            restore();
        }
    }

    private void restore() throws IOException {
        if(backup.exists()) {
            copy(backup, original);
            backup.delete();
        }
    }

    private void copy(File sourceFile, File destinationFile) throws IOException {
        FileInputStream source = new FileInputStream(sourceFile);
        FileOutputStream destination = new FileOutputStream(destinationFile);
        try {
            byte[] bytes = new byte[16384];
            int read;
            while((read = source.read(bytes)) != -1) {
                destination.write(bytes, 0, read);
            }
        } finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.flush();
                destination.close();
            }
        }
    }
}
